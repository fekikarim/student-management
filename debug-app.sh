#!/bin/bash

echo "=== Vérification des logs de l'application ==="
kubectl logs -l app=student-management --tail=100 --all-containers=true

echo -e "\n=== Description détaillée d'un pod ==="
POD=$(kubectl get pods -l app=student-management -o jsonpath='{.items[0].metadata.name}')
echo "Pod: $POD"
kubectl describe pod $POD

echo -e "\n=== Events récents ==="
kubectl get events --sort-by=.metadata.creationTimestamp | tail -20

echo -e "\n=== Vérification de la connectivité MySQL ==="
echo "En attente que le pod soit accessible..."
sleep 5
kubectl exec -it $POD -- nc -zv mysql-service 3306 2>&1 || echo "Échec de la connexion à MySQL"

echo -e "\n=== Variables d'environnement ==="
kubectl exec -it $POD -- env | grep SPRING || echo "Variables SPRING non trouvées"
