# Guide de Déploiement - Vagrant + Minikube

## 🚀 Instructions étape par étape

### 1. Préparation de l'environnement Vagrant

```bash
# Sur votre MacBook Pro M4
cd ~/Vagrant/ubunto
vagrant up
vagrant ssh
```

### 2. Vérification de Minikube

```bash
# Dans la VM Vagrant
minikube status

# Si Minikube n'est pas démarré
minikube start

# Vérifier kubectl
kubectl version --client
kubectl cluster-info
```

### 3. Préparation du workspace

```bash
# Créer le répertoire de travail s'il n'existe pas
mkdir -p ~/workspace/k8s-atelier
cd ~/workspace/k8s-atelier

# Cloner votre projet (ou copier les fichiers depuis /vagrant)
# Si votre projet est synchronisé avec Vagrant
ls /vagrant/

# Ou cloner depuis GitHub
git clone https://github.com/fekikarim/student-management.git
cd student-management
```

### 4. Construire l'image Docker localement (optionnel)

Si vous voulez tester avec une image locale avant le push:

```bash
# Build l'image
./mvnw clean package -DskipTests
docker build -t fekikarim/student-management:latest .

# Charger l'image dans Minikube
minikube image load fekikarim/student-management:latest

# Vérifier que l'image est présente
minikube image ls | grep student-management
```

### 5. Déploiement sur Kubernetes

#### Option A: Déploiement rapide avec le script

```bash
# Rendre le script exécutable
chmod +x deploy-k8s.sh

# Déployer tout
./deploy-k8s.sh apply
```

#### Option B: Déploiement manuel étape par étape

```bash
# 1. Créer le répertoire pour MySQL sur le nœud Minikube
minikube ssh "sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"

# 2. Déployer MySQL
echo "📦 Déploiement de MySQL..."
kubectl apply -f k8s/mysql-configmap.yaml
kubectl apply -f k8s/mysql-secret.yaml
kubectl apply -f k8s/mysql-pv.yaml
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/mysql-service.yaml

# 3. Attendre que MySQL soit prêt (peut prendre 1-2 minutes)
echo "⏳ Attente du démarrage de MySQL..."
kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s

# Vérifier que MySQL est bien démarré
kubectl get pods -l app=mysql
kubectl logs -l app=mysql --tail=20

# 4. Déployer l'application Spring Boot
echo "📦 Déploiement de l'application..."
kubectl apply -f k8s/app-configmap.yaml
kubectl apply -f k8s/app-secret.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml

# 5. Attendre que l'application soit prête (peut prendre 2-3 minutes)
echo "⏳ Attente du démarrage de l'application..."
kubectl wait --for=condition=ready pod -l app=student-management --timeout=180s

# Vérifier que l'application est bien démarrée
kubectl get pods -l app=student-management
kubectl logs -l app=student-management --tail=30
```

### 6. Vérification du déploiement

```bash
# Vérifier tous les pods
kubectl get pods

# Résultat attendu:
# NAME                                  READY   STATUS    RESTARTS   AGE
# mysql-xxxxx                          1/1     Running   0          2m
# student-management-xxxxx             1/1     Running   0          1m
# student-management-yyyyy             1/1     Running   0          1m

# Vérifier les services
kubectl get svc

# Résultat attendu:
# NAME                          TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
# kubernetes                    ClusterIP   10.96.0.1        <none>        443/TCP          1h
# mysql-service                 ClusterIP   10.xxx.xxx.xxx   <none>        3306/TCP         2m
# student-management-service    NodePort    10.xxx.xxx.xxx   <none>        8089:30089/TCP   1m

# Vérifier les déploiements
kubectl get deployments
```

### 7. Tester l'application

```bash
# Obtenir l'IP de Minikube
export MINIKUBE_IP=$(minikube ip)
echo "Minikube IP: $MINIKUBE_IP"

# Test 1: Health check
echo "=== Test Health Check ==="
curl http://$MINIKUBE_IP:30089/student/actuator/health

# Résultat attendu: {"status":"UP"}

# Test 2: Info de l'application
echo -e "\n=== Test Application Info ==="
curl http://$MINIKUBE_IP:30089/student/actuator/info

# Test 3: Métriques Prometheus
echo -e "\n=== Test Prometheus Metrics ==="
curl http://$MINIKUBE_IP:30089/student/actuator/prometheus | head -20

# Test 4: API Students (si des endpoints existent)
echo -e "\n=== Test API Students ==="
curl http://$MINIKUBE_IP:30089/student/api/students
```

### 8. Commandes utiles pour le monitoring

