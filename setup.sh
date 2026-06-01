#!/usr/bin/env bash
# setup.sh — Install all dependencies for the weather-aggregator project
# Supports: macOS, Linux (Debian/Ubuntu, RHEL/Fedora), Windows (Git Bash / WSL)
#
# Java isolation: uses SDKMAN
#   - installs SDKMAN into ~/.sdkman
#   - installs JDK 21 via SDKMAN
#   - writes .sdkmanrc so `sdk env` auto-selects JDK 21 in this project
# Node isolation: node_modules/ inside frontend/ (standard npm, project-local)
# PostgreSQL: Docker Compose (docker-compose.yml) — container weather-postgres on port 5432

set -e

# ─── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[OK]${NC}   $1"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
error()   { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

JAVA_VERSION="21.0.7-tem"   # Temurin (Eclipse Adoptium) JDK 21 via SDKMAN
JAVA_VERSION_SHORT=21
POSTGRES_DB=weatherdb
POSTGRES_USER=weather
POSTGRES_PASS=weather
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ─── Detect OS ────────────────────────────────────────────────────────────────
detect_os() {
  case "$(uname -s)" in
    Darwin)  OS="mac" ;;
    Linux)   OS="linux"
             if [ -f /etc/debian_version ]; then DISTRO="debian"
             elif [ -f /etc/redhat-release ]; then DISTRO="rhel"
             else DISTRO="unknown"; fi ;;
    MINGW*|MSYS*|CYGWIN*) OS="windows" ;;
    *) error "Unsupported OS: $(uname -s)" ;;
  esac
  info "Detected OS: $OS ${DISTRO:-}"
}

# ─── Shell profile helper ─────────────────────────────────────────────────────
get_shell_profile() {
  if [ -n "$ZSH_VERSION" ] || [ "$SHELL" = "/bin/zsh" ]; then
    echo "$HOME/.zshrc"
  else
    echo "$HOME/.bashrc"
  fi
}

append_to_profile() {
  local profile; profile="$(get_shell_profile)"
  local marker="$1"; local content="$2"
  if ! grep -qF "$marker" "$profile" 2>/dev/null; then
    echo "$content" >> "$profile"
  fi
}

# ─── Homebrew (macOS only) ────────────────────────────────────────────────────
install_homebrew() {
  if [ "$OS" != "mac" ]; then return; fi
  if command -v brew &>/dev/null; then success "Homebrew already installed"; return; fi
  info "Installing Homebrew..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  success "Homebrew installed"
}

# ─── SDKMAN ───────────────────────────────────────────────────────────────────
install_sdkman() {
  if [ "$OS" = "windows" ]; then
    warn "SDKMAN not supported on native Windows — use WSL or install JDK manually"
    return
  fi

  if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    success "SDKMAN already installed"
  else
    info "Installing SDKMAN (Java version manager)..."
    curl -s "https://get.sdkman.io" | bash
    success "SDKMAN installed"
  fi

  # Load SDKMAN into current shell session
  export SDKMAN_DIR="$HOME/.sdkman"
  # shellcheck disable=SC1091
  source "$HOME/.sdkman/bin/sdkman-init.sh"

  # Ensure SDKMAN is sourced in shell profile
  append_to_profile "sdkman-init.sh" \
    $'\n# SDKMAN\nexport SDKMAN_DIR="$HOME/.sdkman"\n[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"'
}

