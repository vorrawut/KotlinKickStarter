# 🚀 Lesson 19: Cloud Deployment - Concept Guide

## 🎯 Learning Objectives

By the end of this lesson, you will:
- **Deploy to cloud platforms** using Infrastructure as Code with Terraform and cloud-native services
- **Orchestrate with Kubernetes** in production environments with auto-scaling and load balancing
- **Implement monitoring and observability** with cloud-native tools and distributed tracing
- **Configure security and compliance** with cloud security best practices and governance
- **Optimize costs and performance** with resource monitoring, scaling policies, and cost management
- **Design disaster recovery** with multi-region deployment and backup strategies

---

## 🔍 Why Cloud Deployment Matters

### **The Cloud-Native Revolution**
```yaml
# Traditional On-Premises: Manual, Fixed Infrastructure
Physical Servers → Manual Provisioning → Static Scaling → Manual Monitoring
- Weeks to provision infrastructure
- Fixed capacity regardless of demand
- Manual scaling and maintenance
- Limited disaster recovery options

# Cloud-Native: Automated, Elastic Infrastructure
IaC Templates → Auto Provisioning → Dynamic Scaling → Intelligent Monitoring
- Minutes to provision globally
- Pay-per-use elastic scaling
- Automated operations and healing
- Built-in disaster recovery and backup
```

### **Business Benefits**
- **Global Reach**: Deploy applications worldwide with low latency
- **Elastic Scaling**: Handle traffic spikes automatically without over-provisioning
- **Cost Efficiency**: Pay only for resources used with automatic optimization
- **High Availability**: 99.99% uptime with multi-region disaster recovery
- **Security & Compliance**: Enterprise-grade security with automated compliance

---

## ☁️ Infrastructure as Code with Terraform

### **Cloud Infrastructure Foundation**
```hcl
# terraform/main.tf - Complete cloud infrastructure
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.10"
    }
  }
  
  backend "s3" {
    bucket = "kotlin-kickstarter-terraform-state"
    key    = "infrastructure/terraform.tfstate"
    region = "us-west-2"
  }
}

# Configure AWS Provider
provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = "KotlinKickStarter"
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = var.owner_email
    }
  }
}

# VPC and Networking
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  
  tags = {
    Name = "${var.project_name}-vpc-${var.environment}"
  }
}

# Public Subnets for Load Balancers
resource "aws_subnet" "public" {
  count = length(var.availability_zones)
  
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true
  
  tags = {
    Name = "${var.project_name}-public-${var.availability_zones[count.index]}"
    Type = "Public"
    "kubernetes.io/role/elb" = "1"
  }
}

# Private Subnets for Application Workloads
resource "aws_subnet" "private" {
  count = length(var.availability_zones)
  
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]
  
  tags = {
    Name = "${var.project_name}-private-${var.availability_zones[count.index]}"
    Type = "Private"
    "kubernetes.io/role/internal-elb" = "1"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  
  tags = {
    Name = "${var.project_name}-igw"
  }
}

# NAT Gateways for Private Subnet Internet Access
resource "aws_eip" "nat" {
  count  = length(aws_subnet.public)
  domain = "vpc"
  
  tags = {
    Name = "${var.project_name}-nat-eip-${count.index + 1}"
  }
}

resource "aws_nat_gateway" "main" {
  count = length(aws_subnet.public)
  
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id
  
  tags = {
    Name = "${var.project_name}-nat-${count.index + 1}"
  }
  
  depends_on = [aws_internet_gateway.main]
}

# Route Tables
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  
  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

resource "aws_route_table" "private" {
  count  = length(aws_nat_gateway.main)
  vpc_id = aws_vpc.main.id
  
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main[count.index].id
  }
  
  tags = {
    Name = "${var.project_name}-private-rt-${count.index + 1}"
  }
}

# Route Table Associations
resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)
  
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  count = length(aws_subnet.private)
  
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}
```

