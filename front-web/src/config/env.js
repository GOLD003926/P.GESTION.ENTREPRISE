// Seul endroit qui lit les variables d'environnement (.env). Le reste du code importe d'ici.
export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
