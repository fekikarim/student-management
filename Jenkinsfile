pipeline {
    agent any
    environment {
        IMAGE_NAME = 'fekikarim/student-management'
    }
    triggers {
        githubPush()
    }
    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                git branch: 'main', 
                    url: 'https://github.com/fekikarim/student-management.git'
                sh 'chmod +x mvnw'
            }
        }
        stage('Build & Test') {
            steps {
                sh './mvnw clean test'
            }
        }
        stage('Package') {
            steps {
                sh './mvnw package -DskipTests'
            }
        }
        stage('Docker Build') {
            steps {
                sh """
                    docker build \
                        -t ${env.IMAGE_NAME}:${env.BUILD_NUMBER} \
                        -t ${env.IMAGE_NAME}:latest \
                        .
                """
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                    sh """
                        echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        docker push ${env.IMAGE_NAME}:${env.BUILD_NUMBER}
                        docker push ${env.IMAGE_NAME}:latest
                        docker logout
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}