# ─── Java 21 via SDKMAN (isolated, project-pinned) ───────────────────────────
install_java() {
  if [ "$OS" = "windows" ]; then
    warn "On Windows: download JDK 21 from https://adoptium.net/ and add to PATH"
    return
  fi

  # Load SDKMAN if not already loaded
  if ! command -v sdk &>/dev/null; then
    export SDKMAN_DIR="$HOME/.sdkman"
    # shellcheck disable=SC1091
    source "$HOME/.sdkman/bin/sdkman-init.sh" 2>/dev/null || error "SDKMAN not found — run install_sdkman first"
  fi

  info "Installing JDK $JAVA_VERSION via SDKMAN..."
  # sdk install is a no-op if already installed
  sdk install java "$JAVA_VERSION" 2>/dev/null || true
  sdk default java "$JAVA_VERSION"
  success "JDK $JAVA_VERSION set as default via SDKMAN"

  # Write .sdkmanrc — auto-selects this JDK when you cd into the project
  if [ ! -f "$PROJECT_DIR/.sdkmanrc" ]; then
    echo "java=$JAVA_VERSION" > "$PROJECT_DIR/.sdkmanrc"
    success "Created .sdkmanrc (JDK $JAVA_VERSION pinned to this project)"
  else
    success ".sdkmanrc already exists"
  fi

  # Enable auto-env in SDKMAN config so .sdkmanrc is read automatically on cd
  SDKMAN_CONFIG="$HOME/.sdkman/etc/config"
  if [ -f "$SDKMAN_CONFIG" ] && grep -q "sdkman_auto_env=false" "$SDKMAN_CONFIG"; then
    sed -i.bak 's/sdkman_auto_env=false/sdkman_auto_env=true/' "$SDKMAN_CONFIG"
    info "Enabled sdkman_auto_env — JDK will switch automatically on cd"
  fi

  # Activate for current session
  sdk env install 2>/dev/null || true
  sdk env 2>/dev/null || true
  success "Java $(java -version 2>&1 | awk -F '"' '/version/{print $2}') active"
}

# ─── Node.js & npm ────────────────────────────────────────────────────────────
install_node() {
  if command -v node &>/dev/null; then
    NODE_VER=$(node -v | cut -c2- | cut -d. -f1)
    if [ "$NODE_VER" -ge 18 ] 2>/dev/null; then
      success "Node.js v$(node -v) already installed"; return
    fi
    warn "Node $(node -v) found but need v18+, upgrading..."
  else
    info "Node.js not found, installing..."
  fi

  case $OS in
    mac)   brew install node ;;
    linux)
      curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - 2>/dev/null || true
      case $DISTRO in
        debian) sudo apt-get install -y nodejs ;;
        rhel)   sudo dnf install -y nodejs npm ;;
      esac ;;
    windows)
      warn "On Windows: download Node.js from https://nodejs.org/ (LTS)"
      return ;;
  esac
  success "Node.js $(node -v) and npm $(npm -v) installed"
}

# ─── Docker ───────────────────────────────────────────────────────────────────
check_docker() {
  # On macOS, Docker Desktop puts its CLI in a fixed location that may not be
  # on PATH until the app has been launched at least once.
  if [ "$OS" = "mac" ] && ! command -v docker &>/dev/null; then
    local docker_bin="/Applications/Docker.app/Contents/Resources/bin/docker"
    if [ -x "$docker_bin" ]; then
      export PATH="$(dirname "$docker_bin"):$PATH"
      info "Added Docker Desktop bin to PATH for this session"
    fi
  fi

  if ! command -v docker &>/dev/null; then
    case $OS in
      mac)
        if [ -d "/Applications/Docker.app" ]; then
          warn "Docker Desktop is installed but its CLI is not yet available."
          warn "Please open Docker Desktop from Applications and wait until the whale icon appears in the menu bar, then re-run ./setup.sh"
          exit 1
        fi
        info "Docker Desktop not found — installing via Homebrew..."
        brew install --cask docker
        warn "Open Docker Desktop from Applications, wait until it is running, then re-run ./setup.sh"
        exit 1
        ;;
      linux)
        case $DISTRO in
          debian)
            info "Installing Docker..."
            sudo apt-get install -y docker.io docker-compose-plugin
            sudo systemctl enable --now docker
            sudo usermod -aG docker "$USER" 2>/dev/null || true
            warn "Log out and back in (or run: newgrp docker), then re-run ./setup.sh"
            ;;
          rhel)
            info "Installing Docker..."
            sudo dnf install -y docker docker-compose-plugin
            sudo systemctl enable --now docker
            sudo usermod -aG docker "$USER" 2>/dev/null || true
            warn "Log out and back in (or run: newgrp docker), then re-run ./setup.sh"
            ;;
        esac
        if ! command -v docker &>/dev/null; then
          error "Docker install failed. Install manually: https://docs.docker.com/engine/install/"
        fi
        ;;
      *)
        error "Docker is required for PostgreSQL. Install from https://www.docker.com/products/docker-desktop/"
        ;;
    esac
  fi

  if ! docker info &>/dev/null 2>&1; then
    error "Docker is installed but not running. Start Docker Desktop and re-run ./setup.sh"
  fi

  success "Docker is available"
}

