// Base URL comes from environment variable injected at Docker build time.
// Local dev: Vite proxy rewrites /api → http://localhost:8080
// Docker / K8s: VITE_API_BASE_URL is set to the API Gateway or Ingress host
const BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'

export const API = {
  products: () => fetch(`${BASE}/products`),
  createProduct: (body) =>
    fetch(`${BASE}/products`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }),
  orders: () => fetch(`${BASE}/orders`),
  createOrder: (body) =>
    fetch(`${BASE}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }),
}