### **EKS Cluster Configuration**
```hcl
# terraform/eks.tf - Managed Kubernetes cluster
resource "aws_eks_cluster" "main" {
  name     = "${var.project_name}-cluster-${var.environment}"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = var.kubernetes_version
  
  vpc_config {
    subnet_ids              = concat(aws_subnet.private[*].id, aws_subnet.public[*].id)
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = var.cluster_endpoint_public_access_cidrs
  }
  
  encryption_config {
    provider {
      key_arn = aws_kms_key.eks.arn
    }
    resources = ["secrets"]
  }
  
  enabled_cluster_log_types = ["api", "audit", "authenticator", "controllerManager", "scheduler"]
  
  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_AmazonEKSClusterPolicy,
    aws_cloudwatch_log_group.eks_cluster,
  ]
  
  tags = {
    Name = "${var.project_name}-eks-cluster"
  }
}

# EKS Node Group
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.project_name}-node-group"
  node_role_arn   = aws_iam_role.eks_node_group.arn
  subnet_ids      = aws_subnet.private[*].id
  
  instance_types = var.node_instance_types
  ami_type       = "AL2_x86_64"
  capacity_type  = "ON_DEMAND"
  disk_size      = var.node_disk_size
  
  scaling_config {
    desired_size = var.node_desired_size
    max_size     = var.node_max_size
    min_size     = var.node_min_size
  }
  
  update_config {
    max_unavailable_percentage = 25
  }
  
  remote_access {
    ec2_ssh_key               = var.node_ssh_key
    source_security_group_ids = [aws_security_group.node_group_ssh.id]
  }
  
  labels = {
    Environment = var.environment
    NodeGroup   = "main"
  }
  
  taint {
    key    = "dedicated"
    value  = "application"
    effect = "NO_SCHEDULE"
  }
  
  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_container_registry_policy,
  ]
  
  tags = {
    Name = "${var.project_name}-node-group"
  }
}

# Security Groups
resource "aws_security_group" "eks_cluster" {
  name_prefix = "${var.project_name}-cluster-"
  vpc_id      = aws_vpc.main.id
  
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.cluster_endpoint_public_access_cidrs
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = {
    Name = "${var.project_name}-cluster-sg"
  }
}

resource "aws_security_group" "node_group" {
  name_prefix = "${var.project_name}-node-"
  vpc_id      = aws_vpc.main.id
  
  ingress {
    description = "Node to node all ports/protocols"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }
  
  ingress {
    description     = "Cluster API to node groups"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_cluster.id]
  }
  
  ingress {
    description     = "Cluster API to node kubelets"
    from_port       = 10250
    to_port         = 10250
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_cluster.id]
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = {
    Name = "${var.project_name}-node-sg"
  }
}
```

### **Application Load Balancer & Auto Scaling**
```hcl
# terraform/alb.tf - Application Load Balancer
resource "aws_lb" "main" {
  name               = "${var.project_name}-alb-${var.environment}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
  
  enable_deletion_protection       = var.environment == "production"
  enable_cross_zone_load_balancing = true
  enable_http2                     = true
  
  access_logs {
    bucket  = aws_s3_bucket.alb_logs.id
    prefix  = "alb-logs"
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-alb"
  }
}

# ALB Target Group
resource "aws_lb_target_group" "app" {
  name     = "${var.project_name}-app-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id
  
  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 10
    interval            = 30
    path                = "/actuator/health/readiness"
    matcher             = "200"
    port                = "traffic-port"
    protocol            = "HTTP"
  }
  
  stickiness {
    type            = "lb_cookie"
    cookie_duration = 86400
    enabled         = true
  }
  
  tags = {
    Name = "${var.project_name}-app-tg"
  }
}

# ALB Listener with SSL
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2019-07"
  certificate_arn   = aws_acm_certificate_validation.main.certificate_arn
  
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# Redirect HTTP to HTTPS
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"
  
  default_action {
    type = "redirect"
    
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# Auto Scaling Group for additional capacity
resource "aws_autoscaling_group" "app" {
  name                = "${var.project_name}-asg-${var.environment}"
  vpc_zone_identifier = aws_subnet.private[*].id
  target_group_arns   = [aws_lb_target_group.app.arn]
  health_check_type   = "ELB"
  health_check_grace_period = 300
  
  min_size         = var.asg_min_size
  max_size         = var.asg_max_size
  desired_capacity = var.asg_desired_capacity
  
  launch_template {
    id      = aws_launch_template.app.id
    version = "$Latest"
  }
  
  tag {
    key                 = "Name"
    value               = "${var.project_name}-asg-instance"
    propagate_at_launch = true
  }
  
  tag {
    key                 = "Environment"
    value               = var.environment
    propagate_at_launch = true
  }
  
  instance_refresh {
    strategy = "Rolling"
    preferences {
      min_healthy_percentage = 50
    }
  }
}

# Auto Scaling Policies
resource "aws_autoscaling_policy" "scale_up" {
  name                   = "${var.project_name}-scale-up"
  scaling_adjustment     = 2
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.app.name
}

resource "aws_autoscaling_policy" "scale_down" {
  name                   = "${var.project_name}-scale-down"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.app.name
}

# CloudWatch Alarms for Auto Scaling
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "${var.project_name}-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "75"
  alarm_description   = "This metric monitors ec2 cpu utilization"
  alarm_actions       = [aws_autoscaling_policy.scale_up.arn]
  
  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.app.name
  }
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "${var.project_name}-cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "25"
  alarm_description   = "This metric monitors ec2 cpu utilization"
  alarm_actions       = [aws_autoscaling_policy.scale_down.arn]
  
  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.app.name
  }
}
```

