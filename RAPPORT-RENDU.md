# Atelier 3 - Rapport de Déploiement Kubernetes
## Student Management Application

**Étudiant**: Karim Feki  
**Date**: 15 Décembre 2025  
**Objectif**: Déploiement d'une application Spring Boot/MySQL sur Kubernetes avec intégration CI/CD

---

## 1. Architecture déployée

### 1.1 Vue d'ensemble

L'application **Student Management** est une application Spring Boot déployée sur un cluster Kubernetes (Minikube) avec une base de données MySQL persistante. L'architecture est entièrement conteneurisée et orchestrée par Kubernetes.

### 1.2 Schéma de l'architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Kubernetes Cluster (Minikube)                 │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                   Application Tier                           │   │
│  │                                                               │   │
│  │   ┌─────────────────┐         ┌─────────────────┐          │   │
│  │   │   Pod 1         │         │   Pod 2         │          │   │
│  │   │  Spring Boot    │         │  Spring Boot    │          │   │
│  │   │  Port: 8089     │         │  Port: 8089     │          │   │
│  │   └────────┬────────┘         └────────┬────────┘          │   │
│  │            │                            │                    │   │
│  │            └──────────┬─────────────────┘                   │   │
│  │                       │                                      │   │
│  │          ┌────────────▼────────────┐                        │   │
│  │          │  student-management     │                        │   │
│  │          │       -service          │                        │   │
│  │          │  Type: NodePort         │                        │   │
│  │          │  Port: 8089 -> 30089    │                        │   │
│  │          └────────────┬────────────┘                        │   │
│  └─────────────────────┬─┬─────────────────────────────────────┘   │
│                        │ │                                           │
│  ┌─────────────────────┴─┴─────────────────────────────────────┐   │
│  │                   Database Tier                              │   │
│  │                                                               │   │
│  │          ┌────────────────────────┐                          │   │
│  │          │   mysql-service        │                          │   │
│  │          │   Type: ClusterIP      │                          │   │
│  │          │   Port: 3306           │                          │   │
│  │          └───────────┬────────────┘                          │   │
│  │                      │                                        │   │
│  │          ┌───────────▼────────────┐                          │   │
│  │          │   MySQL Pod            │                          │   │
│  │          │   Image: mysql:8.0     │                          │   │
│  │          │   Port: 3306           │                          │   │
│  │          └───────────┬────────────┘                          │   │
│  │                      │                                        │   │
│  │          ┌───────────▼────────────┐                          │   │
│  │          │   mysql-pvc            │                          │   │
│  │          │   Storage: 1Gi         │                          │   │
│  │          └───────────┬────────────┘                          │   │
│  │                      │                                        │   │
│  │          ┌───────────▼────────────┐                          │   │
│  │          │   mysql-pv             │                          │   │
│  │          │   hostPath             │                          │   │
│  │          │   /mnt/data/mysql      │                          │   │
│  │          └────────────────────────┘                          │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                   Configuration Layer                         │   │
│  │                                                               │   │
│  │  ConfigMaps:                    Secrets:                     │   │
│  │  • mysql-config                 • mysql-secret               │   │
│  │    - MYSQL_DATABASE             - mysql-root-password        │   │
│  │                                  - mysql-user                 │   │
│  │  • app-config                    - mysql-password            │   │
│  │    - SPRING_DATASOURCE_URL                                   │   │
│  │    - SPRING_DATASOURCE_USERNAME • app-secret                 │   │
│  │    - JPA Configuration           - spring-datasource-password│   │
│  └──────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    Accès externe via NodePort
                http://<MINIKUBE_IP>:30089/student
