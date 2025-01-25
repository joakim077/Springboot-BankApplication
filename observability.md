# Observability

### Install helm
```bash
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

### Install Kube prometheus stack
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install kube-prometheus-stack prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace
```

### Apply Sevice Monitor
```bash
kubectl apply -f servicemonitor.yaml
```

### Getting Grafana login secret for admin user
```bash 
kubectl get secret --namespace monitoring kube-prometheus-stack-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
kubectl port-forward svc/kube-prometheus-stack-grafana 3000:80 -n monitoring --address=0.0.0.0
```

Access the grafana and create Dashboards.
