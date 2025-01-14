# Run Bank app in EKS Cluster

### Launch an EC2 instance and install AWS CLI, eksctl and kubectl
- Launch an Ubuntu EC2 instance of t2.medium size in AWS.
- Install aws cli
  ```bash
  curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"

  curl -o awscliv2.sig https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip.sig

  unzip awscliv2.zip

  sudo ./aws/install -i /usr/local/aws-cli -b /usr/local/bin

  # Confirm installation by running
  aws --version
  ```
- Configure aws cli 
  ```bash
    aws configure 
    # provide AWS Access key ID, AWS Secret Acccess key ID, default region and output format.

    # confirm installaation by running
    aws s3 ls
  ```

- Install eksctl
  ```bash
  # for ARM systems, set ARCH to: `arm64`, `armv6` or `armv7`
  ARCH=amd64
  PLATFORM=$(uname -s)_$ARCH

  curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$PLATFORM.tar.gz"

  # (Optional) Verify checksum
  curl -sL "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_checksums.txt" | grep $PLATFORM | sha256sum --check

  tar -xzf eksctl_$PLATFORM.tar.gz -C /tmp && rm eksctl_$PLATFORM.tar.gz

  sudo mv /tmp/eksctl /usr/local/bin
  ```

- Install kubectl
  ```bash
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"

  echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check

  sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

  kubectl version --client # check installation
  ```
### 1. EKS Cluster Creation and Configuration
- create cluster
  ```bash
  eksctl create cluster --name=mycluster \
                        --region=ap-south-1 \
                        --zones=ap-south-1a,ap-south-1b \
                        --without-nodegroup 
  ```
- associate oidc provider
  ```bash
  eksctl utils associate-iam-oidc-provider \
      --region ap-south-1 \
      --cluster mycluster \
      --approve
  ```
- create nodegroup and add nodes
  ```bash
  eksctl create nodegroup --cluster=mycluster \
                          --region=ap-south-1 \
                          --name=node-grp-1 \
                          --node-type=t2.medium \
                          --nodes-min=2 \
                          --nodes-max=3 \
                          --node-volume-size=20 \
                          --managed \
                          --asg-access \
                          --external-dns-access \
                          --full-ecr-access \
                          --appmesh-access \
                          --alb-ingress-access \
                          --node-private-networking
  ```

- Create IAM role for service account
  ```bash
  eksctl create iamserviceaccount \
      --name ebs-csi-controller-sa \
      --namespace kube-system \
      --cluster mycluster \
      --role-name AmazonEKS_EBS_CSI_DriverRole \
      --role-only \
      --attach-policy-arn arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy \
      --approve
  ```

- get ARN of IAM Role
  ```bash
  ARN=$(aws iam get-role --role-name AmazonEKS_EBS_CSI_DriverRole --query 'Role.Arn' --output text)
  ```
- Deploy EBS CSI Driver (Add on)
  ```bash
  eksctl create addon --cluster mycluster --name aws-ebs-csi-driver --version latest \
      --service-account-role-arn $ARN --force
  ```

### 2. Run Database

- Install MySQL Operator for Kubernetes

  - deploy the Custom Resource Definition (CRDs):
    ```bash
      kubectl apply -f https://raw.githubusercontent.com/mysql/mysql-operator/9.1.0-2.2.2/deploy/deploy-crds.yaml
    ```
  - deploy MySQL Operator for Kubernetes:
    ```bash
      kubectl apply -f https://raw.githubusercontent.com/mysql/mysql-operator/9.1.0-2.2.2/deploy/deploy-operator.yaml
    ```
  - Verify the operator is running by checking the deployment inside the mysql-operator namespace:
    ```bash
      kubectl get deployment -n mysql-operator mysql-operator
    ```
- Create cofigmap and secret
  ```bash    
      kubectl apply -f configmap.yaml

      kubectl apply -f db-secret.yaml
  ```
- Deploy  MySQL InnoDB Cluster
  ```bash
      kubectl apply -f innodbcluster.yaml
      # it will create 2 service.
      # clusterIP and headless.
  ```


- check 
  ```bash
    kubectl run --rm -it myshell --image=container-registry.oracle.com/mysql/community-operator -- mysqlsh

    MySQL JS>  \connect root@mycluster
    Creating a session to 'root@mycluster'
    Please provide the password for 'root@mycluster': ******
    MySQL mycluster JS>
  ```
- Exec into mycluster-0 pod and create DB
  ```bash
    kubectl exec -it mycluster-0 -- mysqlsh
    \connect root@localhost
    # provide password
    create database BankDB;
  ```
**Note** enter ctrl + d to exit from mysqlsh

### 3. Deploy application

Deploy application
  ```bash
  kubectl apply -f app-deployment.yaml

  kubectl apply -f app-service.yaml
  ```

### 4. Cert Manager

- Install Cert Manager into your Kubernetes cluster.
  ```bash
  kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.5/cert-manager.yaml
  ```
- Create Cluster Issuer.
  ```bash
  kubectl apply -f cluster_issuer.yaml
  ```

### 5 .Ingress
- Install Nginx Ingress Controller
  ```bash
  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml

  kubectl get svc -n ingress-nginx

  # It will create a LoadBlancer, map the expernal IP/DNS of LoadBalancer with your domain
  ```
- Create Certificate
  ```bash
  kubectl apply -f certificate
  ```
- Apply Ingress Resource Manifest
  ```bash
  kubectl apply -f ingress.yaml
  ```

### 6. Access application
Access application to URL: <Your_Domain>
```url
https://bank.joakim.online
```

### 7. HPA and Load Testing

- Install Metrics Server
  ```bash
  kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
  ```

- Create HPA 
  ```bash
  kubectl apply -f hpa.yaml
  ```

- Install Grafana K6 for load testing.
  ```bash
  sudo gpg -k
  sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
  echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update
  sudo apt-get install k6
  ```

- Generate load to application
  ```bash
  k6 run load.js
  ```

For detailed CI/CD guide, refer to the [CI/CD Guideline for the Project](cicd.md).