```

---

## 2. Ressources Kubernetes mises en œuvre

### 2.1 PersistentVolume (PV) - `mysql-pv.yaml`

**Rôle**: Définit un volume de stockage physique sur le cluster pour assurer la persistance des données MySQL.

**Caractéristiques**:
- **Capacité**: 1Gi
- **Mode d'accès**: ReadWriteOnce (lecture/écriture par un seul nœud)
- **Type**: hostPath (stockage local sur `/mnt/data/mysql`)
- **StorageClass**: manual

**Importance**: Garantit que les données MySQL survivent aux redémarrages des pods.

### 2.2 PersistentVolumeClaim (PVC) - `mysql-pvc.yaml`

**Rôle**: Demande/réclame un volume de stockage du PV pour être utilisé par MySQL.

**Caractéristiques**:
- **Capacité demandée**: 1Gi
- **Mode d'accès**: ReadWriteOnce
- **StorageClass**: manual (lié au PV)

**Importance**: Mécanisme d'abstraction permettant aux pods de demander du stockage sans connaître les détails d'implémentation.

### 2.3 ConfigMap

#### a) `mysql-configmap.yaml`
**Rôle**: Stocke les configurations non sensibles de MySQL.

**Données**:
- `MYSQL_DATABASE`: student_management

#### b) `app-configmap.yaml`
**Rôle**: Stocke les configurations de l'application Spring Boot.

**Données**:
- URL de connexion MySQL
- Nom d'utilisateur de la base de données
- Configuration JPA/Hibernate
- Port de management

**Importance**: Sépare la configuration du code, permettant de modifier les paramètres sans rebuild de l'image.

### 2.4 Secret

#### a) `mysql-secret.yaml`
**Rôle**: Stocke les informations sensibles de MySQL (encodées en base64).

**Données**:
- `mysql-root-password`: Mot de passe root
- `mysql-user`: studentapp
- `mysql-password`: studentpass

#### b) `app-secret.yaml`
**Rôle**: Stocke le mot de passe de connexion à la base de données pour l'application.

**Données**:
- `spring-datasource-password`: studentpass (encodé en base64)

**Importance**: Sécurise les données sensibles (les Secrets sont encodés et peuvent être chiffrés au repos).

### 2.5 Deployment

#### a) `mysql-deployment.yaml`
**Rôle**: Définit et gère le déploiement du pod MySQL.

**Caractéristiques**:
- **Replicas**: 1 (base de données stateful)
- **Image**: mysql:8.0
- **Port**: 3306
- **Variables d'environnement**: Injectées depuis ConfigMap et Secret
- **Volume**: Monte le PVC pour la persistance
- **Ressources**:
  - Limites: 512Mi RAM, 500m CPU
  - Requêtes: 256Mi RAM, 250m CPU

**Stratégie**: Recreate (arrêt puis redémarrage pour éviter deux instances simultanées)

#### b) `app-deployment.yaml`
**Rôle**: Définit et gère le déploiement des pods de l'application Spring Boot.

**Caractéristiques**:
- **Replicas**: 2 (haute disponibilité et load balancing)
- **Image**: fekikarim/student-management:latest
- **ImagePullPolicy**: Always (toujours récupérer la dernière version)
- **Port**: 8089
- **InitContainer**: Attend que MySQL soit disponible avant de démarrer
- **Probes**:
  - **Liveness**: Health check (`/student/actuator/health`) - redémarre le pod si échec
  - **Readiness**: Vérifie que l'app est prête à recevoir du trafic
- **Ressources**:
  - Limites: 512Mi RAM, 500m CPU
  - Requêtes: 256Mi RAM, 250m CPU

**Importance**: 
- Les probes garantissent la haute disponibilité
- L'initContainer évite les erreurs de connexion au démarrage
- Les 2 replicas permettent le rolling update sans downtime

### 2.6 Service

#### a) `mysql-service.yaml`
**Rôle**: Expose MySQL en interne au cluster uniquement.

**Caractéristiques**:
- **Type**: ClusterIP (accès interne uniquement)
- **Port**: 3306
- **Selector**: app=mysql

**Importance**: MySQL n'est pas accessible depuis l'extérieur, renforçant la sécurité.

#### b) `app-service.yaml`
**Rôle**: Expose l'application à l'extérieur du cluster.

**Caractéristiques**:
- **Type**: NodePort (accès externe)
- **Port interne**: 8089
- **NodePort**: 30089 (port exposé sur chaque nœud)
- **Selector**: app=student-management

**Importance**: Permet l'accès externe à l'application et fait du load balancing entre les 2 pods.

---

## 3. Pipeline CI/CD - Étapes principales

### 3.1 Architecture du pipeline

```
┌──────────────────────────────────────────────────────────────────┐
│                         Pipeline Jenkins                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Checkout                                                      │
│     └─> Clone repository GitHub (branche main)                   │
│                                                                   │
│  2. Build & Test                                                  │
│     └─> Compilation + Tests unitaires (Maven)                    │
│                                                                   │
│  3. MVN SONARQUBE                                                 │
│     └─> Analyse qualité du code (SonarQube)                      │
│                                                                   │
│  4. Quality Gate                                                  │
│     └─> Validation des critères de qualité                       │
│                                                                   │
│  5. Package                                                       │
│     └─> Création du JAR                                          │
│                                                                   │
│  6. Docker Build                                                  │
│     └─> Construction de l'image Docker                           │
│         Tags: BUILD_NUMBER et latest                             │
│                                                                   │
│  7. Docker Push                                                   │
│     └─> Push vers Docker Hub                                     │
│         Image: fekikarim/student-management                      │
│                                                                   │
│  8. Deploy to Kubernetes ⭐ NOUVEAU                               │
│     ├─> Application des manifestes MySQL                         │
│     ├─> Attente du démarrage de MySQL                            │
│     ├─> Application des manifestes de l'application              │
│     ├─> Mise à jour de l'image du deployment                     │
│     └─> Attente du démarrage de l'application                    │
│                                                                   │
│  9. Verify Deployment ⭐ NOUVEAU                                  │
│     └─> Vérification du status des pods et services              │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### 3.2 Description détaillée des étapes

