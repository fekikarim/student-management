#!/bin/bash

# Script de déploiement Kubernetes pour l'application Student Management
# Usage: ./deploy-k8s.sh [apply|delete|status]

set -e

NAMESPACE="default"
K8S_DIR="k8s"

# Couleurs pour l'output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

function print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

function print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

function print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

function check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl n'est pas installé"
        exit 1
    fi
    print_info "kubectl est installé"
}

function check_k8s_connection() {
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Impossible de se connecter au cluster Kubernetes"
        exit 1
    fi
    print_info "Connexion au cluster Kubernetes OK"
}

function deploy_mysql() {
    print_info "Déploiement de MySQL..."
    
    kubectl apply -f ${K8S_DIR}/mysql-configmap.yaml
    kubectl apply -f ${K8S_DIR}/mysql-secret.yaml
    kubectl apply -f ${K8S_DIR}/mysql-pv.yaml
    kubectl apply -f ${K8S_DIR}/mysql-pvc.yaml
    kubectl apply -f ${K8S_DIR}/mysql-deployment.yaml
    kubectl apply -f ${K8S_DIR}/mysql-service.yaml
    
    print_info "Attente du démarrage de MySQL..."
    kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s || true
}

function deploy_app() {
    print_info "Déploiement de l'application Student Management..."
    
    kubectl apply -f ${K8S_DIR}/app-configmap.yaml
    kubectl apply -f ${K8S_DIR}/app-secret.yaml
    kubectl apply -f ${K8S_DIR}/app-deployment.yaml
    kubectl apply -f ${K8S_DIR}/app-service.yaml
    
    print_info "Attente du démarrage de l'application..."
    kubectl wait --for=condition=ready pod -l app=student-management --timeout=180s || true
}

function delete_all() {
    print_warning "Suppression de tous les déploiements..."
    
    kubectl delete -f ${K8S_DIR}/app-service.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/app-deployment.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/app-secret.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/app-configmap.yaml --ignore-not-found=true
    
    kubectl delete -f ${K8S_DIR}/mysql-service.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/mysql-deployment.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/mysql-pvc.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/mysql-pv.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/mysql-secret.yaml --ignore-not-found=true
    kubectl delete -f ${K8S_DIR}/mysql-configmap.yaml --ignore-not-found=true
    
    print_info "Suppression terminée"
}

function show_status() {
    print_info "=== Status des Pods ==="
    kubectl get pods -l app=mysql
    kubectl get pods -l app=student-management
    
    print_info "\n=== Status des Services ==="
    kubectl get svc mysql-service
    kubectl get svc student-management-service
    
    print_info "\n=== Status des PVC ==="
    kubectl get pvc mysql-pvc
    
    print_info "\n=== Status des Deployments ==="
    kubectl get deployment mysql
    kubectl get deployment student-management
    
    print_info "\n=== URL d'accès ==="
    if command -v minikube &> /dev/null; then
        MINIKUBE_IP=$(minikube ip)
        echo "Application accessible à: http://${MINIKUBE_IP}:30089/student"
    else
        NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
        echo "Application accessible à: http://${NODE_IP}:30089/student"
    fi
}

function show_logs() {
    print_info "=== Logs MySQL ==="
    kubectl logs -l app=mysql --tail=50
    
    print_info "\n=== Logs Application ==="
    kubectl logs -l app=student-management --tail=50
}

# Main
check_kubectl
check_k8s_connection

case "${1:-apply}" in
    apply)
        print_info "Déploiement de l'application sur Kubernetes..."
        deploy_mysql
        sleep 10
        deploy_app
        print_info "\n✅ Déploiement terminé avec succès!"
        show_status
        ;;
    delete)
        delete_all
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    *)
        echo "Usage: $0 {apply|delete|status|logs}"
        exit 1
        ;;
esac