docker_compose() {
  if docker compose version &>/dev/null 2>&1; then
    docker compose "$@"
  elif command -v docker-compose &>/dev/null; then
    docker-compose "$@"
  else
    error "Docker Compose not found. Install docker-compose-plugin or Docker Desktop."
  fi
}

# ─── PostgreSQL (Docker) ──────────────────────────────────────────────────────
start_postgres_docker() {
  check_docker

  local compose_file="$PROJECT_DIR/docker-compose.yml"
  if [ ! -f "$compose_file" ]; then
    error "docker-compose.yml not found at $compose_file"
  fi

  # Force-remove any existing container with this name (running or stopped)
  # so docker compose can always start fresh without a name conflict.
  if docker ps -a --format '{{.Names}}' | grep -q '^weather-postgres$'; then
    info "Removing existing weather-postgres container..."
    docker rm -f weather-postgres
  fi

  info "Starting PostgreSQL via Docker Compose..."
  cd "$PROJECT_DIR"
  docker_compose up -d postgres

  info "Waiting for PostgreSQL to be ready..."
  local max_attempts=30
  local attempt=0
  while [ "$attempt" -lt "$max_attempts" ]; do
    if docker exec weather-postgres pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" &>/dev/null 2>&1; then
      success "PostgreSQL ready — database '$POSTGRES_DB' on localhost:5432 (user: $POSTGRES_USER / pass: $POSTGRES_PASS)"
      return
    fi
    attempt=$((attempt + 1))
    sleep 2
  done

  error "PostgreSQL did not become ready in time. Check logs: docker compose logs postgres"
}

# ─── Maven wrapper ────────────────────────────────────────────────────────────
check_mvnw() {
  if [ -f "$PROJECT_DIR/mvnw" ]; then
    chmod +x "$PROJECT_DIR/mvnw"
    success "Maven wrapper (mvnw) is ready"
  else
    warn "mvnw not found — re-clone the repo or install Maven manually"
  fi
}

# ─── Frontend — isolated node_modules ─────────────────────────────────────────
install_frontend_deps() {
  local frontend_dir="$PROJECT_DIR/frontend"
  if [ -d "$frontend_dir" ]; then
    info "Installing frontend deps into frontend/node_modules/ (project-local)..."
    cd "$frontend_dir"
    npm install
    cd "$PROJECT_DIR"
    success "Frontend deps installed in frontend/node_modules/"
  else
    warn "No frontend/ directory found, skipping npm install"
  fi
}

