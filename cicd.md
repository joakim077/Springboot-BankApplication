# Countinous Integration using Jenkins and Continous Delivery using ArgoCD
### 1. Install Jenkins
Install Jenkins on the EC2 instance.
- Install Java 
    ```bash
    sudo apt-get install openjdk-17-jre
    ```
- Install Jenkins
    ```bash
    sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
    https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
    echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
    https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
    /etc/apt/sources.list.d/jenkins.list > /dev/null
    sudo apt-get update
    sudo apt-get install jenkins
    ```

- Start and Enable Jenkins
    ```bash
    sudo systemctl start jenkins && sudo systemctl enable jenkins
    ```

- Login Into Jenkins and Install Suggested Plugins

### 2. Add Docker and GitHub Credentials to Jenkins Secrets
Dashboard > Manage Jenkins > Credentials > System > Global credentials (unrestricted)

- GitHub: Create Personal Access TOKEN which is used for pull and push chages.
- DockerHub: Create TOKEN which will be used for push Image.

### 3. Install docker and Trivy
- Install docker and add jenkins user into docker group
    ```bash
    sudo apt-get update and && sudo apt-get install docker.io

    sudo usermod -aG docker jenkins && newgrp docker
    ```
- Trivy
    - Install on Machine
    ```bash
    sudo apt-get install wget apt-transport-https gnupg lsb-release

    wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | sudo tee /usr/share/keyrings/trivy.gpg > /dev/null

    echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list

    sudo apt-get update

    sudo apt-get install trivy -y
    ``` 
### 4. Create Pipeline
- Name
- GitHub Project
- Pipeline from SCM

### Pipeline 
- Checkout: clone the code from GitHub.
- Build: Building a docker image
- Trivy Image Vulnerability Scan: Scanning build docker image.
- Tag Image and Push: Create tag for docker image from SHA of last commit and push into dockerhub.
- Modify Manifest: update the image tag i file file deploy/deploy.yaml.
- Commit and Push Changes: push into GitHub so that ArgoCD which is monitoring that folder can make changes into cluster.


- Install ArgoCD on cluster
  ```bash
  kubectl create namespace argocd
  kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
  ```

- Access 
  ```bash
	kubectl port-forward svc/argocd-server -n argocd 8081:80 --address=0.0.0.0
  ```

- Password
  ```bash
  kubectl get secret -n argocd argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d ; echo
  ```

- Create Application







For detailed Observability guide, refer to the [Observability Guideline for the Project](observability.md).
