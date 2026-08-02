import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/roles': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/job-offers': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/job-applications': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/quotations': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/company-profiles': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/customer-profiles': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/professional-profiles': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/reviews': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/notifications': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/api/catalogo': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
    },
  },
})
