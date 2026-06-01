import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  // Proxy only active in local dev (npm run dev).
  // In production (Vercel) VITE_API_URL points directly at the Railway backend.
  server: {
    proxy: {
      '/weather': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})