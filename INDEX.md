# 📚 Index - Atelier 3 Kubernetes

Bienvenue dans votre projet de déploiement Kubernetes! Ce fichier vous guide vers les bonnes ressources.

---

## 🎯 Par où commencer?

### 1️⃣ Vous voulez COMPRENDRE l'architecture?
👉 Lisez: [RAPPORT-RENDU.md](RAPPORT-RENDU.md)
- Architecture complète
- Explications détaillées de chaque ressource Kubernetes
- Diagrammes
- Étapes du pipeline CI/CD

### 2️⃣ Vous voulez DÉPLOYER rapidement?
👉 Suivez: [COMMANDES-QUICK-START.md](COMMANDES-QUICK-START.md)
- Commandes prêtes à copier-coller
- Pas de théorie, que de la pratique
- Tests de vérification

### 3️⃣ Vous utilisez Vagrant?
👉 Consultez: [VAGRANT-GUIDE.md](VAGRANT-GUIDE.md)
- Instructions spécifiques pour Vagrant + Minikube
- Pas-à-pas détaillé
- Troubleshooting

### 4️⃣ Vous devez SYNCHRONISER vos fichiers?
👉 Regardez: [VAGRANT-SYNC-GUIDE.md](VAGRANT-SYNC-GUIDE.md)
- Plusieurs méthodes de synchronisation
- Configuration Vagrant
- Résolution de problèmes

### 5️⃣ Vous voulez la DOCUMENTATION complète?
👉 Explorez: [K8S-DEPLOYMENT-README.md](K8S-DEPLOYMENT-README.md)
- Documentation technique complète
- Toutes les commandes kubectl
- Monitoring et debugging
- Concepts Kubernetes

---

## 📁 Structure du projet

```
student-management/
│
├── 📂 k8s/                          # Manifestes Kubernetes
│   ├── mysql-configmap.yaml        # Configuration MySQL
│   ├── mysql-secret.yaml           # Secrets MySQL
│   ├── mysql-pv.yaml               # PersistentVolume MySQL
│   ├── mysql-pvc.yaml              # PersistentVolumeClaim MySQL
│   ├── mysql-deployment.yaml       # Deployment MySQL
│   ├── mysql-service.yaml          # Service MySQL
│   ├── app-configmap.yaml          # Configuration Application
│   ├── app-secret.yaml             # Secrets Application
│   ├── app-deployment.yaml         # Deployment Application
│   ├── app-service.yaml            # Service Application
│   └── kustomization.yaml          # Kustomize config
│
├── 📜 deploy-k8s.sh                # Script de déploiement automatique
│
├── 📄 Jenkinsfile                  # Pipeline CI/CD Jenkins
│
├── 📋 Documentation
│   ├── INDEX.md                    # ⬅️ VOUS ÊTES ICI!
│   ├── RAPPORT-RENDU.md            # 📊 Rapport complet pour le rendu
│   ├── K8S-DEPLOYMENT-README.md    # 📚 Doc technique complète
│   ├── VAGRANT-GUIDE.md            # 🚀 Guide Vagrant pas-à-pas
│   ├── VAGRANT-SYNC-GUIDE.md       # 🔄 Synchronisation des fichiers
│   └── COMMANDES-QUICK-START.md    # ⚡ Commandes rapides
│
└── 📂 src/                         # Code source Spring Boot
    └── ...
```

---

## 🚀 Démarrage ultra-rapide (3 minutes)

```bash
# 1. Entrer dans Vagrant
cd ~/Vagrant/ubunto && vagrant ssh

# 2. Démarrer Minikube
minikube start

# 3. Cloner/aller dans le projet
cd ~/workspace/k8s-atelier
git clone https://github.com/fekikarim/student-management.git
cd student-management

# 4. Préparer MySQL
minikube ssh "sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"

# 5. Déployer
chmod +x deploy-k8s.sh
./deploy-k8s.sh apply

# 6. Vérifier
kubectl get pods
curl http://$(minikube ip):30089/student/actuator/health
```

✅ C'est tout! Votre application est déployée!

---

## 📋 Checklist du rendu

### Fichiers à soumettre:
- [ ] Code source complet (src/)
- [ ] Manifestes Kubernetes (k8s/)
- [ ] Jenkinsfile modifié
- [ ] Documentation (RAPPORT-RENDU.md)