# ─── Windows guide ────────────────────────────────────────────────────────────
windows_guide() {
  echo ""
  echo -e "${CYAN}════════════════════════════════════════════════════════${NC}"
  echo -e "${CYAN} Windows Setup — Recommended: WSL2 (run this script as-is)${NC}"
  echo -e "${CYAN}════════════════════════════════════════════════════════${NC}"
  echo ""
  echo -e "${GREEN} OPTION 1 — WSL2 (easiest, runs this script unchanged)${NC}"
  echo " ─────────────────────────────────────────────────────"
  echo " Step 1: Open PowerShell as Administrator and run:"
  echo "           wsl --install"
  echo "         This installs WSL2 + Ubuntu. Reboot when prompted."
  echo ""
  echo " Step 2: Open the Ubuntu app from the Start Menu."
  echo "         Set a username and password when asked."
  echo ""
  echo " Step 3: Clone or copy the project into WSL, then run:"
  echo "           chmod +x setup.sh"
  echo "           ./setup.sh"
  echo "         The script will handle everything automatically."
  echo ""
  echo " Step 4: Access the app at http://localhost:8080 (works from Windows browser)"
  echo ""
  echo -e "${YELLOW} OPTION 2 — Manual install (no WSL)${NC}"
  echo " ─────────────────────────────────────────────────────"
  echo " 1. Java 21 JDK   → https://adoptium.net/"
  echo "    After install, set JAVA_HOME in System Environment Variables"
  echo ""
  echo " 2. Node.js 20    → https://nodejs.org/ (choose LTS)"
  echo ""
  echo " 3. Docker Desktop → https://www.docker.com/products/docker-desktop/"
  echo "    Start Docker Desktop, then from the project root run:"
  echo "      docker compose up -d"
  echo "    PostgreSQL runs in a container (weatherdb / weather / weather on port 5432)"
  echo ""
  echo " 4. SDKMAN (optional, Java version manager):"
  echo "    Use WSL or install manually from https://sdkman.io"
  echo ""
  echo " 5. Open Git Bash in the project root and run:"
  echo "      chmod +x mvnw"
  echo "      cd frontend && npm install && cd .."
  echo "      ./mvnw quarkus:dev          # terminal 1 — backend"
  echo "      cd frontend && npm run dev  # terminal 2 — frontend"
  echo -e "${CYAN}════════════════════════════════════════════════════════${NC}"
}

# ─── Summary ─────────────────────────────────────────────────────────────────
print_summary() {
  local profile; profile="$(get_shell_profile)"
  echo ""
  echo -e "${GREEN}════════════════════════════════════════════════════════${NC}"
  echo -e "${GREEN} Setup Complete!${NC}"
  echo -e "${GREEN}════════════════════════════════════════════════════════${NC}"
  echo ""
  echo " Java isolation : SDKMAN (~/.sdkman)  +  .sdkmanrc pins JDK $JAVA_VERSION"
  echo " Node isolation : frontend/node_modules/ (project-local)"
  echo " PostgreSQL     : Docker container weather-postgres (localhost:5432)"
  echo ""
  echo " Database commands:"
  echo "   docker compose up -d         # start PostgreSQL"
  echo "   docker compose down          # stop PostgreSQL"
  echo "   docker compose logs postgres # view logs"
  echo ""
  echo " Activate Java env in this project:"
  echo "   sdk env"
  echo ""
  echo " Run the project (two terminals):"
  echo "   Terminal 1 → ./mvnw quarkus:dev          (http://localhost:8080)"
  echo "   Terminal 2 → cd frontend && npm run dev  (http://localhost:5173)"
  echo ""
  echo " Quarkus Dev UI → http://localhost:8080/q/dev"
  echo ""
  echo -e "${YELLOW} Reload your shell to pick up SDKMAN:${NC}"
  echo "   source $profile"
  echo ""
  echo -e "${YELLOW} If 'sdk' is still not found after reloading, run:${NC}"
  echo "   curl -s \"https://get.sdkman.io\" | bash"
  echo "   source $profile"
  echo "   sdk install java $JAVA_VERSION"
  echo "   sdk env"
  echo -e "${GREEN}════════════════════════════════════════════════════════${NC}"
}

# ─── Main ────────────────────────────────────────────────────────────────────
main() {
  echo -e "${CYAN}"
  echo "╔══════════════════════════════════════════════╗"
  echo "║   Weather Aggregator — Environment Setup     ║"
  echo "╚══════════════════════════════════════════════╝"
  echo -e "${NC}"

  detect_os

  if [ "$OS" = "windows" ]; then
    windows_guide
    exit 0
  fi

  [ "$OS" = "linux" ] && [ "$DISTRO" = "debian" ] && sudo apt-get update -qq

  install_homebrew
  install_sdkman
  install_java
  install_node
  start_postgres_docker
  check_mvnw
  install_frontend_deps
  print_summary
}

main "$@"
