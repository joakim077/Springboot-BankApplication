pipeline {
    agent {label 'runner_01'}

    stages {
        stage('Checkout') {
            steps {
                echo 'Checkout code from github'
                git branch: 'main', url: 'https://github.com/joakim077/Springboot-BankApplication.git'
            }
        }
        stage('Build') {
            steps {
                echo 'building the docker image..'
                sh 'docker build -t bank-app .'
            }
        }
         stage('Test') {
            steps {
                echo 'testing the application..'
            }
        }
         stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred', passwordVariable: 'PASSWD', usernameVariable: 'USER')]) {
                    sh 'docker tag bank-app ${USER}/spring-boot-bankapp'
                    sh 'docker login -u$USER -p$PASSWD'
                    sh 'docker push $USER/spring-boot-bankapp'
                } 
            }
        }
         stage('Deploy') {
            steps {
                sh 'docker compose version'
                sh 'docker compose up -d'
            }
        }
    }
}
