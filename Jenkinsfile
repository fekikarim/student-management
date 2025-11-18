pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    url: 'https://github.com/fekikarim/student-management.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn compile'
            }
        }
    }
}