---

## 🎛️ Kubernetes Production Deployment

### **Production-Ready Kubernetes Manifests**
```yaml
# k8s/namespace.yml
apiVersion: v1
kind: Namespace
metadata:
  name: kotlin-kickstarter
  labels:
    name: kotlin-kickstarter
    environment: production
    istio-injection: enabled
---
# k8s/configmap.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: kotlin-kickstarter
data:
  application.yml: |
    spring:
      profiles:
        active: production
      datasource:
        url: jdbc:postgresql://postgres-service:5432/proddb
        username: ${DATABASE_USERNAME}
        password: ${DATABASE_PASSWORD}
      data:
        redis:
          host: redis-service
          port: 6379
          password: ${REDIS_PASSWORD}
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: when-authorized
      metrics:
        export:
          prometheus:
            enabled: true
    
    logging:
      level:
        com.learning: INFO
        org.springframework.security: WARN
      pattern:
        console: "%d{ISO8601} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"

---
# k8s/secret.yml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: kotlin-kickstarter
type: Opaque
data:
  database-username: cHJvZHVzZXI=  # Base64 encoded
  database-password: c3VwZXJzZWNyZXRwYXNzd29yZA==
  redis-password: cmVkaXNzZWNyZXRwYXNz
  jwt-secret: am9reHNlY3JldGtleWZvcnByb2R1Y3Rpb25qd3Q=

---
# k8s/deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kotlin-kickstarter
  namespace: kotlin-kickstarter
  labels:
    app: kotlin-kickstarter
    version: v1
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 2
  selector:
    matchLabels:
      app: kotlin-kickstarter
  template:
    metadata:
      labels:
        app: kotlin-kickstarter
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/path: "/actuator/prometheus"
        prometheus.io/port: "8080"
    spec:
      serviceAccountName: kotlin-kickstarter
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
      containers:
      - name: app
        image: ghcr.io/your-org/kotlin-kickstarter:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: jwt-secret
        - name: JVM_OPTS
          value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 90
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: app-config
          mountPath: /app/config
          readOnly: true
      volumes:
      - name: tmp
        emptyDir: {}
      - name: app-config
        configMap:
          name: app-config
      nodeSelector:
        kubernetes.io/arch: amd64
      tolerations:
      - key: "dedicated"
        operator: "Equal"
        value: "application"
        effect: "NoSchedule"
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - kotlin-kickstarter
              topologyKey: kubernetes.io/hostname

---
# k8s/service.yml
apiVersion: v1
kind: Service
metadata:
  name: kotlin-kickstarter-service
  namespace: kotlin-kickstarter
  labels:
    app: kotlin-kickstarter
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
    service.beta.kubernetes.io/aws-load-balancer-backend-protocol: "http"
spec:
  type: LoadBalancer
  selector:
    app: kotlin-kickstarter
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
    name: http
  sessionAffinity: ClientIP
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 10800

---
# k8s/hpa.yml - Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: kotlin-kickstarter-hpa
  namespace: kotlin-kickstarter
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: kotlin-kickstarter
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 4
        periodSeconds: 15
      selectPolicy: Max
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
      selectPolicy: Min

---
# k8s/pdb.yml - Pod Disruption Budget
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: kotlin-kickstarter-pdb
  namespace: kotlin-kickstarter
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: kotlin-kickstarter

---
# k8s/network-policy.yml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kotlin-kickstarter-netpol
  namespace: kotlin-kickstarter
spec:
  podSelector:
    matchLabels:
      app: kotlin-kickstarter
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    - podSelector:
        matchLabels:
          app: prometheus
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
  - to:
    - podSelector:
        matchLabels:
          app: postgresql
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
```

