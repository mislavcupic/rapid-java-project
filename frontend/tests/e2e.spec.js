// frontend/tests/e2e.spec.js
import { test, expect } from '@playwright/test';

// Koristimo opseg 'test' koji je definiran u ci-pipeline.yml
// Aplikacija se pokreće na http://localhost, kako je definirano u docker-compose.yml
const BASE_URL = 'http://localhost';

test.describe('End-to-End Test for Shipment CRUD (LO8)', () => {

    test('should allow creating a new shipment and viewing it', async ({ page }) => {

        // 1. KREIRANJE NOVE POŠILJKE

        // Posjeti Početnu stranicu
        await page.goto(`${BASE_URL}`);

        // Klikni na 'Pošiljke' u navigaciji (pretpostavljamo da imate navigaciju)
        await page.click('text=Pošiljke');

        // Osiguravamo da je aplikacija učitana i da je vidljiv gumb 'Dodaj Novo'
        await expect(page.locator('text=Dodaj Novo')).toBeVisible({ timeout: 10000 });

        // Klikni na 'Dodaj Novo'
        await page.click('text=Dodaj Novo');

        // 2. UNOS PODATAKA

        // Popunjavanje forme (pretpostavljamo da su input polja označena etiketama ili placeholderima)
        const randomID = Date.now(); // Koristimo timestamp za jedinstveni ID pošiljke
        const origin = `Test Origin ${randomID}`;
        const destination = `Test Destination ${randomID}`;

        await page.fill('input[id="originAddress"]', origin);
        await page.fill('input[id="destinationAddress"]', destination);

        // Morate popuniti i ostala obavezna polja ako ih imate (npr. težina)
        await page.fill('input[id="weight"]', '100');

        // Klikni na 'Spremi'
        await page.click('button:has-text("Spremi")');

        // 3. PROVJERA

        // Pričekaj da se pojavi lista pošiljaka nakon spremanja
        await page.waitForURL(`${BASE_URL}/shipments`);

        // Provjeri je li nova pošiljka vidljiva na listi
        await expect(page.locator(`text=${origin}`)).toBeVisible();
        await expect(page.locator(`text=${destination}`)).toBeVisible();

        // Dodatna provjera: Klik na 'Pregled' (View) i provjera detalja
        await page.locator(`text=${origin}`).locator('..').locator('button:has-text("Pregled")').click();

        // Provjeri da su adrese vidljive na stranici s detaljima
        await expect(page.locator(`text=${origin}`)).toBeVisible();
        await expect(page.locator(`text=${destination}`)).toBeVisible();
    });
});