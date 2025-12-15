# Commandes Rapides - Copier-Coller

## 🚀 Démarrage rapide

### 1. Connexion à Vagrant et Minikube
```bash
# Sur votre Mac
cd ~/Vagrant/ubunto
vagrant up
vagrant ssh

# Dans Vagrant
minikube start
cd ~/workspace/k8s-atelier
```

### 2. Récupérer le projet
```bash
# Option 1: Clone depuis GitHub
git clone https://github.com/fekikarim/student-management.git
cd student-management

# Option 2: Si synchronisé avec Vagrant
cd /vagrant
```

### 3. Préparer le répertoire MySQL
```bash
minikube ssh "sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"
```

### 4. Déployer sur Kubernetes
```bash
# Avec le script
chmod +x deploy-k8s.sh
./deploy-k8s.sh apply

# OU manuellement
kubectl apply -f k8s/mysql-configmap.yaml
kubectl apply -f k8s/mysql-secret.yaml
kubectl apply -f k8s/mysql-pv.yaml
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/mysql-service.yaml

# Attendre MySQL
kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s

# Déployer l'application
kubectl apply -f k8s/app-configmap.yaml
kubectl apply -f k8s/app-secret.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml

# Attendre l'application
kubectl wait --for=condition=ready pod -l app=student-management --timeout=180s
```

### 5. Vérifier le déploiement
```bash
kubectl get all
kubectl get pods
kubectl get svc
kubectl get pv,pvc
```

### 6. Tester l'application
```bash
# Obtenir l'IP
export MINIKUBE_IP=$(minikube ip)
echo "URL: http://$MINIKUBE_IP:30089/student"

# Health check
curl http://$MINIKUBE_IP:30089/student/actuator/health

# Métriques
curl http://$MINIKUBE_IP:30089/student/actuator/prometheus
```

---

## 📸 Commandes pour les captures d'écran

### Capture 1: Vue d'ensemble des ressources
```bash
kubectl get all
```

### Capture 2: Pods détaillés
```bash
kubectl get pods -o wide
```

### Capture 3: Services exposés
```bash
kubectl get svc
```

### Capture 4: Stockage (PV et PVC)
```bash
kubectl get pv,pvc
```

### Capture 5: ConfigMaps et Secrets
```bash
kubectl get configmap,secret
```

### Capture 6: Description du Deployment
```bash
kubectl describe deployment student-management
```

### Capture 7: Description du Service
```bash
kubectl describe svc student-management-service
```

### Capture 8: Logs de l'application
```bash
kubectl logs -l app=student-management --tail=50
```

### Capture 9: Logs MySQL
```bash
kubectl logs -l app=mysql --tail=30
```

### Capture 10: Health check fonctionnel
```bash
curl -i http://$(minikube ip):30089/student/actuator/health
```

### Capture 11: Événements du cluster
```bash
kubectl get events --sort-by=.metadata.creationTimestamp
```

### Capture 12: Rolling update en action
```bash
kubectl rollout status deployment/student-management
kubectl rollout history deployment/student-management
```

---

## 🔍 Commandes de debug

### Vérifier un pod qui ne démarre pas
```bash
kubectl get pods
kubectl describe pod <nom-du-pod>
kubectl logs <nom-du-pod>
```

### Vérifier la connectivité MySQL
```bash
# Depuis un pod de l'application
kubectl exec -it <nom-pod-app> -- nc -zv mysql-service 3306

# Tester directement
kubectl run mysql-client --rm -it --image=mysql:8.0 -- mysql -h mysql-service -u studentapp -pstudentpass student_management
```

### Vérifier les variables d'environnement
```bash
kubectl exec -it <nom-pod-app> -- env | grep SPRING
```

### Vérifier les endpoints
```bash
kubectl get endpoints
kubectl describe endpoints student-management-service
```

---

## 🔄 Commandes de mise à jour

### Mettre à jour l'image de l'application
```bash
# Build et push nouvelle image
docker build -t fekikarim/student-management:v2 .
docker push fekikarim/student-management:v2

# Mettre à jour le deployment
kubectl set image deployment/student-management student-management=fekikarim/student-management:v2 --record

# Suivre le rolling update
kubectl rollout status deployment/student-management

# En cas de problème, rollback
kubectl rollout undo deployment/student-management
```

### Scaler l'application
```bash
# Augmenter à 3 replicas
kubectl scale deployment/student-management --replicas=3

# Vérifier
kubectl get pods -l app=student-management
```

---

## 📊 Commandes de monitoring

### Surveiller en temps réel
```bash
# Pods
kubectl get pods -w

# Logs en temps réel
kubectl logs -f -l app=student-management

# Top (ressources)
kubectl top pods
kubectl top nodes
```

### Accéder aux métriques Prometheus
```bash
curl http://$(minikube ip):30089/student/actuator/prometheus | head -50
```

