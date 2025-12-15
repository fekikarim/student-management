# Guide de Dépannage Avancé 🔧

Ce guide couvre les problèmes courants et leurs solutions lors du déploiement sur Kubernetes.

---

## 🔴 Problèmes de démarrage

### MySQL ne démarre pas

#### Symptômes
```bash
kubectl get pods
# NAME                     READY   STATUS             RESTARTS   AGE
# mysql-xxx                0/1     CrashLoopBackOff   5          5m
```

#### Diagnostic
```bash
# Voir les logs
kubectl logs -l app=mysql

# Erreur commune: "Can't create/write to file '/var/lib/mysql/...' (Errcode: 13 - Permission denied)"
```

#### Solution 1: Permissions du volume
```bash
# Recréer le répertoire avec les bonnes permissions
minikube ssh
sudo rm -rf /mnt/data/mysql
sudo mkdir -p /mnt/data/mysql
sudo chmod 777 /mnt/data/mysql
exit

# Supprimer et recréer le pod
kubectl delete pod -l app=mysql
kubectl get pods -w
```

#### Solution 2: SELinux (si applicable)
```bash
minikube ssh
sudo chcon -Rt svirt_sandbox_file_t /mnt/data/mysql
exit
```

#### Solution 3: Recréer complètement le PV/PVC
```bash
kubectl delete -f k8s/mysql-deployment.yaml
kubectl delete -f k8s/mysql-pvc.yaml
kubectl delete -f k8s/mysql-pv.yaml

# Attendre quelques secondes
sleep 5

# Recréer dans l'ordre
kubectl apply -f k8s/mysql-pv.yaml
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml

# Vérifier
kubectl get pv,pvc
kubectl get pods -w
```

---

### Application ne se connecte pas à MySQL

#### Symptômes
```bash
# Logs de l'application
kubectl logs -l app=student-management

# Erreur: "Communications link failure" ou "Connection refused"
```

#### Diagnostic
```bash
# 1. Vérifier que MySQL est en cours d'exécution
kubectl get pods -l app=mysql
# Doit être Running et READY 1/1

# 2. Vérifier le service MySQL
kubectl get svc mysql-service
kubectl get endpoints mysql-service
# Les endpoints ne doivent PAS être vides

# 3. Tester la connexion depuis un pod de l'app
POD_NAME=$(kubectl get pod -l app=student-management -o jsonpath="{.items[0].metadata.name}")
kubectl exec -it $POD_NAME -- nc -zv mysql-service 3306
```

#### Solution 1: Attendre que MySQL soit complètement prêt
```bash
# MySQL peut prendre 1-2 minutes pour initialiser
kubectl wait --for=condition=ready pod -l app=mysql --timeout=180s

# Redémarrer l'application après
kubectl rollout restart deployment/student-management
```

#### Solution 2: Vérifier les variables d'environnement
```bash
POD_NAME=$(kubectl get pod -l app=student-management -o jsonpath="{.items[0].metadata.name}")
kubectl exec -it $POD_NAME -- env | grep SPRING

# Vérifier que:
# SPRING_DATASOURCE_URL=jdbc:mysql://mysql-service:3306/student_management
# SPRING_DATASOURCE_USERNAME=studentapp
# SPRING_DATASOURCE_PASSWORD=studentpass
```

#### Solution 3: Vérifier les ConfigMaps et Secrets
```bash
# ConfigMap
kubectl get configmap app-config -o yaml

# Secret (decoder)
kubectl get secret app-secret -o jsonpath='{.data.spring-datasource-password}' | base64 --decode
# Doit afficher: studentpass
```

---

### Image Docker ne se télécharge pas

#### Symptômes
```bash
kubectl get pods
# NAME                         READY   STATUS         RESTARTS   AGE
# student-management-xxx       0/1     ErrImagePull   0          30s
```

#### Diagnostic
```bash
kubectl describe pod <pod-name>
# Regarder la section "Events"
# Erreur commune: "Failed to pull image" ou "ImagePullBackOff"
```

#### Solution 1: Vérifier que l'image existe
```bash
# Tester le pull depuis votre machine
docker pull fekikarim/student-management:latest

# Si ça fonctionne, le problème est dans Kubernetes
```

#### Solution 2: Charger l'image dans Minikube
```bash
# Build l'image localement
./mvnw clean package -DskipTests
docker build -t fekikarim/student-management:latest .

# Charger dans Minikube
minikube image load fekikarim/student-management:latest

# Vérifier
minikube image ls | grep student-management

# Modifier imagePullPolicy dans app-deployment.yaml
# imagePullPolicy: Always -> imagePullPolicy: IfNotPresent

# Redéployer
kubectl apply -f k8s/app-deployment.yaml
```

