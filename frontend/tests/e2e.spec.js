import { test, expect } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:5173';

test.describe('End-to-End Test for Shipment CRUD (LO8)', () => {

    test('should allow creating a new shipment and viewing it', async ({ page }) => {

        // 1. KREIRANJE NOVE POŠILJKE
        // Povećan timeout i čeka se da se mreža smiri
        await page.goto(`${BASE_URL}`, {
            waitUntil: 'networkidle',
            timeout: 30000
        });

        await page.click('text=Pošiljke');

        // Povećan timeout za čekanje na gumb 'Dodaj Novo'
        await expect(page.locator('text=Dodaj Novo')).toBeVisible({ timeout: 15000 });

        await page.click('text=Dodaj Novo');

        // 2. UNOS PODATAKA
        const randomID = Date.now();
        const origin = `Test Origin ${randomID}`;
        const destination = `Test Destination ${randomID}`;

        await page.fill('input[id="originAddress"]', origin);
        await page.fill('input[id="destinationAddress"]', destination);
        await page.fill('input[id="weight"]', '100');

        await page.click('button:has-text("Spremi")');

        // 3. PROVJERA
        await page.waitForURL(`${BASE_URL}/shipments`);

        await expect(page.locator(`text=${origin}`)).toBeVisible();
        await expect(page.locator(`text=${destination}`)).toBeVisible();

        // 4. PREGLED DETALJA
        await page.locator(`text=${origin}`).locator('..').locator('button:has-text("Pregled")').click();

        await expect(page.locator('text=Detalji Pošiljke')).toBeVisible();
        await expect(page.locator(`text=${origin}`)).toBeVisible();
        await expect(page.locator(`text=${destination}`)).toBeVisible();

    });
});
