#!/bin/bash

echo "Tearing down ToDo application..."

# Delete resources in reverse order
kubectl delete -f k8s/todo-app/ingress.yaml --ignore-not-found=true
kubectl delete -f k8s/todo-app/hpa.yaml --ignore-not-found=true
kubectl delete -f k8s/todo-app/service.yaml --ignore-not-found=true
kubectl delete -f k8s/todo-app/deployment.yaml --ignore-not-found=true

kubectl delete -f k8s/liquibase/job.yaml --ignore-not-found=true

kubectl delete -f k8s/redis/service.yaml --ignore-not-found=true
kubectl delete -f k8s/redis/deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/redis/pvc.yaml --ignore-not-found=true

kubectl delete -f k8s/postgres/service.yaml --ignore-not-found=true
kubectl delete -f k8s/postgres/deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/postgres/pvc.yaml --ignore-not-found=true

kubectl delete -f k8s/liquibase/configmap.yaml --ignore-not-found=true
kubectl delete -f k8s/configmap.yaml --ignore-not-found=true
kubectl delete -f k8s/secret.yaml --ignore-not-found=true

# Wait for PVCs to be deleted
kubectl delete pvc -n todo-app --all --ignore-not-found=true

# Delete namespace (this will delete all resources in the namespace)
kubectl delete -f k8s/namespace.yaml --ignore-not-found=true

echo "Teardown completed!"