### **Helm Chart for Application Deployment**
```yaml
# helm/kotlin-kickstarter/Chart.yaml
apiVersion: v2
name: kotlin-kickstarter
description: A Helm chart for KotlinKickStarter application
type: application
version: 0.1.0
appVersion: "1.0.0"

dependencies:
- name: postgresql
  version: "12.1.9"
  repository: "https://charts.bitnami.com/bitnami"
  condition: postgresql.enabled
- name: redis
  version: "17.9.3"
  repository: "https://charts.bitnami.com/bitnami"
  condition: redis.enabled

---
# helm/kotlin-kickstarter/values.yaml
# Default values for kotlin-kickstarter
replicaCount: 3

image:
  repository: ghcr.io/your-org/kotlin-kickstarter
  pullPolicy: IfNotPresent
  tag: "latest"

nameOverride: ""
fullnameOverride: ""

serviceAccount:
  create: true
  annotations: {}
  name: ""

podAnnotations:
  prometheus.io/scrape: "true"
  prometheus.io/path: "/actuator/prometheus"
  prometheus.io/port: "8080"

podSecurityContext:
  fsGroup: 1001
  runAsNonRoot: true
  runAsUser: 1001

securityContext:
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
    - ALL

service:
  type: ClusterIP
  port: 80
  targetPort: 8080

ingress:
  enabled: true
  className: "nginx"
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
  hosts:
  - host: api.kotlinkickstarter.com
    paths:
    - path: /
      pathType: Prefix
  tls:
  - secretName: kotlin-kickstarter-tls
    hosts:
    - api.kotlinkickstarter.com

resources:
  limits:
    cpu: 500m
    memory: 1Gi
  requests:
    cpu: 250m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

nodeSelector: {}

tolerations: []

affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app.kubernetes.io/name
            operator: In
            values:
            - kotlin-kickstarter
        topologyKey: kubernetes.io/hostname

# Application configuration
app:
  environment: production
  jvmOpts: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
  
  config:
    logLevel: INFO
    
  secrets:
    databasePassword: "supersecretpassword"
    redisPassword: "redissecretpass"
    jwtSecret: "jwtsecretkeyforproductionjwt"

# PostgreSQL dependency
postgresql:
  enabled: true
  auth:
    postgresPassword: "postgres"
    username: "produser"
    password: "supersecretpassword"
    database: "proddb"
  primary:
    persistence:
      enabled: true
      size: 20Gi
    resources:
      requests:
        memory: 256Mi
        cpu: 250m
      limits:
        memory: 512Mi
        cpu: 500m

# Redis dependency
redis:
  enabled: true
  auth:
    enabled: true
    password: "redissecretpass"
  master:
    persistence:
      enabled: true
      size: 8Gi
    resources:
      requests:
        memory: 256Mi
        cpu: 100m
      limits:
        memory: 512Mi
        cpu: 200m
```

---

## 📊 Cloud Monitoring & Observability

