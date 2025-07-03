import tailwindcss from '@tailwindcss/vite'

export default {
    base: "/",

    define: {
        global: 'globalThis'
    },
    optimizeDeps: {
        include: ['@stomp/stompjs']
    },
    build: {
        outDir: "dist",

        rollupOptions: {
            external: [],
        },
        commonjsOptions: {
            include: [/node_modules/]
        }
    },

    server: {
      proxy: {
          '/api': {
              target: 'http://localhost:8080',
              changeOrigin: true,
              secure: false,
          },

          '/ws': {
              target: 'http://localhost:8080',
              ws: true,
              changeOrigin: true,
          },
      },
    },

    plugins: tailwindcss()
}