### Captures d'écran à prendre:
- [ ] `kubectl get all` - Vue d'ensemble
- [ ] `kubectl get pods -o wide` - Pods détaillés
- [ ] `kubectl get svc` - Services
- [ ] `kubectl get pv,pvc` - Stockage
- [ ] `kubectl describe deployment student-management`
- [ ] `kubectl logs -l app=student-management` - Logs applicatifs
- [ ] `curl http://$(minikube ip):30089/student/actuator/health` - Health check
- [ ] Interface Jenkins (pipeline en cours d'exécution)
- [ ] SonarQube Quality Gate

### Éléments à expliquer dans le rapport:
- [ ] Architecture Kubernetes déployée
- [ ] Rôle de chaque ressource (PV, PVC, ConfigMap, Secret, Deployment, Service)
- [ ] Étapes du pipeline CI/CD
- [ ] Preuve de fonctionnement (screenshots + explications)

---

## 🎯 Ressources Kubernetes déployées

| Type | Nom | Rôle | Fichier |
|------|-----|------|---------|
| **ConfigMap** | mysql-config | Config MySQL | [mysql-configmap.yaml](k8s/mysql-configmap.yaml) |
| **ConfigMap** | app-config | Config App | [app-configmap.yaml](k8s/app-configmap.yaml) |
| **Secret** | mysql-secret | Passwords MySQL | [mysql-secret.yaml](k8s/mysql-secret.yaml) |
| **Secret** | app-secret | Password App | [app-secret.yaml](k8s/app-secret.yaml) |
| **PersistentVolume** | mysql-pv | Volume physique | [mysql-pv.yaml](k8s/mysql-pv.yaml) |
| **PersistentVolumeClaim** | mysql-pvc | Demande volume | [mysql-pvc.yaml](k8s/mysql-pvc.yaml) |
| **Deployment** | mysql | Pods MySQL | [mysql-deployment.yaml](k8s/mysql-deployment.yaml) |
| **Deployment** | student-management | Pods App (x2) | [app-deployment.yaml](k8s/app-deployment.yaml) |
| **Service** | mysql-service | Expose MySQL (interne) | [mysql-service.yaml](k8s/mysql-service.yaml) |
| **Service** | student-management-service | Expose App (NodePort) | [app-service.yaml](k8s/app-service.yaml) |

---

## 🔧 Commandes essentielles

### Déploiement
```bash
./deploy-k8s.sh apply          # Déployer tout
./deploy-k8s.sh status         # Voir le status
./deploy-k8s.sh logs           # Voir les logs
./deploy-k8s.sh delete         # Tout supprimer
```

### Vérification
```bash
kubectl get all                # Vue d'ensemble
kubectl get pods              # Voir les pods
kubectl get svc               # Voir les services
kubectl logs -f -l app=student-management  # Logs en temps réel
```

### Tests
```bash
export MINIKUBE_IP=$(minikube ip)
curl http://$MINIKUBE_IP:30089/student/actuator/health    # Health
curl http://$MINIKUBE_IP:30089/student/actuator/info      # Info
curl http://$MINIKUBE_IP:30089/student/actuator/prometheus # Metrics
```

---

## 📞 Aide et Support

### Problème de déploiement?
1. Vérifiez les logs: `kubectl logs <pod-name>`
2. Vérifiez les événements: `kubectl get events`
3. Consultez: [VAGRANT-GUIDE.md](VAGRANT-GUIDE.md) section "Résolution des problèmes"

### Commande ne fonctionne pas?
- Consultez: [COMMANDES-QUICK-START.md](COMMANDES-QUICK-START.md) section "Résolution de problèmes"

### Fichiers manquants dans Vagrant?
- Lisez: [VAGRANT-SYNC-GUIDE.md](VAGRANT-SYNC-GUIDE.md)

### Questions sur l'architecture?
- Référez-vous à: [K8S-DEPLOYMENT-README.md](K8S-DEPLOYMENT-README.md)

---

## 🎓 Pour le rendu académique

Le fichier [RAPPORT-RENDU.md](RAPPORT-RENDU.md) contient:
- ✅ Architecture détaillée avec schémas
- ✅ Explication de toutes les ressources Kubernetes
- ✅ Description complète du pipeline CI/CD
- ✅ Liste des commandes de vérification
- ✅ Instructions pour les captures d'écran

**C'est le document principal à soumettre pour votre rendu!**

---

## 📊 Pipeline CI/CD

Le [Jenkinsfile](Jenkinsfile) contient maintenant:
1. Checkout du code
2. Build & Test (Maven)
3. Analyse SonarQube
4. Quality Gate
5. Package (JAR)
6. Docker Build
7. Docker Push
8. **Deploy to Kubernetes** ⭐ NOUVEAU
9. **Verify Deployment** ⭐ NOUVEAU

---

## 🌟 Points clés de l'architecture

### Haute disponibilité
- ✅ 2 replicas de l'application
- ✅ Rolling updates sans downtime
- ✅ Health checks (liveness + readiness)

### Persistance
- ✅ PersistentVolume pour MySQL
- ✅ Données conservées après redémarrage

### Sécurité
- ✅ Secrets pour les mots de passe
- ✅ MySQL non exposé à l'extérieur
- ✅ Séparation des configurations

### Observabilité
- ✅ Logs centralisés (kubectl logs)
- ✅ Métriques Prometheus exposées
- ✅ Health checks endpoints

---

## 🎉 Félicitations!

Vous avez maintenant:
- ✅ Une application Spring Boot déployée sur Kubernetes
- ✅ Une base de données MySQL persistante
- ✅ Un pipeline CI/CD complet
- ✅ Une architecture professionnelle
- ✅ Une documentation complète

**Tout est prêt pour votre rendu!** 🚀

---

## 📝 Contacts et ressources

- **Projet GitHub**: https://github.com/fekikarim/student-management
- **Docker Hub**: https://hub.docker.com/r/fekikarim/student-management
- **Documentation Kubernetes**: https://kubernetes.io/docs/
- **Documentation Spring Boot**: https://spring.io/projects/spring-boot

---

**Dernière mise à jour**: 15 Décembre 2025  
**Version**: 1.0  
**Environnement testé**: Vagrant + Minikube sur MacBook Pro M4