### **Prometheus and Grafana Setup**
```yaml
# monitoring/prometheus.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: monitoring
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      serviceAccountName: prometheus
      containers:
      - name: prometheus
        image: prom/prometheus:v2.45.0
        args:
        - '--config.file=/etc/prometheus/prometheus.yml'
        - '--storage.tsdb.path=/prometheus/'
        - '--web.console.libraries=/etc/prometheus/console_libraries'
        - '--web.console.templates=/etc/prometheus/consoles'
        - '--storage.tsdb.retention.time=30d'
        - '--web.enable-lifecycle'
        - '--web.enable-admin-api'
        ports:
        - containerPort: 9090
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 1000m
            memory: 2Gi
        volumeMounts:
        - name: prometheus-config
          mountPath: /etc/prometheus/
        - name: prometheus-storage
          mountPath: /prometheus/
      volumes:
      - name: prometheus-config
        configMap:
          name: prometheus-config
      - name: prometheus-storage
        persistentVolumeClaim:
          claimName: prometheus-pvc

---
# monitoring/prometheus-config.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
    
    rule_files:
    - "/etc/prometheus/rules/*.yml"
    
    alerting:
      alertmanagers:
      - static_configs:
        - targets:
          - alertmanager:9093
    
    scrape_configs:
    # Kubernetes API Server
    - job_name: 'kubernetes-apiservers'
      kubernetes_sd_configs:
      - role: endpoints
      scheme: https
      tls_config:
        ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
      bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
      relabel_configs:
      - source_labels: [__meta_kubernetes_namespace, __meta_kubernetes_service_name, __meta_kubernetes_endpoint_port_name]
        action: keep
        regex: default;kubernetes;https
    
    # Kubernetes Nodes
    - job_name: 'kubernetes-nodes'
      kubernetes_sd_configs:
      - role: node
      scheme: https
      tls_config:
        ca_file: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
      bearer_token_file: /var/run/secrets/kubernetes.io/serviceaccount/token
      relabel_configs:
      - action: labelmap
        regex: __meta_kubernetes_node_label_(.+)
    
    # Kubernetes Pods
    - job_name: 'kubernetes-pods'
      kubernetes_sd_configs:
      - role: pod
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__
      - action: labelmap
        regex: __meta_kubernetes_pod_label_(.+)
      - source_labels: [__meta_kubernetes_namespace]
        action: replace
        target_label: kubernetes_namespace
      - source_labels: [__meta_kubernetes_pod_name]
        action: replace
        target_label: kubernetes_pod_name
    
    # Application specific scraping
    - job_name: 'kotlin-kickstarter'
      kubernetes_sd_configs:
      - role: endpoints
        namespaces:
          names:
          - kotlin-kickstarter
      relabel_configs:
      - source_labels: [__meta_kubernetes_service_name]
        action: keep
        regex: kotlin-kickstarter-service
      - source_labels: [__meta_kubernetes_endpoint_port_name]
        action: keep
        regex: http
      - source_labels: [__address__]
        action: replace
        target_label: __address__
        regex: ([^:]+)(?::\d+)?
        replacement: $1:8080
      - source_labels: [__meta_kubernetes_service_name]
        action: replace
        target_label: job
      - source_labels: [__meta_kubernetes_namespace]
        action: replace
        target_label: namespace
      metrics_path: /actuator/prometheus

---
# monitoring/grafana-dashboard.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kotlin-kickstarter-dashboard
  namespace: monitoring
  labels:
    grafana_dashboard: "1"
data:
  kotlin-kickstarter.json: |
    {
      "dashboard": {
        "id": null,
        "title": "KotlinKickStarter Application Metrics",
        "tags": ["kotlin", "spring-boot", "application"],
        "timezone": "browser",
        "panels": [
          {
            "id": 1,
            "title": "HTTP Request Rate",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(http_server_requests_total{job=\"kotlin-kickstarter\"}[5m])",
                "legendFormat": "{{method}} {{uri}}"
              }
            ],
            "yAxes": [
              {
                "label": "Requests/sec",
                "min": 0
              }
            ]
          },
          {
            "id": 2,
            "title": "HTTP Request Duration",
            "type": "graph",
            "targets": [
              {
                "expr": "histogram_quantile(0.95, rate(http_server_requests_duration_seconds_bucket{job=\"kotlin-kickstarter\"}[5m]))",
                "legendFormat": "95th percentile"
              },
              {
                "expr": "histogram_quantile(0.50, rate(http_server_requests_duration_seconds_bucket{job=\"kotlin-kickstarter\"}[5m]))",
                "legendFormat": "50th percentile"
              }
            ],
            "yAxes": [
              {
                "label": "Duration (seconds)",
                "min": 0
              }
            ]
          },
          {
            "id": 3,
            "title": "JVM Memory Usage",
            "type": "graph",
            "targets": [
              {
                "expr": "jvm_memory_used_bytes{job=\"kotlin-kickstarter\"}",
                "legendFormat": "{{area}} - {{id}}"
              }
            ]
          },
          {
            "id": 4,
            "title": "Database Connection Pool",
            "type": "graph",
            "targets": [
              {
                "expr": "hikaricp_connections_active{job=\"kotlin-kickstarter\"}",
                "legendFormat": "Active Connections"
              },
              {
                "expr": "hikaricp_connections_idle{job=\"kotlin-kickstarter\"}",
                "legendFormat": "Idle Connections"
              }
            ]
          }
        ],
        "time": {
          "from": "now-1h",
          "to": "now"
        },
        "refresh": "30s"
      }
    }
```