#### Étape 1: Checkout
- Clone le code depuis GitHub
- Branche: `main`
- Rend le wrapper Maven exécutable

#### Étape 2: Build & Test
- Commande: `./mvnw clean test`
- Compile le code source
- Exécute les tests unitaires
- Génère les rapports de tests

#### Étape 3: MVN SONARQUBE
- Analyse statique du code
- Détection de:
  - Bugs
  - Vulnérabilités
  - Code smells
  - Couverture de code
- Serveur: http://192.168.33.10:9000

#### Étape 4: Quality Gate
- Attend la validation par SonarQube
- **Bloque le pipeline** si les critères ne sont pas respectés
- Timeout: 10 minutes

#### Étape 5: Package
- Commande: `./mvnw package -DskipTests`
- Création du fichier JAR
- Résultat: `target/student-management-0.0.1-SNAPSHOT.jar`

#### Étape 6: Docker Build
- Construction de l'image Docker
- Tags créés:
  - `fekikarim/student-management:${BUILD_NUMBER}`
  - `fekikarim/student-management:latest`

#### Étape 7: Docker Push
- Connexion à Docker Hub (credentials sécurisés)
- Push des deux tags
- Déconnexion automatique

#### Étape 8: Deploy to Kubernetes ⭐
**Nouvelle étape intégrée**

Séquence de déploiement:

1. **Déploiement MySQL**:
   ```bash
   kubectl apply -f k8s/mysql-configmap.yaml
   kubectl apply -f k8s/mysql-secret.yaml
   kubectl apply -f k8s/mysql-pv.yaml
   kubectl apply -f k8s/mysql-pvc.yaml
   kubectl apply -f k8s/mysql-deployment.yaml
   kubectl apply -f k8s/mysql-service.yaml
   ```

2. **Attente MySQL**:
   ```bash
   kubectl wait --for=condition=ready pod -l app=mysql --timeout=120s
   ```

3. **Déploiement Application**:
   ```bash
   kubectl apply -f k8s/app-configmap.yaml
   kubectl apply -f k8s/app-secret.yaml
   kubectl set image deployment/student-management \
     student-management=${IMAGE_NAME}:${BUILD_NUMBER}
   kubectl apply -f k8s/app-service.yaml
   ```

4. **Attente Application**:
   ```bash
   kubectl wait --for=condition=ready pod \
     -l app=student-management --timeout=180s
   ```

#### Étape 9: Verify Deployment ⭐
**Vérification automatique**

Affiche:
- Status des deployments
- Liste des pods
- Description des services

---

