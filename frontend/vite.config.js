// frontend/vite.config.js (POJEDNOSTAVLJENO)
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Ukloni import tailwindcssPostcss i autoprefixer OVDJE
// jer ćemo ih koristiti u postcss.config.js

export default defineConfig({
  plugins: [react()],
  // UKLONI CIJELI 'css: { postcss: { ... } }' BLOK OVDJE!
  // css: {
  //   postcss: {
  //     plugins: [...]
  //   }
  // }
});