### **AWS CloudWatch Integration**
```yaml
# monitoring/cloudwatch-config.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cloudwatch-config
  namespace: monitoring
data:
  cloudwatch-config.json: |
    {
      "logs": {
        "logs_collected": {
          "kubernetes": {
            "cluster_name": "kotlin-kickstarter-cluster",
            "log_group_name": "/aws/eks/kotlin-kickstarter/cluster",
            "log_stream_name": "{hostname}",
            "multiline_start_pattern": "{timestamp_regex}",
            "timezone": "UTC"
          }
        },
        "log_streams": [
          {
            "file_path": "/var/log/containers/kotlin-kickstarter-*.log",
            "log_group_name": "/aws/eks/kotlin-kickstarter/application",
            "log_stream_name": "{ip_address}",
            "multiline_start_pattern": "^\\d{4}-\\d{2}-\\d{2}",
            "timezone": "UTC"
          }
        ]
      },
      "metrics": {
        "namespace": "EKS/Application",
        "metrics_collected": {
          "cpu": {
            "measurement": [
              "cpu_usage_idle",
              "cpu_usage_iowait",
              "cpu_usage_user",
              "cpu_usage_system"
            ],
            "metrics_collection_interval": 60
          },
          "disk": {
            "measurement": [
              "used_percent"
            ],
            "metrics_collection_interval": 60,
            "resources": [
              "*"
            ]
          },
          "diskio": {
            "measurement": [
              "io_time"
            ],
            "metrics_collection_interval": 60,
            "resources": [
              "*"
            ]
          },
          "mem": {
            "measurement": [
              "mem_used_percent"
            ],
            "metrics_collection_interval": 60
          }
        }
      }
    }

---
# monitoring/fluent-bit.yml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluent-bit
  namespace: monitoring
  labels:
    k8s-app: fluent-bit
spec:
  selector:
    matchLabels:
      k8s-app: fluent-bit
  template:
    metadata:
      labels:
        k8s-app: fluent-bit
    spec:
      serviceAccountName: fluent-bit
      tolerations:
      - key: node-role.kubernetes.io/master
        operator: Exists
        effect: NoSchedule
      containers:
      - name: fluent-bit
        image: fluent/fluent-bit:2.1.8
        env:
        - name: AWS_REGION
          value: "us-west-2"
        - name: CLUSTER_NAME
          value: "kotlin-kickstarter-cluster"
        resources:
          limits:
            memory: 200Mi
            cpu: 100m
          requests:
            cpu: 100m
            memory: 200Mi
        volumeMounts:
        - name: varlog
          mountPath: /var/log
        - name: varlibdockercontainers
          mountPath: /var/lib/docker/containers
          readOnly: true
        - name: fluent-bit-config
          mountPath: /fluent-bit/etc/
      volumes:
      - name: varlog
        hostPath:
          path: /var/log
      - name: varlibdockercontainers
        hostPath:
          path: /var/lib/docker/containers
      - name: fluent-bit-config
        configMap:
          name: fluent-bit-config
```

---

## 🔒 Security & Compliance

### **Pod Security Standards**
```yaml
# security/pod-security-policy.yml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: kotlin-kickstarter-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  hostNetwork: false
  hostIPC: false
  hostPID: false
  runAsUser:
    rule: 'MustRunAsNonRoot'
  supplementalGroups:
    rule: 'MustRunAs'
    ranges:
      - min: 1
        max: 65535
  fsGroup:
    rule: 'MustRunAs'
    ranges:
      - min: 1
        max: 65535
  readOnlyRootFilesystem: true

---
# security/network-policy.yml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: kotlin-kickstarter
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress

---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-kotlin-kickstarter
  namespace: kotlin-kickstarter
spec:
  podSelector:
    matchLabels:
      app: kotlin-kickstarter
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    - podSelector:
        matchLabels:
          app: prometheus
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to: []
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
  - to:
    - podSelector:
        matchLabels:
          app: postgresql
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379

---
# security/rbac.yml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: kotlin-kickstarter
  namespace: kotlin-kickstarter

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: kotlin-kickstarter
  name: kotlin-kickstarter-role
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: kotlin-kickstarter-rolebinding
  namespace: kotlin-kickstarter
subjects:
- kind: ServiceAccount
  name: kotlin-kickstarter
  namespace: kotlin-kickstarter
roleRef:
  kind: Role
  name: kotlin-kickstarter-role
  apiGroup: rbac.authorization.k8s.io

---
# security/admission-controller.yml
apiVersion: admissionregistration.k8s.io/v1
kind: ValidatingAdmissionWebhook
metadata:
  name: security-policy-webhook
webhooks:
- name: security.example.com
  clientConfig:
    service:
      name: security-webhook
      namespace: kube-system
      path: "/validate"
  rules:
  - operations: ["CREATE", "UPDATE"]
    apiGroups: ["apps"]
    apiVersions: ["v1"]
    resources: ["deployments"]
  admissionReviewVersions: ["v1", "v1beta1"]
  sideEffects: None
  failurePolicy: Fail
```

---

## 💰 Cost Optimization & Management