## 4. Preuve du bon fonctionnement

### 4.1 Commandes de vérification

```bash
# 1. Vérifier les pods
kubectl get pods

# 2. Vérifier les services
kubectl get svc

# 3. Vérifier les déploiements
kubectl get deployments

# 4. Vérifier le stockage
kubectl get pv,pvc

# 5. Test health check
curl http://$(minikube ip):30089/student/actuator/health
```

### 4.2 Résultats attendus

#### Pods:
```
NAME                                  READY   STATUS    RESTARTS   AGE
mysql-xxxxxxxxxx-xxxxx               1/1     Running   0          5m
student-management-xxxxxxxxxx-xxxxx  1/1     Running   0          3m
student-management-xxxxxxxxxx-yyyyy  1/1     Running   0          3m
```

#### Services:
```
NAME                         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
mysql-service                ClusterIP   10.96.xxx.xxx   <none>        3306/TCP         5m
student-management-service   NodePort    10.96.xxx.xxx   <none>        8089:30089/TCP   3m
```

#### Health Check:
```json
{
  "status": "UP"
}
```

### 4.3 Tests fonctionnels

```bash
# Test 1: Actuator Health
curl http://$(minikube ip):30089/student/actuator/health

# Test 2: Actuator Info
curl http://$(minikube ip):30089/student/actuator/info

# Test 3: Prometheus Metrics
curl http://$(minikube ip):30089/student/actuator/prometheus

# Test 4: Connexion à la base de données (via logs)
kubectl logs -l app=student-management | grep "HikariPool"
```

---

## 5. Avantages de cette architecture

### 5.1 Haute disponibilité
- 2 replicas de l'application
- Rolling updates sans downtime
- Health checks automatiques

### 5.2 Scalabilité
- Facile de modifier le nombre de replicas
- Load balancing automatique via Service

### 5.3 Sécurité
- MySQL non exposé à l'extérieur
- Secrets encodés
- Séparation des configurations

### 5.4 Persistance
- Données MySQL conservées même après redémarrage
- PersistentVolume indépendant du cycle de vie des pods

### 5.5 Observabilité
- Probes pour monitoring
- Métriques Prometheus exposées
- Logs centralisés via kubectl

### 5.6 Automatisation
- Pipeline CI/CD complet
- Déploiement automatique après chaque commit
- Vérifications automatiques

---

## 6. Commandes utiles

### Déploiement
```bash
# Déploiement complet
./deploy-k8s.sh apply

# Status
./deploy-k8s.sh status

# Logs
./deploy-k8s.sh logs

# Suppression
./deploy-k8s.sh delete
```

### Debug
```bash
# Logs d'un pod
kubectl logs <pod-name>

# Describe un pod
kubectl describe pod <pod-name>

# Shell dans un pod
kubectl exec -it <pod-name> -- /bin/bash

# Port-forward pour test local
kubectl port-forward svc/student-management-service 8089:8089
```

### Monitoring
```bash
# Surveiller les pods en temps réel
kubectl get pods -w

# Événements du cluster
kubectl get events --sort-by=.metadata.creationTimestamp

# Ressources utilisées
kubectl top pods
```

---

## 7. Conclusion

Ce projet démontre une maîtrise complète de:

✅ **Kubernetes**: Déploiement avec toutes les ressources essentielles (Deployment, Service, ConfigMap, Secret, PV, PVC)

✅ **Conteneurisation**: Application Spring Boot complètement dockerisée

✅ **CI/CD**: Pipeline Jenkins automatisé de bout en bout

✅ **DevOps**: Intégration complète du build, test, qualité, et déploiement

✅ **Bonnes pratiques**: 
- Séparation des configurations
- Haute disponibilité
- Health checks
- Gestion des ressources
- Sécurité

L'application est maintenant prête pour un environnement de production avec quelques ajustements (StorageClass cloud, Ingress, monitoring avancé, etc.).

---

**Date de réalisation**: 15 Décembre 2025  
**Environnement**: Vagrant + Minikube sur MacBook Pro M4  
**Stack technique**: Spring Boot 3.x + MySQL 8.0 + Kubernetes + Jenkins + Docker
