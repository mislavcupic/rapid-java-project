import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import Backend from 'i18next-http-backend';
import LanguageDetector from 'i18next-browser-languagedetector';

i18n
    // 1. Učitavanje prijevoda s putanje (public/locales/{{lng}}/translation.json)
    .use(Backend)
    // 2. Automatsko otkrivanje jezika (cookie, preglednik)
    .use(LanguageDetector)
    // 3. Povezivanje s Reactom
    .use(initReactI18next)
    .init({
        // Glavni jezici i fallback
        supportedLngs: ['en', 'fr', 'hr'], // Podržavamo sva tri
        fallbackLng: 'en',
        debug: false,

        // Konfiguracija i18next-http-backend: gdje se nalaze JSON datoteke
        backend: {
            // Putanja u public mapi: /locales/hr/translation.json
            loadPath: '/locales/{{lng}}/translation.json',
        },

        detection: {
            // Prioritet detekcije jezika
            order: ['cookie', 'localStorage', 'navigator'],
            caches: ['cookie'], // Pohrana trenutnog jezika u cookie
            cookieName: 'i18next_lang', // Naziv cookie-a
        },

        interpolation: {
            escapeValue: false, // Potrebno za React
        }
    });

export default i18n;