### **Cost Monitoring with AWS Cost Explorer**
```yaml
# cost-optimization/cost-budget.yml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'Cost budget and monitoring for KotlinKickStarter'

Resources:
  CostBudget:
    Type: AWS::Budgets::Budget
    Properties:
      Budget:
        BudgetName: 'KotlinKickStarter-Monthly-Budget'
        BudgetLimit:
          Amount: 500
          Unit: USD
        TimeUnit: MONTHLY
        BudgetType: COST
        CostFilters:
          TagKey:
            - 'Project'
          TagValue:
            - 'KotlinKickStarter'
      NotificationsWithSubscribers:
        - Notification:
            NotificationType: ACTUAL
            ComparisonOperator: GREATER_THAN
            Threshold: 80
            ThresholdType: PERCENTAGE
          Subscribers:
            - SubscriptionType: EMAIL
              Address: 'devops@company.com'
        - Notification:
            NotificationType: FORECASTED
            ComparisonOperator: GREATER_THAN
            Threshold: 100
            ThresholdType: PERCENTAGE
          Subscribers:
            - SubscriptionType: EMAIL
              Address: 'devops@company.com'

  CostAnomalyDetector:
    Type: AWS::CE::AnomalyDetector
    Properties:
      AnomalyDetectorName: 'KotlinKickStarter-Anomaly-Detector'
      MonitorType: 'DIMENSIONAL'
      MonitorSpecification: |
        {
          "DimensionKey": "SERVICE",
          "DimensionValueList": ["Amazon Elastic Kubernetes Service", "Amazon EC2-Instance"],
          "MatchOptions": ["EQUALS"]
        }

  CostAnomalySubscription:
    Type: AWS::CE::AnomalySubscription
    Properties:
      SubscriptionName: 'KotlinKickStarter-Anomaly-Subscription'
      MonitorArnList:
        - !GetAtt CostAnomalyDetector.AnomalyDetectorArn
      Subscribers:
        - Address: 'devops@company.com'
          Type: 'EMAIL'
      Frequency: 'DAILY'
      ThresholdExpression: |
        {
          "And": [
            {
              "Dimensions": {
                "Key": "ANOMALY_TOTAL_IMPACT_ABSOLUTE",
                "Values": ["100"]
              }
            }
          ]
        }
```

### **Resource Optimization Policies**
```yaml
# cost-optimization/cluster-autoscaler.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cluster-autoscaler
  namespace: kube-system
  labels:
    app: cluster-autoscaler
spec:
  selector:
    matchLabels:
      app: cluster-autoscaler
  template:
    metadata:
      labels:
        app: cluster-autoscaler
    spec:
      serviceAccountName: cluster-autoscaler
      containers:
      - image: k8s.gcr.io/autoscaling/cluster-autoscaler:v1.27.1
        name: cluster-autoscaler
        resources:
          limits:
            cpu: 100m
            memory: 300Mi
          requests:
            cpu: 100m
            memory: 300Mi
        command:
        - ./cluster-autoscaler
        - --v=4
        - --stderrthreshold=info
        - --cloud-provider=aws
        - --skip-nodes-with-local-storage=false
        - --expander=least-waste
        - --node-group-auto-discovery=asg:tag=k8s.io/cluster-autoscaler/enabled,k8s.io/cluster-autoscaler/kotlin-kickstarter-cluster
        - --balance-similar-node-groups
        - --scale-down-enabled=true
        - --scale-down-delay-after-add=10m
        - --scale-down-unneeded-time=10m
        - --scale-down-utilization-threshold=0.5
        - --skip-nodes-with-system-pods=false
        env:
        - name: AWS_REGION
          value: us-west-2
        volumeMounts:
        - name: ssl-certs
          mountPath: /etc/ssl/certs/ca-certificates.crt
          readOnly: true
      volumes:
      - name: ssl-certs
        hostPath:
          path: "/etc/ssl/certs/ca-bundle.crt"

---
# cost-optimization/vertical-pod-autoscaler.yml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: kotlin-kickstarter-vpa
  namespace: kotlin-kickstarter
spec:
  targetRef:
    apiVersion: "apps/v1"
    kind: Deployment
    name: kotlin-kickstarter
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: app
      maxAllowed:
        cpu: 1000m
        memory: 2Gi
      minAllowed:
        cpu: 100m
        memory: 256Mi
      controlledResources: ["cpu", "memory"]
```

---

## 🎯 Best Practices

### **Do's**
- ✅ **Use Infrastructure as Code** for reproducible deployments
- ✅ **Implement auto-scaling** for cost efficiency and reliability
- ✅ **Monitor everything** with comprehensive observability
- ✅ **Secure by default** with least privilege and network policies
- ✅ **Plan for disaster recovery** with multi-region strategies
- ✅ **Optimize costs** with resource monitoring and right-sizing
- ✅ **Use managed services** when possible to reduce operational overhead

