pipeline {
    agent any

    environment{
        DOCKER_IMAGE = 'joakim077/springboot-application:latest'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checkout Code'
                git credentialsId: 'github', url: 'https://github.com/joakim077/Springboot-BankApplication.git', branch: 'main'
            }
        }
        
        stage('Build') {
            steps {
                echo 'building docker image'
                sh 'docker build  -t "${DOCKER_IMAGE}" .'
            }
        }

        stage('Trivy Image Vulnerability Scan') {
            steps {
                sh 'trivy image --format json --output trivy-report.json "${DOCKER_IMAGE}"'
            }
        }
        
        stage('Tag Image and Push') {
            steps {
                script {
                    // Use Jenkins credentials for Docker login
                    withCredentials([usernamePassword(credentialsId: 'dockerHub', usernameVariable: 'USER', passwordVariable: 'PASSWD')]) {
                        sh 'echo $PASSWD | docker login -u $USER --password-stdin'

                        // Generate tag from the current Git commit hash
                        env.TAG = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        
                        // Tag and push the Docker image
                        sh """
                        docker image tag ${DOCKER_IMAGE} joakim077/springboot-application:${env.TAG}
                        docker push joakim077/springboot-application:${env.TAG}
                        """
                    }
                }
            }
        }
        
        stage('Modify Manifest') {
            steps {
                script {
                    sh """
                    sed 's/{{ image }}/joakim077\\/springboot-application:${env.TAG}/g' template/deploy.j2 > deploy/deploy.yaml
                    """
                }
            }
        }
        
        stage('Commit and Push Changes') {
            steps {
                
                    withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'GIT_PASS', usernameVariable: 'GIT_USER')]) {
                    sh '''
                    git add deploy/deploy.yaml
                    git commit -m "SKIP CI"
                    git remote set-url origin https://${GIT_PASS}@github.com/${GIT_USER}/Springboot-BankApplication.git
                    git push origin main
                    '''
                }
            }
        }
    }
}
