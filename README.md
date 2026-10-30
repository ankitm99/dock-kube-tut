# Microservices with Docker, Kubernetes, PostgreSQL & Helm

A complete, production-ready microservices repository designed for learning **Docker**, **Kubernetes (including AWS EKS)**, **Service Discovery without Eureka**, **PostgreSQL Persistence**, and **Helm Packaging**.

---

## Two-Mode Traffic Architecture

```
1. LOCAL DEVELOPMENT (Docker Compose / Local Run)
   Client ──► API Gateway (:8080) ──┬──► product-service (:8082)
                                    └──► order-service (:8081)

2. PRODUCTION ON AWS EKS (Kubernetes Ingress Controller)
   Client ──► AWS ALB Ingress Controller ──┬──► product-service Pods
                                           └──► order-service Pods
              (Spring Cloud Gateway disabled in prod to save costs & eliminate network hop!)
```

---

## Architecture Summary

| Environment | Routing Entrypoint | Gateway Container | Profile | Database |
|---|---|---|---|---|
| **Local (Mac/Docker)** | **API Gateway** (`port 8080`) | **Enabled** | `local` | Localhost / Docker PostgreSQL (`5432`) |
| **Prod (AWS EKS)** | **Kubernetes Ingress (AWS ALB)** | **Disabled** (0 pods) | `prod` | AWS RDS / Cloud PostgreSQL |

---

## 1. Local Development (`local` Profile)

### Option A: Using Docker Compose
Runs all services and PostgreSQL locally:
```bash
docker compose up --build -d
```
Test via Gateway:
```bash
curl http://localhost:8080/products
curl http://localhost:8080/orders
```

### Option B: Running directly on Mac with Maven
Ensure PostgreSQL is running (`docker compose up -d postgres`), then in 3 separate terminals:

```bash
# Terminal 1: Product Service
cd product-service && mvn spring-boot:run

# Terminal 2: Order Service
cd order-service && mvn spring-boot:run

# Terminal 3: API Gateway
cd api-gateway && mvn spring-boot:run
```

---

## 2. Production Deployment on AWS EKS (`prod` Profile + Ingress)

In production on AWS EKS, we use the **AWS Load Balancer Controller (ALB Ingress)** which routes directly to `product-service` and `order-service`. The Spring Gateway container is automatically disabled to eliminate an unnecessary network hop and save RAM/CPU!

### Deploying with Helm

1. **Connect kubectl to your AWS EKS cluster**:
   ```bash
   aws eks update-kubeconfig --region <region> --name <cluster-name>
   ```

2. **Deploy using `values-prod.yaml`**:
   ```bash
   helm install my-app ./helm/microservices-app -f ./helm/microservices-app/values-prod.yaml
   ```

   This automatically:
   - **Disables `api-gateway`** (`apiGateway.enabled: false`).
   - **Enables `Ingress`** (`ingress.enabled: true`) with AWS ALB annotations.
   - Sets **`SPRING_PROFILES_ACTIVE=prod`**.
   - Scales `product-service` and `order-service` to **3 replicas**.

3. **Verify Ingress & Pods**:
   ```bash
   kubectl get ingress
   kubectl get pods,pvc,svc
   ```
   AWS will provision an **Application Load Balancer (ALB)** with an address like:
   `k8s-default-microser-xxxxxxxxxx.us-east-1.elb.amazonaws.com`

4. **Test AWS ALB Endpoints**:
   ```bash
   # Replace with your AWS ALB DNS name
   ALB_URL="http://k8s-default-microser-xxxxxxxxxx.us-east-1.elb.amazonaws.com"

   curl $ALB_URL/products
   curl -X POST $ALB_URL/orders -H "Content-Type: application/json" -d '{"productId": 1, "quantity": 2}'
   curl $ALB_URL/orders
   ```

5. **Deploying with Raw Kubernetes Manifests**:
   Build and push the frontend image to ECR without setting `VITE_API_BASE_URL`; it will use same-origin `/products` and `/orders` routes through the ALB. After frontend source or Nginx config changes, push a new image tag and update `k8s/frontend-service.yaml` so nodes do not reuse a cached image. Update all three manifests with their ECR image URIs, then apply:
   ```bash
   # Deploy application services and ALB Ingress; the database is AWS RDS
   kubectl apply -f k8s/product-service.yaml
   kubectl apply -f k8s/order-service.yaml
   kubectl apply -f k8s/frontend-service.yaml
   kubectl apply -f k8s/ingress.yaml
   ```

---

## Project Structure

```
dock-kube-tut/
├── api-gateway/            # Port 8080 - Spring Cloud Gateway (used locally)
├── product-service/        # Port 8082 - Product catalog (Spring Data JPA + Postgres)
├── order-service/          # Port 8081 - Orders (Spring Data JPA + Postgres + RestClient)
├── init-db/                # Database initialization scripts (init.sql)
├── helm/microservices-app/ # Helm Chart (Parameterized for local vs prod)
│   ├── values.yaml         # Local dev (API Gateway ON, Ingress OFF)
│   ├── values-prod.yaml    # Production AWS EKS (API Gateway OFF, Ingress ON, 3 replicas)
│   └── templates/          # Kubernetes manifests & Ingress template
├── k8s/                    # Raw Kubernetes manifests
│   ├── ingress.yaml        # AWS ALB Ingress (Production)
│   ├── api-gateway.yaml    # API Gateway (Local K8s testing)
│   ├── order-service.yaml
│   ├── product-service.yaml
│   └── postgres.yaml
├── docker-compose.yml      # Local multi-container development
└── README.md
```