### **Don'ts**
- ❌ **Don't deploy without monitoring** - observability is essential
- ❌ **Don't ignore security** - implement defense in depth
- ❌ **Don't over-provision** - use auto-scaling and monitoring to right-size
- ❌ **Don't forget disaster recovery** - plan for failures
- ❌ **Don't skip cost monitoring** - cloud costs can escalate quickly
- ❌ **Don't hardcode configurations** - use configuration management
- ❌ **Don't neglect compliance** - implement governance from the start

### **Cloud-Native Principles**
- 🌟 **Design for Failure**: Assume components will fail and design resilience
- 🌟 **Automate Everything**: Infrastructure, deployment, scaling, recovery
- 🌟 **Monitor and Observe**: Comprehensive visibility into system behavior
- 🌟 **Scale Elastically**: Automatically adapt to demand changes
- 🌟 **Secure by Design**: Security integrated throughout the stack

---

## 🚀 Configuration Best Practices

### **Environment Management**
```hcl
# terraform/environments/production/terraform.tfvars
aws_region = "us-west-2"
environment = "production"
project_name = "kotlin-kickstarter"

# Network Configuration
vpc_cidr = "10.0.0.0/16"
availability_zones = ["us-west-2a", "us-west-2b", "us-west-2c"]
public_subnet_cidrs = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
private_subnet_cidrs = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]

# EKS Configuration
kubernetes_version = "1.27"
node_instance_types = ["t3.large", "t3.xlarge"]
node_desired_size = 3
node_min_size = 3
node_max_size = 20
node_disk_size = 50

# Auto Scaling Configuration
asg_min_size = 0
asg_max_size = 10
asg_desired_capacity = 3

# Security
cluster_endpoint_public_access_cidrs = ["0.0.0.0/0"]
node_ssh_key = "production-key"

# Tagging
owner_email = "devops@company.com"
```

### **Deployment Scripts**
```bash
#!/bin/bash
# scripts/deploy-to-cloud.sh

set -e

ENVIRONMENT=${1:-staging}
AWS_REGION=${2:-us-west-2}
CLUSTER_NAME="kotlin-kickstarter-cluster-${ENVIRONMENT}"

echo "🚀 Deploying to ${ENVIRONMENT} environment in ${AWS_REGION}"

# Step 1: Deploy Infrastructure
echo "📦 Deploying infrastructure with Terraform..."
cd terraform/environments/${ENVIRONMENT}
terraform init
terraform plan
terraform apply -auto-approve

# Step 2: Configure kubectl
echo "🔧 Configuring kubectl..."
aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}

# Step 3: Deploy Application
echo "🚢 Deploying application with Helm..."
cd ../../../helm
helm upgrade --install kotlin-kickstarter ./kotlin-kickstarter \
  --namespace kotlin-kickstarter \
  --create-namespace \
  --values values-${ENVIRONMENT}.yaml \
  --wait \
  --timeout 10m

# Step 4: Verify Deployment
echo "✅ Verifying deployment..."
kubectl get pods -n kotlin-kickstarter
kubectl get services -n kotlin-kickstarter

# Step 5: Run Health Checks
echo "🏥 Running health checks..."
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=kotlin-kickstarter -n kotlin-kickstarter --timeout=300s

echo "✅ Deployment completed successfully!"
```

---

## 🎓 Summary

Cloud Deployment provides:

- **☁️ Infrastructure as Code**: Terraform-managed cloud infrastructure with version control
- **🎛️ Kubernetes Orchestration**: Production-ready container orchestration with auto-scaling
- **📊 Comprehensive Monitoring**: Cloud-native observability with Prometheus, Grafana, and CloudWatch
- **🔒 Security & Compliance**: Enterprise-grade security with RBAC, network policies, and admission controllers
- **💰 Cost Optimization**: Automated cost monitoring, right-sizing, and resource optimization
- **🌍 Global Scale**: Multi-region deployment with disaster recovery and high availability

**Key Takeaways**:
1. **Infrastructure as Code**: Terraform enables reproducible, version-controlled infrastructure
2. **Kubernetes Production**: Proper configuration is essential for reliability and security
3. **Observability**: Comprehensive monitoring is crucial for production operations
4. **Security First**: Implement security throughout the stack, not as an afterthought
5. **Cost Management**: Proactive cost monitoring prevents budget overruns

Next lesson: **Capstone Project - Complete Booking System** integrating all learned concepts!