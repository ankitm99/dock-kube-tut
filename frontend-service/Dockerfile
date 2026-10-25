# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies first (leverages Docker layer cache)
COPY package*.json ./
RUN npm install

# Copy source and build
COPY . .

# VITE_API_BASE_URL is baked in at build time.
# For local Docker: pass --build-arg VITE_API_BASE_URL=http://localhost:8080
# For prod K8s:     pass --build-arg VITE_API_BASE_URL=https://<your-alb-dns>
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ── Stage 2: Serve ───────────────────────────────────────────────────────────
FROM nginx:alpine

# Remove default nginx static content
RUN rm -rf /usr/share/nginx/html/*

# Copy built React app from Stage 1
COPY --from=build /app/dist /usr/share/nginx/html

# Custom nginx config (handles client-side routing + proxies /api)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
