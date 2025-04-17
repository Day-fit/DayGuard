export default {
    define: {
        global: 'globalThis'
    },
    optimizeDeps: {
        include: ['@stomp/stompjs']
    },
    build: {
        rollupOptions: {
            external: [],
        },
        commonjsOptions: {
            include: [/node_modules/]
        }
    }
}
