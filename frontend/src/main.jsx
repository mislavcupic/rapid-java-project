// frontend/src/main.jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// *** KRITIČNO: UVEZI BOOTSTRAP CSS ***
import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css' // Ostavljamo, ali će biti prazan ili za custom stilove
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <App />
    </StrictMode>,
)