#### Solution 3: Credentials Docker Hub (image privée)
```bash
# Si votre image est privée, créer un secret
kubectl create secret docker-registry dockerhub-secret \
  --docker-server=https://index.docker.io/v1/ \
  --docker-username=fekikarim \
  --docker-password=<votre-password> \
  --docker-email=<votre-email>

# Puis dans app-deployment.yaml, ajouter:
# spec:
#   template:
#     spec:
#       imagePullSecrets:
#       - name: dockerhub-secret
```

---

## 🟡 Problèmes de performance

### Pods redémarrent fréquemment

#### Diagnostic
```bash
kubectl get pods
# RESTARTS column > 0

kubectl describe pod <pod-name>
# Regarder la raison du restart
```

#### Problème 1: Liveness probe échoue

```bash
# Logs du pod avant le restart
kubectl logs <pod-name> --previous

# Solution: Augmenter le délai initial
# Dans app-deployment.yaml:
livenessProbe:
  initialDelaySeconds: 90  # au lieu de 60
  periodSeconds: 15         # au lieu de 10
  timeoutSeconds: 10        # au lieu de 5
```

#### Problème 2: Mémoire insuffisante

```bash
# Vérifier les ressources
kubectl top pods

# Si proche de la limite, augmenter dans app-deployment.yaml:
resources:
  limits:
    memory: "1Gi"      # au lieu de 512Mi
    cpu: "1000m"       # au lieu de 500m
  requests:
    memory: "512Mi"    # au lieu de 256Mi
    cpu: "500m"        # au lieu de 250m
```

---

### Application lente

#### Diagnostic
```bash
# 1. Vérifier les ressources
kubectl top pods
kubectl top nodes

# 2. Vérifier les logs
kubectl logs -l app=student-management --tail=100 | grep -i "slow\|timeout\|error"

# 3. Vérifier la connexion MySQL
kubectl exec -it <app-pod> -- time nc -zv mysql-service 3306
```

#### Solutions

**1. Augmenter les ressources MySQL**
```yaml
# Dans mysql-deployment.yaml
resources:
  limits:
    memory: "1Gi"
    cpu: "1000m"
  requests:
    memory: "512Mi"
    cpu: "500m"
```

**2. Optimiser la connexion pool**
Ajouter dans application.properties:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

**3. Scaler l'application**
```bash
kubectl scale deployment/student-management --replicas=3
```

---

## 🟢 Problèmes réseau

### Service non accessible depuis l'extérieur

#### Diagnostic
```bash
# 1. Vérifier le service
kubectl get svc student-management-service

# 2. Vérifier le NodePort
kubectl describe svc student-management-service | grep NodePort

# 3. Obtenir l'IP de Minikube
minikube ip
```

#### Solutions

**1. Utiliser minikube service**
```bash
minikube service student-management-service --url
# Utiliser l'URL retournée
```

**2. Port-forwarding en local**
```bash
kubectl port-forward svc/student-management-service 8089:8089
# Puis accéder à: http://localhost:8089/student/actuator/health
```

**3. Vérifier les endpoints**
```bash
kubectl get endpoints student-management-service

# Si vide, problème avec les pods
kubectl get pods -l app=student-management -o wide
```

---

### Pods ne peuvent pas communiquer entre eux

#### Diagnostic
```bash
# Tester la communication
POD_APP=$(kubectl get pod -l app=student-management -o jsonpath="{.items[0].metadata.name}")
kubectl exec -it $POD_APP -- ping -c 3 mysql-service

# Si ça ne fonctionne pas, problème réseau
```

#### Solutions

**1. Vérifier le DNS**
```bash
kubectl exec -it $POD_APP -- nslookup mysql-service
# Doit retourner l'IP du service
```

**2. Vérifier les Network Policies**
```bash
kubectl get networkpolicies
# S'il y en a, elles peuvent bloquer le trafic
```

**3. Redémarrer CoreDNS**
```bash
kubectl rollout restart deployment/coredns -n kube-system
```

---

## 🔵 Problèmes de stockage

### PVC reste en Pending

#### Diagnostic
```bash
kubectl get pvc
# STATUS: Pending

kubectl describe pvc mysql-pvc
# Regarder les Events
```

#### Solutions

**1. Vérifier le PV**
```bash
kubectl get pv
# STATUS doit être Available

# Si pas de PV, le créer:
kubectl apply -f k8s/mysql-pv.yaml
```

**2. Vérifier le StorageClass**
```bash
kubectl get storageclass

# Pour Minikube, utiliser 'standard' au lieu de 'manual'
# Dans mysql-pv.yaml et mysql-pvc.yaml:
# storageClassName: standard
```

