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

    await page.click('text=Login');
    // 2. Perform Login (Adjust selectors like 'input[name="username"]' to your code)
    await page.fill('input[placeholder*="Username"]', 'dispatcher');
    await page.fill('input[placeholder*="Enter password"]', 'password');
    await page.click('button[type="submit"]');

        // Povećan timeout za čekanje na gumb 'Dodaj Novo'
        await page.click('text = Shipments');
        await expect(page.locator('text=Create Shipment')).toBeVisible({ timeout: 15000 });

        await page.click('text=Create Shipment');

await expect(page.locator('text=forms.create_shipment_title')).toBeVisible({ timeout: 15000 });


        // 2. UNOS PODATAKA
        const randomID = Date.now();
        const origin = "Ilica 2, Zagreb";
        const destination = "Glavna ulica 114, Sesvete";

      await page.locator('label:has-text("Origin Address") + input').fill(origin);

      // Fill Destination
      await page.locator('label:has-text("Destination Address") + input').fill(destination);

      // Coordinatama treba vise od 800ms da se pokazu
      await page.waitForTimeout(2000);

      // Potvrdi da su se markeri pokazali
      await expect(page.locator('.leaflet-marker-icon')).toHaveCount(2, { timeout: 10000 });

        await page.locator('label:has-text("Weight (kg)") + input').fill('100');

        await page.click('button:has-text("general.save_shipment")');

        // 3. PROVJERA
        await page.waitForURL(`${BASE_URL}/shipments`);

        await expect(page.locator(`text=${origin}`).first()).toBeVisible();
        await expect(page.locator(`text=${destination}`).first()).toBeVisible();

        // 4. PREGLED DETALJA
        await page.locator('text=Ilica 2, Zagreb').first().locator('..').locator('button:has-text("Details")').click();

        await expect(page.locator('text=Details').first()).toBeVisible();
        await expect(page.locator(`text=${origin}`)).toBeVisible();
        await expect(page.locator(`text=${destination}`)).toBeVisible();

    });
});
