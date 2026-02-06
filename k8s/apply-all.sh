#!/bin/bash
echo "Applying Kubernetes manifests..."

kubectl apply -f namespace.yaml

kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml

echo "All manifests applied successfully!"

echo "Checking deployment status..."
kubectl get all -n todo-app