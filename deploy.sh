#!/bin/bash

set -e

echo "Starting ToDo application deployment to Kubernetes..."

# Apply namespace
echo "Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# Apply secrets
echo "Creating secrets..."
kubectl apply -f k8s/secret.yaml

# Apply configmaps
echo "Creating configmaps..."
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/liquibase/configmap.yaml

# Deploy PostgreSQL
echo "Deploying PostgreSQL..."
kubectl apply -f k8s/postgres/pvc.yaml
kubectl apply -f k8s/postgres/deployment.yaml
kubectl apply -f k8s/postgres/service.yaml

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n todo-app --timeout=300s

# Deploy Redis
echo "Deploying Redis..."
kubectl apply -f k8s/redis/pvc.yaml
kubectl apply -f k8s/redis/deployment.yaml
kubectl apply -f k8s/redis/service.yaml

# Wait for Redis to be ready
echo "Waiting for Redis to be ready..."
kubectl wait --for=condition=ready pod -l app=redis -n todo-app --timeout=300s

# Run database migrations
echo "Running database migrations..."
kubectl apply -f k8s/liquibase/job.yaml

# Wait for migrations to complete
echo "Waiting for migrations to complete..."
kubectl wait --for=condition=complete job/liquibase-migration -n todo-app --timeout=600s

# Deploy ToDo application
echo "Deploying ToDo application..."
kubectl apply -f k8s/todo-app/deployment.yaml
kubectl apply -f k8s/todo-app/service.yaml
kubectl apply -f k8s/todo-app/hpa.yaml

# Wait for application to be ready
echo "Waiting for application to be ready..."
kubectl wait --for=condition=ready pod -l app=todo-app -n todo-app --timeout=300s

# Deploy ingress
echo "Deploying ingress..."
kubectl apply -f k8s/todo-app/ingress.yaml

# Get application info
echo "Deployment completed!"
echo ""
echo "Application URLs:"
echo "  API: http://todo.local/api"
echo "  Swagger UI: http://todo.local/swagger-ui.html"
echo "  Actuator: http://todo.local/actuator/health"
echo ""
echo "To access the application, add to /etc/hosts:"
echo "  $(minikube ip) todo.local"
echo ""
echo "Pods status:"
kubectl get pods -n todo-app
echo ""
echo "Services:"
kubectl get services -n todo-app
echo ""
echo "Ingress:"
kubectl get ingress -n todo-app