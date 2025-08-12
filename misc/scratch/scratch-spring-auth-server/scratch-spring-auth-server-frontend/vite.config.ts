import react from "@vitejs/plugin-react";
import { resolve } from "node:path";
import { defineConfig } from "vite";


// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      input: {
        "index": resolve(import.meta.dirname, "index.html"),
        "login": resolve(import.meta.dirname, "login.html"),
      },
    },
  },
  /*
  server: {
    proxy: {
      "/api": "http://localhost:9000",
    },
  },
  */
});
