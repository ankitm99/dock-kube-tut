// Use same-origin API routes by default; EKS Ingress routes them to each service.
// Docker Compose can set VITE_API_BASE_URL=/api to use its Nginx/API Gateway proxy.
const BASE = import.meta.env.VITE_API_BASE_URL ?? ''

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
