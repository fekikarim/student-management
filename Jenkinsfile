pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        IMAGE_NAME = 'fekikarim/student-management'
        // SonarQube configuration
        SONARQUBE_SERVER = 'SonarQube'
        SONAR_PROJECT_KEY = 'fekikarim_student-management_9e2a077b-3049-4a9a-9c0f-cdd057d5f477'
        SONAR_PROJECT_NAME = 'student-management'
        SONAR_HOST_URL = 'http://192.168.33.10:9000'
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
        
        stage('MVN SONARQUBE') {
            steps {
                withSonarQubeEnv("${env.SONARQUBE_SERVER}") {
                    sh """
                        ./mvnw sonar:sonar \
                            -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
                            -Dsonar.projectName=${env.SONAR_PROJECT_NAME} \
                            -Dsonar.host.url=http://192.168.33.10:9000
                    """
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    // Give SonarQube time to finalize the task status
                    sleep(time: 5, unit: 'SECONDS')
                }
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
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
            echo 'Build and SonarQube analysis succeeded!'
        }
        failure {
            echo 'Build or SonarQube analysis failed!'
        }
    }
}