**3. Créer manuellement le répertoire**
```bash
minikube ssh
sudo mkdir -p /mnt/data/mysql
sudo chmod 777 /mnt/data/mysql
exit
```

---

### Données MySQL perdues après redémarrage

#### Diagnostic
```bash
# Vérifier que le PVC est bien monté
kubectl describe pod <mysql-pod> | grep -A 5 Mounts

# Vérifier le contenu du volume
kubectl exec -it <mysql-pod> -- ls -la /var/lib/mysql
```

#### Solutions

**1. Vérifier le PVC dans le Deployment**
```yaml
# Dans mysql-deployment.yaml, vérifier:
volumeMounts:
- name: mysql-persistent-storage
  mountPath: /var/lib/mysql

volumes:
- name: mysql-persistent-storage
  persistentVolumeClaim:
    claimName: mysql-pvc
```

**2. Vérifier que le PV est persistant**
```yaml
# Dans mysql-pv.yaml:
spec:
  persistentVolumeReclaimPolicy: Retain  # au lieu de Delete
```

---

## 🛠️ Outils de debug avancés

### 1. Créer un pod de debug

```bash
# Pod avec outils réseau
kubectl run debug --rm -it --image=nicolaka/netshoot -- bash

# Dans le pod:
ping mysql-service
nslookup mysql-service
curl http://student-management-service:8089/student/actuator/health
```

### 2. Activer les logs détaillés de l'application

```bash
# Ajouter dans app-configmap.yaml:
data:
  LOGGING_LEVEL_ROOT: "DEBUG"
  LOGGING_LEVEL_ORG_SPRINGFRAMEWORK: "DEBUG"

# Redéployer
kubectl apply -f k8s/app-configmap.yaml
kubectl rollout restart deployment/student-management
```

### 3. Accéder à la console MySQL

```bash
# Se connecter au pod MySQL
MYSQL_POD=$(kubectl get pod -l app=mysql -o jsonpath="{.items[0].metadata.name}")
kubectl exec -it $MYSQL_POD -- mysql -u root -prootpassword

# Dans MySQL:
SHOW DATABASES;
USE student_management;
SHOW TABLES;
SELECT * FROM students LIMIT 10;
```

### 4. Examiner les métriques Kubernetes

```bash
# Activer metrics-server si nécessaire
minikube addons enable metrics-server

# Voir les métriques
kubectl top nodes
kubectl top pods

# Métriques détaillées
kubectl describe node minikube
```

---

## 📊 Monitoring et alerting

### Vérifier la santé globale

```bash
#!/bin/bash
echo "=== Checking Cluster Health ==="

echo -e "\n1. Nodes:"
kubectl get nodes

echo -e "\n2. Pods:"
kubectl get pods

echo -e "\n3. Services:"
kubectl get svc

echo -e "\n4. PVC:"
kubectl get pvc

echo -e "\n5. Recent Events:"
kubectl get events --sort-by=.metadata.creationTimestamp | tail -10

echo -e "\n6. Application Health:"
curl -s http://$(minikube ip):30089/student/actuator/health | jq

echo -e "\n✅ Health check complete!"
```

---

## 🚨 Commandes d'urgence

### Redémarrage complet

```bash
# 1. Supprimer tout
kubectl delete -f k8s/

# 2. Nettoyer les volumes
minikube ssh "sudo rm -rf /mnt/data/mysql"
minikube ssh "sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"

# 3. Attendre
sleep 5

# 4. Redéployer
./deploy-k8s.sh apply
```

### Reset complet de Minikube

```bash
# Sauvegarder vos données si nécessaire!

# Supprimer le cluster
minikube delete

# Recréer
minikube start --memory=4096 --cpus=2

# Redéployer
./deploy-k8s.sh apply
```

---

## 📞 Obtenir de l'aide

### Logs à collecter pour le support

```bash
#!/bin/bash
# Script pour collecter les logs de debug

mkdir -p debug-logs

kubectl get all > debug-logs/all-resources.txt
kubectl get events --sort-by=.metadata.creationTimestamp > debug-logs/events.txt
kubectl describe deployments > debug-logs/deployments.txt
kubectl describe pods > debug-logs/pods.txt
kubectl logs -l app=mysql > debug-logs/mysql-logs.txt 2>&1
kubectl logs -l app=student-management > debug-logs/app-logs.txt 2>&1
kubectl get pv,pvc -o yaml > debug-logs/storage.yaml

echo "Logs collectés dans debug-logs/"
```

---

**Besoin d'aide supplémentaire?** Consultez les autres documentations ou créez une issue sur GitHub!