---

## 🧹 Commandes de nettoyage

### Supprimer tout
```bash
# Avec le script
./deploy-k8s.sh delete

# Ou manuellement
kubectl delete -f k8s/app-service.yaml
kubectl delete -f k8s/app-deployment.yaml
kubectl delete -f k8s/app-secret.yaml
kubectl delete -f k8s/app-configmap.yaml
kubectl delete -f k8s/mysql-service.yaml
kubectl delete -f k8s/mysql-deployment.yaml
kubectl delete -f k8s/mysql-pvc.yaml
kubectl delete -f k8s/mysql-pv.yaml
kubectl delete -f k8s/mysql-secret.yaml
kubectl delete -f k8s/mysql-configmap.yaml
```

### Nettoyer complètement
```bash
# Supprimer tous les déploiements
kubectl delete deployment --all

# Supprimer tous les services (sauf kubernetes)
kubectl delete svc --all --field-selector metadata.name!=kubernetes

# Supprimer tous les pods
kubectl delete pods --all

# Supprimer tous les PVC
kubectl delete pvc --all
```

---

## 🎯 Tests fonctionnels complets

### Test complet de l'application
```bash
#!/bin/bash

MINIKUBE_IP=$(minikube ip)
BASE_URL="http://$MINIKUBE_IP:30089/student"

echo "=== Test 1: Health Check ==="
curl -s $BASE_URL/actuator/health | jq

echo -e "\n=== Test 2: Application Info ==="
curl -s $BASE_URL/actuator/info | jq

echo -e "\n=== Test 3: Prometheus Metrics (premiers 20 lignes) ==="
curl -s $BASE_URL/actuator/prometheus | head -20

echo -e "\n=== Test 4: Vérification des pods ==="
kubectl get pods

echo -e "\n=== Test 5: Vérification des services ==="
kubectl get svc

echo -e "\n=== Test 6: Logs récents de l'application ==="
kubectl logs -l app=student-management --tail=10

echo -e "\n✅ Tests terminés!"
```

### Copier ce script et l'exécuter
```bash
# Créer le script
cat > test-app.sh << 'EOF'
#!/bin/bash
MINIKUBE_IP=$(minikube ip)
BASE_URL="http://$MINIKUBE_IP:30089/student"
echo "=== Test Health Check ==="
curl -s $BASE_URL/actuator/health
echo -e "\n=== Pods status ==="
kubectl get pods -l app=student-management
echo -e "\n✅ Tests OK!"
EOF

# Exécuter
chmod +x test-app.sh
./test-app.sh
```

---

## 📝 Commandes pour Jenkins

Si vous configurez Jenkins pour déployer automatiquement:

### Configurer kubectl dans Jenkins
```bash
# Dans Jenkins, installer kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Copier la config de Minikube
mkdir -p ~/.kube
minikube kubectl -- config view --raw > ~/.kube/config
```

### Tester la connexion
```bash
kubectl cluster-info
kubectl get nodes
```

---

## 🆘 Résolution de problèmes courants

### Pod en CrashLoopBackOff
```bash
kubectl logs <pod-name>
kubectl describe pod <pod-name>
kubectl get events --field-selector involvedObject.name=<pod-name>
```

### Image ne se télécharge pas
```bash
# Vérifier l'image
docker pull fekikarim/student-management:latest

# Ou charger dans Minikube
minikube image load fekikarim/student-management:latest
minikube image ls | grep student
```

### Service non accessible
```bash
# Vérifier le service
kubectl get svc student-management-service
kubectl get endpoints student-management-service

# Test depuis un pod temporaire
kubectl run test --rm -it --image=busybox -- wget -O- http://student-management-service:8089/student/actuator/health
```

### MySQL ne démarre pas
```bash
# Vérifier les logs
kubectl logs -l app=mysql

# Vérifier le PVC
kubectl describe pvc mysql-pvc

# Recréer le répertoire
minikube ssh "sudo rm -rf /mnt/data/mysql && sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"

# Redémarrer MySQL
kubectl delete pod -l app=mysql
```

---

## 💡 Astuces supplémentaires

### Alias utiles
```bash
# Ajouter dans ~/.bashrc ou ~/.zshrc
alias k='kubectl'
alias kgp='kubectl get pods'
alias kgs='kubectl get svc'
alias kgd='kubectl get deployments'
alias kl='kubectl logs'
alias kd='kubectl describe'
```

### Bash completion pour kubectl
```bash
source <(kubectl completion bash)
# Ou pour zsh
source <(kubectl completion zsh)
```

### Watch en continu
```bash
# Surveiller les pods
watch kubectl get pods

# Surveiller tous les objets
watch kubectl get all
```

---

Copiez et exécutez ces commandes dans l'ordre pour un déploiement réussi! 🚀
