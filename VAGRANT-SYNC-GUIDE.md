# Synchronisation des Fichiers avec Vagrant

## Option 1: Utiliser le dossier partagé Vagrant

Vagrant synchronise automatiquement le dossier de votre projet avec la VM.

### Sur votre MacBook:
```bash
# Le dossier sur votre Mac
cd /Users/karimfeki/Documents/esprit/4sim1/devops/student-management

# Tous les fichiers ici sont automatiquement synchronisés avec /vagrant dans la VM
```

### Dans la VM Vagrant:
```bash
vagrant ssh

# Accéder aux fichiers
cd /vagrant
ls -la

# Copier dans le workspace
mkdir -p ~/workspace/k8s-atelier
cp -r /vagrant/* ~/workspace/k8s-atelier/
cd ~/workspace/k8s-atelier
```

---

## Option 2: Cloner depuis GitHub (recommandé)

### Sur votre MacBook, pusher vers GitHub:
```bash
cd /Users/karimfeki/Documents/esprit/4sim1/devops/student-management

# Initialiser git si ce n'est pas fait
git init
git add .
git commit -m "Add Kubernetes manifests and deployment scripts"

# Pousser vers GitHub
git remote add origin https://github.com/fekikarim/student-management.git
git branch -M main
git push -u origin main
```

### Dans la VM Vagrant:
```bash
vagrant ssh

# Créer le workspace
mkdir -p ~/workspace/k8s-atelier
cd ~/workspace/k8s-atelier

# Cloner le projet
git clone https://github.com/fekikarim/student-management.git
cd student-management

# Tout est prêt!
ls -la k8s/
```

---

## Option 3: Copier manuellement avec scp

### Depuis votre MacBook:
```bash
# Trouver le port SSH de Vagrant
cd ~/Vagrant/ubunto
vagrant ssh-config

# Exemple de sortie:
# Host default
#   HostName 127.0.0.1
#   User vagrant
#   Port 2222
#   ...

# Copier tout le projet
scp -P 2222 -r /Users/karimfeki/Documents/esprit/4sim1/devops/student-management vagrant@127.0.0.1:~/workspace/

# Ou copier juste le dossier k8s
scp -P 2222 -r /Users/karimfeki/Documents/esprit/4sim1/devops/student-management/k8s vagrant@127.0.0.1:~/workspace/student-management/
```

---

## Option 4: Utiliser rsync (plus rapide)

### Depuis votre MacBook:
```bash
cd /Users/karimfeki/Documents/esprit/4sim1/devops/student-management

# Synchroniser avec rsync
vagrant rsync

# Ou manuellement
rsync -avz -e "ssh -p 2222" \
  --exclude='.git' \
  --exclude='target' \
  --exclude='*.jar' \
  ./ vagrant@127.0.0.1:~/workspace/student-management/
```

---

## Vérification dans Vagrant

```bash
# SSH dans Vagrant
vagrant ssh

# Vérifier que tous les fichiers sont présents
cd ~/workspace/student-management
ls -la

# Vérifier les manifestes Kubernetes
ls -la k8s/

# Vérifier le script de déploiement
ls -la deploy-k8s.sh
chmod +x deploy-k8s.sh

# Vérifier la structure
tree -L 2
# ou si tree n'est pas installé:
find . -maxdepth 2 -type d
```

---

## Configuration du Vagrantfile (optionnel)

Si vous voulez améliorer la synchronisation, modifiez votre Vagrantfile:

```ruby
Vagrant.configure("2") do |config|
  config.vm.box = "bento/ubuntu-24.04"
  
  # Synchronisation de votre projet
  config.vm.synced_folder "/Users/karimfeki/Documents/esprit/4sim1/devops/student-management", 
                          "/vagrant/student-management"
  
  # Port forwarding
  config.vm.network "forwarded_port", guest: 8080, host: 8080
  config.vm.network "forwarded_port", guest: 30089, host: 30089
  
  # Configuration mémoire/CPU
  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.cpus = 2
  end
end
```

Puis:
```bash
vagrant reload
```

---

## Structure attendue dans Vagrant

```
~/workspace/student-management/
├── k8s/
│   ├── mysql-configmap.yaml
│   ├── mysql-secret.yaml
│   ├── mysql-pv.yaml
│   ├── mysql-pvc.yaml
│   ├── mysql-deployment.yaml
│   ├── mysql-service.yaml
│   ├── app-configmap.yaml
│   ├── app-secret.yaml
│   ├── app-deployment.yaml
│   ├── app-service.yaml
│   └── kustomization.yaml
├── deploy-k8s.sh
├── Jenkinsfile
├── Dockerfile
├── pom.xml
├── K8S-DEPLOYMENT-README.md
├── VAGRANT-GUIDE.md
├── RAPPORT-RENDU.md
└── COMMANDES-QUICK-START.md
```

---

## Commandes de démarrage rapide après synchronisation

```bash
# 1. SSH dans Vagrant
vagrant ssh

# 2. Aller dans le projet
cd ~/workspace/student-management
# ou si via /vagrant
cd /vagrant/student-management

# 3. Démarrer Minikube
minikube start

# 4. Créer le répertoire MySQL
minikube ssh "sudo mkdir -p /mnt/data/mysql && sudo chmod 777 /mnt/data/mysql"

# 5. Déployer
chmod +x deploy-k8s.sh
./deploy-k8s.sh apply

# 6. Vérifier
kubectl get all
curl http://$(minikube ip):30089/student/actuator/health
```

---

## Troubleshooting

### Fichiers non synchronisés

```bash
# Forcer la synchronisation
vagrant rsync

# Recharger Vagrant
vagrant reload

# Vérifier les permissions
vagrant ssh
ls -la /vagrant
```

### Problèmes de permissions

```bash
# Dans Vagrant
sudo chown -R vagrant:vagrant ~/workspace/student-management
chmod +x ~/workspace/student-management/deploy-k8s.sh
```

### Fichiers manquants

```bash
# Re-cloner depuis GitHub
cd ~/workspace
rm -rf student-management
git clone https://github.com/fekikarim/student-management.git
cd student-management
```

---

Choisissez l'option qui vous convient le mieux. Je recommande **l'Option 2 (GitHub)** car c'est la plus propre et reproductible! 🚀
