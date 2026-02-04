// frontend/playwright.config.js
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  reporter: [['html', { outputFolder: 'playwright-report' }]], // Forces output to this folder
  use: {
    baseURL: 'http://localhost:5173',
    viewport: { width: 1280, height: 720 },
  },
});