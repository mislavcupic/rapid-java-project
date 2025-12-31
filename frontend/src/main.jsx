import { StrictMode, Suspense } from 'react'
import { createRoot } from 'react-dom/client'
// 🌟 KRITIČNO: UVEZI BrowserRouter
import { BrowserRouter } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css'
import App from './App.jsx'
// 🌟 UVEZI i18n
import './i18n/i18n';

createRoot(document.getElementById('root')).render(
    <StrictMode>
        {/* Suspense mora biti unutar StrictMode ako koristite async */}
        <Suspense fallback={
            <div className="d-flex justify-content-center align-items-center vh-100">
                <span className="text-info fs-3">Loading...</span>
            </div>
        }>
            <BrowserRouter>
                <App />
            </BrowserRouter>
        </Suspense>
    </StrictMode>,
)