// frontend/tests/e2e.spec.js
import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost';

test.describe('End-to-End Test for Shipment CRUD (LO8)', () => {

    test('should allow creating a new shipment and viewing it', async ({ page }) => {

        // 1. KREIRANJE NOVE POŠILJKE

        // Posjeti Početnu stranicu i čekaj da se aplikacija stabilizira (networkidle)
        await page.goto(`${BASE_URL}`, { waitUntil: 'networkidle' });

        // Klikni na 'Pošiljke' koristeći getByRole ('link') - robustniji lokator
        await page.getByRole('link', { name: 'Pošiljke' }).click();

        // Osiguravamo da je vidljiv gumb 'Dodaj Novo'
        await expect(page.getByRole('button', { name: 'Dodaj Novo' })).toBeVisible({ timeout: 15000 }); // Povećan timeout za svaki slučaj

        // Klikni na 'Dodaj Novo'
        await page.getByRole('button', { name: 'Dodaj Novo' }).click();

        // 2. UNOS PODATAKA

        const randomID = Date.now();
        const origin = `Test Origin ${randomID}`;
        const destination = `Test Destination ${randomID}`;

        // Koristimo ID-ove polja (uvjerite se da su id="originAddress" itd. točni)
        await page.fill('input[id="originAddress"]', origin);
        await page.fill('input[id="destinationAddress"]', destination);
        await page.fill('input[id="weight"]', '100');

        // Klikni na 'Spremi'
        await page.getByRole('button', { name: 'Spremi' }).click();

        // 3. PROVJERA

        // Pričekaj da se pojavi lista pošiljaka nakon spremanja
        await page.waitForURL(`${BASE_URL}/shipments`);

        // Provjeri je li nova pošiljka vidljiva na listi koristeći getByText
        await expect(page.getByText(origin)).toBeVisible();
        await expect(page.getByText(destination)).toBeVisible();

        // Dodatna provjera: Klik na 'Pregled' (View) i provjera detalja
        // Traži redak u tablici koji sadrži 'origin', a zatim klikne na 'Pregled' gumb unutar tog retka
        await page.getByRole('row', { name: origin }).getByRole('button', { name: 'Pregled' }).click();

        // Provjeri da su adrese vidljive na stranici s detaljima
        await expect(page.getByText(origin)).toBeVisible();
        await expect(page.getByText(destination)).toBeVisible();
    });
});