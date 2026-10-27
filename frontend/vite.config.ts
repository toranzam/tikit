import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// Vite 개발 서버 설정
export default defineConfig({
  // 기존 React 및 Tailwind 플러그인 유지
  plugins: [react(), tailwindcss()],

  // 프론트엔드에서 백엔드 API를 호출하기 위한 프록시 설정
  server: {
    proxy: {
      // /api로 시작하는 요청을 백엔드 서버로 전달
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})