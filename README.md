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

### Deploying with Raw Kubernetes Manifests

The checked-in raw manifests target account `707360336863`, region `us-east-1`, cluster `dev-cluster`, and separate ECR repositories for each service. The learning cluster config uses two small On-Demand nodes in public subnets with no NAT Gateway to reduce network costs; this is not the recommended production network layout, and EKS, EC2, ALB, ECR storage, and RDS can still incur charges.

   To create the cluster from scratch:
   ```bash
   eksctl create cluster --config-file=cluster.yaml
   aws eks update-kubeconfig --region us-east-1 --name dev-cluster
   kubectl get nodes
   ```

   This config enables OIDC and explicitly installs the `vpc-cni`, `kube-proxy`, and `coredns` add-ons. After the cluster is created, retrieve its current OIDC issuer before creating/updating `trust-policy.json` for the AWS Load Balancer Controller; the issuer ID changes when a cluster is recreated.

   Connect to the cluster and push the images using the tags referenced by the manifests:
   ```bash
   REGISTRY=707360336863.dkr.ecr.us-east-1.amazonaws.com
   aws ecr get-login-password --region us-east-1 \
     | docker login --username AWS --password-stdin "$REGISTRY"

   docker build --platform linux/amd64 -t "$REGISTRY/frontend-service:frontend-v2" ./frontend-service
   docker build --platform linux/amd64 -t "$REGISTRY/order-service:order-v1" ./order-service
   docker build --platform linux/amd64 -t "$REGISTRY/product-service:product-v1" ./product-service

   docker push "$REGISTRY/frontend-service:frontend-v2"
   docker push "$REGISTRY/order-service:order-v1"
   docker push "$REGISTRY/product-service:product-v1"
   ```

   Before applying the backend manifests, create the RDS PostgreSQL instance and the `productdb` and `orderdb` databases (see `init-db/init.sql`). Update the datasource endpoint in both backend manifests to match the instance, and allow inbound PostgreSQL traffic from the EKS nodes in the database security group. The credentials in the manifests are for learning only.

   For public ALB routing, `iam.withOIDC: true` creates the cluster OIDC provider. After each cluster recreation, read the current issuer and update `trust-policy.json` and the AWS Load Balancer Controller IAM role to match it. The controller manifest uses cert-manager to provision its webhook certificate:
   ```bash
   kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.21.3/cert-manager.yaml
   curl -o /tmp/lbc-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.14.1/docs/install/iam_policy.json
   aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy --policy-document file:///tmp/lbc-policy.json
   aws iam create-role --role-name AmazonEKSLoadBalancerControllerRole --assume-role-policy-document file://trust-policy.json
   aws iam attach-role-policy --role-name AmazonEKSLoadBalancerControllerRole --policy-arn arn:aws:iam::707360336863:policy/AWSLoadBalancerControllerIAMPolicy
   kubectl apply -f lbc-v2_14_1_full.yaml
   ```
   Wait until cert-manager and the AWS Load Balancer Controller pods are ready before creating the Ingress.

   The frontend image uses same-origin `/products` and `/orders` paths through the ALB. After frontend source or Nginx config changes, push a new tag and update `k8s/frontend-service.yaml` so nodes do not reuse an old image. Then apply:
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
│   ├── frontend-service.yaml
│   ├── order-service.yaml
│   ├── product-service.yaml
│   └── ingress.yaml        # AWS ALB Ingress (Production)
├── docker-compose.yml      # Local multi-container development
└── README.md
```