```bash
# Voir les logs en temps réel de l'application
kubectl logs -f -l app=student-management

# Voir les logs de MySQL
kubectl logs -f -l app=mysql

# Voir les événements du cluster
kubectl get events --sort-by=.metadata.creationTimestamp

# Obtenir plus de détails sur un pod
kubectl describe pod <nom-du-pod>

# Se connecter à un pod (pour debug)
kubectl exec -it <nom-du-pod> -- /bin/sh

# Tester la connexion à MySQL depuis un pod de l'application
kubectl exec -it <nom-pod-app> -- nc -zv mysql-service 3306
```

### 9. Accéder à l'application depuis votre MacBook

Deux options:

#### Option 1: Port forwarding

```bash
# Dans la VM Vagrant
kubectl port-forward service/student-management-service 8089:8089

# Puis sur votre Mac (dans un autre terminal)
# Si vous avez configuré le port forwarding dans Vagrantfile
curl http://localhost:8080/student/actuator/health
```

#### Option 2: Accès direct via l'IP de Minikube

```bash
# Dans la VM Vagrant, obtenir l'IP
minikube ip

# Exemple: 192.168.49.2
# Depuis votre Mac, accéder à:
# http://192.168.49.2:30089/student/actuator/health
```

### 10. Mise à jour de l'application

Quand vous modifiez le code:

```bash
# 1. Rebuild l'image
./mvnw clean package -DskipTests
docker build -t fekikarim/student-management:v2 .

# 2. Si vous utilisez Docker Hub
docker push fekikarim/student-management:v2

# 3. Mettre à jour le deployment
kubectl set image deployment/student-management student-management=fekikarim/student-management:v2

# 4. Vérifier le rolling update
kubectl rollout status deployment/student-management

# 5. En cas de problème, rollback
kubectl rollout undo deployment/student-management
```

### 11. Nettoyage

```bash
# Supprimer tout
./deploy-k8s.sh delete

# Ou manuellement
kubectl delete -f k8s/

# Arrêter Minikube
minikube stop

# Sur votre Mac, arrêter Vagrant
exit  # Sortir de la VM
vagrant halt
```

## 🎯 Captures d'écran à prendre pour le rendu

1. **Architecture déployée**:
   ```bash
   kubectl get all
   ```

2. **Pods en cours d'exécution**:
   ```bash
   kubectl get pods -o wide
   ```

3. **Services exposés**:
   ```bash
   kubectl get svc
   ```

4. **PersistentVolume et PVC**:
   ```bash
   kubectl get pv,pvc
   ```

5. **ConfigMaps et Secrets**:
   ```bash
   kubectl get configmap,secret
   ```

6. **Logs de l'application montrant la connexion à MySQL**:
   ```bash
   kubectl logs -l app=student-management --tail=50
   ```

7. **Test de l'endpoint health**:
   ```bash
   curl http://$(minikube ip):30089/student/actuator/health
   ```

8. **Description complète du deployment**:
   ```bash
   kubectl describe deployment student-management
   ```

9. **Pipeline Jenkins (si configuré)** - capture depuis l'interface Jenkins

10. **Preuve du fonctionnement** - test d'une requête API:
    ```bash
    # Créer un test si vous avez des endpoints CRUD
    curl -X GET http://$(minikube ip):30089/student/api/...
    ```

## ⚠️ Résolution des problèmes courants

### Problème 1: Pods en état "ImagePullBackOff"

```bash
# Vérifier les détails
kubectl describe pod <nom-pod>

# Solution: Utiliser l'image locale
minikube image load fekikarim/student-management:latest

# Ou modifier imagePullPolicy à "Never" ou "IfNotPresent" dans app-deployment.yaml
```

### Problème 2: MySQL ne démarre pas

```bash
# Vérifier les logs
kubectl logs -l app=mysql

# Vérifier le PVC
kubectl describe pvc mysql-pvc

# Créer manuellement le répertoire
minikube ssh "sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"

# Supprimer et recréer le pod
kubectl delete pod -l app=mysql
```

### Problème 3: Application ne se connecte pas à MySQL

```bash
# Vérifier que MySQL est accessible
kubectl exec -it <pod-app> -- nc -zv mysql-service 3306

# Vérifier les variables d'environnement
kubectl exec -it <pod-app> -- env | grep SPRING

# Vérifier les logs
kubectl logs -l app=student-management
```

### Problème 4: Service non accessible

```bash
# Vérifier que le service existe
kubectl get svc student-management-service

# Vérifier les endpoints
kubectl get endpoints student-management-service

# Tester depuis un pod de test
kubectl run test-pod --image=busybox -it --rm -- wget -O- http://student-management-service:8089/student/actuator/health
```

## 📋 Checklist avant le rendu

- [ ] Tous les manifestes Kubernetes sont créés
- [ ] MySQL se déploie correctement avec PV/PVC
- [ ] L'application se connecte à MySQL
- [ ] Le health check répond correctement
- [ ] Le pipeline Jenkins est configuré
- [ ] Screenshots de tous les composants
- [ ] Documentation complète de l'architecture
- [ ] Tests effectués et validés
