#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "==> Backend (Maven)"
./mvnw test

echo "==> Frontend (Vitest)"
cd frontend
npm test
cd "$ROOT"

echo "==> All tests passed"
