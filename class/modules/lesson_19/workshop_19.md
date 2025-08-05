# 🚀 Lesson 19 Workshop: Cloud Deployment

## 🎯 Workshop Objective

Deploy a production-ready Spring Boot application to the cloud using Infrastructure as Code (Terraform), Kubernetes orchestration, comprehensive monitoring, security hardening, and cost optimization. You'll create a complete cloud-native deployment that can handle production traffic with enterprise-grade reliability, security, and observability.

**⏱️ Estimated Time**: 75 minutes

---

## 🏗️ What You'll Build

### **Enterprise Cloud Deployment Platform**
- **Infrastructure as Code**: Complete AWS infrastructure managed by Terraform
- **Kubernetes Production**: EKS cluster with auto-scaling, load balancing, and security
- **Monitoring & Observability**: Prometheus, Grafana, CloudWatch, and distributed tracing
- **Security & Compliance**: RBAC, network policies, pod security standards, and audit logging
- **Cost Optimization**: Resource monitoring, auto-scaling, and budget alerting
- **Disaster Recovery**: Multi-AZ deployment with backup and recovery procedures

### **Real-World Features**
```hcl
# Complete cloud infrastructure
resource "aws_eks_cluster" "main" {
  name     = "kotlin-kickstarter-cluster"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.27"
  
  vpc_config {
    subnet_ids = concat(aws_subnet.private[*].id, aws_subnet.public[*].id)
    endpoint_private_access = true
    endpoint_public_access  = true
  }
}

# Auto-scaling with cost optimization
resource "aws_autoscaling_policy" "scale_up" {
  scaling_adjustment     = 2
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.app.name
}
```

**🎯 Success Metrics**: 
- Complete cloud infrastructure deployment in <30 minutes
- Application accessible via HTTPS with SSL termination
- Auto-scaling responding to load with 99.9% availability
- Comprehensive monitoring with alerting and cost tracking

---

## 📁 Project Structure

```
class/workshop/lesson_19/
├── terraform/
│   ├── main.tf                     # TODO: Main infrastructure configuration
│   ├── variables.tf                # TODO: Input variables
│   ├── outputs.tf                  # TODO: Output values
│   ├── eks.tf                      # TODO: EKS cluster configuration
│   ├── vpc.tf                      # TODO: VPC and networking
│   ├── security.tf                 # TODO: Security groups and IAM
│   ├── monitoring.tf               # TODO: CloudWatch and logging
│   └── environments/
│       ├── staging/
│       │   └── terraform.tfvars    # TODO: Staging configuration
│       └── production/
│           └── terraform.tfvars    # TODO: Production configuration
├── k8s/
│   ├── base/
│   │   ├── namespace.yml           # TODO: Kubernetes namespace
│   │   ├── configmap.yml           # TODO: Application configuration
│   │   ├── secret.yml              # TODO: Secrets management
│   │   ├── deployment.yml          # TODO: Application deployment
│   │   ├── service.yml             # TODO: Service configuration
│   │   ├── ingress.yml             # TODO: Ingress controller
│   │   └── hpa.yml                 # TODO: Horizontal pod autoscaler
│   ├── overlays/
│   │   ├── staging/
│   │   │   └── kustomization.yml   # TODO: Staging customizations
│   │   └── production/
│   │       └── kustomization.yml   # TODO: Production customizations
│   └── monitoring/
│       ├── prometheus.yml          # TODO: Prometheus configuration
│       ├── grafana.yml             # TODO: Grafana setup
│       └── alertmanager.yml        # TODO: Alert management
├── helm/
│   └── kotlin-kickstarter/
│       ├── Chart.yaml              # TODO: Helm chart definition
│       ├── values.yaml             # TODO: Default values
│       ├── values-staging.yaml     # TODO: Staging values
│       ├── values-production.yaml  # TODO: Production values
│       └── templates/              # TODO: Kubernetes templates
├── scripts/
│   ├── deploy-infrastructure.sh    # TODO: Infrastructure deployment
│   ├── deploy-application.sh       # TODO: Application deployment
│   ├── setup-monitoring.sh         # TODO: Monitoring setup
│   └── cost-optimization.sh        # TODO: Cost optimization
├── monitoring/
│   ├── dashboards/                 # TODO: Grafana dashboards
│   ├── alerts/                     # TODO: Alert rules
│   └── cost-budgets/               # TODO: Cost management
└── docs/
    ├── architecture.md             # TODO: Architecture documentation
    ├── runbook.md                  # TODO: Operations runbook
    └── disaster-recovery.md        # TODO: DR procedures
```

---

## 🛠️ Step 1: Setup Cloud Infrastructure with Terraform

### **📝 TODO: Create terraform/main.tf**
```hcl
# terraform/main.tf
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
  
  # TODO: Configure backend for state management
  backend "s3" {
    bucket = "kotlin-kickstarter-terraform-state"
    key    = "infrastructure/terraform.tfstate"
    region = "us-west-2"
    
    # Enable state locking
    dynamodb_table = "terraform-state-lock"
    encrypt        = true
  }
}

# TODO: Configure AWS Provider
provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = var.owner_email
      CostCenter  = var.cost_center
    }
  }
}

# TODO: Configure Kubernetes Provider
provider "kubernetes" {
  host                   = aws_eks_cluster.main.endpoint
  cluster_ca_certificate = base64decode(aws_eks_cluster.main.certificate_authority[0].data)
  
  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", aws_eks_cluster.main.name]
  }
}

# TODO: Configure Helm Provider
provider "helm" {
  kubernetes {
    host                   = aws_eks_cluster.main.endpoint
    cluster_ca_certificate = base64decode(aws_eks_cluster.main.certificate_authority[0].data)
    
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", aws_eks_cluster.main.name]
    }
  }
}

# TODO: Get current AWS account and region
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}
data "aws_availability_zones" "available" {
  state = "available"
}

# TODO: Create random suffix for unique resource names
resource "random_string" "suffix" {
  length  = 6
  upper   = false
  special = false
}

locals {
  cluster_name = "${var.project_name}-cluster-${var.environment}"
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ClusterName = local.cluster_name
  }
}
```

### **📝 TODO: Create terraform/variables.tf**
```hcl
# terraform/variables.tf

# General Configuration
variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "kotlin-kickstarter"
}

variable "environment" {
  description = "Environment name (staging, production)"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-west-2"
}

variable "owner_email" {
  description = "Email of the owner"
  type        = string
}

variable "cost_center" {
  description = "Cost center for billing"
  type        = string
  default     = "engineering"
}

# TODO: Network Configuration
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones"
  type        = list(string)
  default     = ["us-west-2a", "us-west-2b", "us-west-2c"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
}

# TODO: EKS Configuration
variable "kubernetes_version" {
  description = "Kubernetes version"
  type        = string
  default     = "1.27"
}

variable "node_instance_types" {
  description = "EC2 instance types for EKS nodes"
  type        = list(string)
  default     = ["t3.medium", "t3.large"]
}

variable "node_desired_size" {
  description = "Desired number of nodes"
  type        = number
  default     = 3
}

variable "node_min_size" {
  description = "Minimum number of nodes"
  type        = number
  default     = 3
}

variable "node_max_size" {
  description = "Maximum number of nodes"
  type        = number
  default     = 20
}

variable "node_disk_size" {
  description = "Disk size for EKS nodes (GB)"
  type        = number
  default     = 50
}

# TODO: Security Configuration
variable "cluster_endpoint_public_access_cidrs" {
  description = "CIDR blocks that can access the cluster endpoint"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "node_ssh_key" {
  description = "SSH key name for node access"
  type        = string
  default     = ""
}

# TODO: Application Configuration
variable "app_name" {
  description = "Application name"
  type        = string
  default     = "kotlin-kickstarter"
}

variable "app_version" {
  description = "Application version"
  type        = string
  default     = "latest"
}

variable "app_replicas" {
  description = "Number of application replicas"
  type        = number
  default     = 3
}

# TODO: Monitoring Configuration
variable "enable_cloudwatch_logs" {
  description = "Enable CloudWatch logs"
  type        = bool
  default     = true
}

variable "enable_prometheus" {
  description = "Enable Prometheus monitoring"
  type        = bool
  default     = true
}

variable "enable_grafana" {
  description = "Enable Grafana dashboards"
  type        = bool
  default     = true
}

# TODO: Cost Management
variable "cost_budget_limit" {
  description = "Monthly cost budget limit in USD"
  type        = number
  default     = 500
}

variable "cost_alert_threshold" {
  description = "Cost alert threshold percentage"
  type        = number
  default     = 80
}
```

### **📝 TODO: Create terraform/vpc.tf**
```hcl
# terraform/vpc.tf - VPC and Networking Configuration

# TODO: Create VPC
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-vpc-${var.environment}"
    "kubernetes.io/cluster/${local.cluster_name}" = "shared"
  })
}

# TODO: Create Internet Gateway
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-igw-${var.environment}"
  })
}

# TODO: Create public subnets
resource "aws_subnet" "public" {
  count = length(var.availability_zones)
  
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-public-${var.availability_zones[count.index]}"
    Type = "Public"
    "kubernetes.io/cluster/${local.cluster_name}" = "shared"
    "kubernetes.io/role/elb" = "1"
  })
}

# TODO: Create private subnets
resource "aws_subnet" "private" {
  count = length(var.availability_zones)
  
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-private-${var.availability_zones[count.index]}"
    Type = "Private"
    "kubernetes.io/cluster/${local.cluster_name}" = "owned"
    "kubernetes.io/role/internal-elb" = "1"
  })
}

# TODO: Create Elastic IPs for NAT Gateways
resource "aws_eip" "nat" {
  count  = length(aws_subnet.public)
  domain = "vpc"
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-nat-eip-${count.index + 1}"
  })
  
  depends_on = [aws_internet_gateway.main]
}

# TODO: Create NAT Gateways
resource "aws_nat_gateway" "main" {
  count = length(aws_subnet.public)
  
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-nat-${count.index + 1}"
  })
  
  depends_on = [aws_internet_gateway.main]
}

# TODO: Create route table for public subnets
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-public-rt"
  })
}

# TODO: Create route tables for private subnets
resource "aws_route_table" "private" {
  count  = length(aws_nat_gateway.main)
  vpc_id = aws_vpc.main.id
  
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main[count.index].id
  }
  
  tags = merge(local.common_tags, {
    Name = "${var.project_name}-private-rt-${count.index + 1}"
  })
}

# TODO: Associate route tables with subnets
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

# TODO: Create VPC Flow Logs for security monitoring
resource "aws_flow_log" "main" {
  iam_role_arn    = aws_iam_role.flow_log.arn
  log_destination = aws_cloudwatch_log_group.vpc_flow_log.arn
  traffic_type    = "ALL"
  vpc_id          = aws_vpc.main.id
}

resource "aws_cloudwatch_log_group" "vpc_flow_log" {
  name              = "/aws/vpc/flow-logs"
  retention_in_days = 30
  
  tags = local.common_tags
}

resource "aws_iam_role" "flow_log" {
  name = "${var.project_name}-flow-log-role"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "vpc-flow-logs.amazonaws.com"
        }
      },
    ]
  })
}

resource "aws_iam_role_policy" "flow_log" {
  name = "${var.project_name}-flow-log-policy"
  role = aws_iam_role.flow_log.id
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogGroups",
          "logs:DescribeLogStreams",
        ]
        Effect   = "Allow"
        Resource = "*"
      },
    ]
  })
}
```

---

## 🛠️ Step 2: Configure EKS Cluster

### **📝 TODO: Create terraform/eks.tf**
```hcl
# terraform/eks.tf - Amazon EKS Cluster Configuration

# TODO: Create KMS key for EKS encryption
resource "aws_kms_key" "eks" {
  description             = "EKS Secret Encryption Key"
  deletion_window_in_days = 7
  enable_key_rotation     = true
  
  tags = local.common_tags
}

resource "aws_kms_alias" "eks" {
  name          = "alias/${var.project_name}-eks-${var.environment}"
  target_key_id = aws_kms_key.eks.key_id
}

# TODO: Create CloudWatch Log Group for EKS
resource "aws_cloudwatch_log_group" "eks_cluster" {
  name              = "/aws/eks/${local.cluster_name}/cluster"
  retention_in_days = 30
  
  tags = local.common_tags
}

# TODO: Create EKS Cluster
resource "aws_eks_cluster" "main" {
  name     = local.cluster_name
  role_arn = aws_iam_role.eks_cluster.arn
  version  = var.kubernetes_version
  
  vpc_config {
    subnet_ids              = concat(aws_subnet.private[*].id, aws_subnet.public[*].id)
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = var.cluster_endpoint_public_access_cidrs
    security_group_ids      = [aws_security_group.eks_cluster.id]
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
  
  tags = local.common_tags
}

# TODO: Create EKS Node Group
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.project_name}-node-group-${var.environment}"
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
  
  dynamic "remote_access" {
    for_each = var.node_ssh_key != "" ? [1] : []
    content {
      ec2_ssh_key               = var.node_ssh_key
      source_security_group_ids = [aws_security_group.node_group_ssh.id]
    }
  }
  
  labels = {
    Environment = var.environment
    NodeGroup   = "main"
    Role        = "application"
  }
  
  # TODO: Add taints for dedicated application nodes
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
  
  tags = local.common_tags
}

# TODO: Create EKS Addons
resource "aws_eks_addon" "vpc_cni" {
  cluster_name = aws_eks_cluster.main.name
  addon_name   = "vpc-cni"
  
  tags = local.common_tags
}

resource "aws_eks_addon" "coredns" {
  cluster_name = aws_eks_cluster.main.name
  addon_name   = "coredns"
  
  depends_on = [aws_eks_node_group.main]
  
  tags = local.common_tags
}

resource "aws_eks_addon" "kube_proxy" {
  cluster_name = aws_eks_cluster.main.name
  addon_name   = "kube-proxy"
  
  tags = local.common_tags
}

resource "aws_eks_addon" "ebs_csi" {
  cluster_name = aws_eks_cluster.main.name
  addon_name   = "aws-ebs-csi-driver"
  
  tags = local.common_tags
}

# TODO: Install AWS Load Balancer Controller
resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.5.4"
  
  set {
    name  = "clusterName"
    value = aws_eks_cluster.main.name
  }
  
  set {
    name  = "serviceAccount.create"
    value = "true"
  }
  
  set {
    name  = "serviceAccount.name"
    value = "aws-load-balancer-controller"
  }
  
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = aws_iam_role.aws_load_balancer_controller.arn
  }
  
  depends_on = [
    aws_eks_cluster.main,
    aws_eks_node_group.main
  ]
}

# TODO: Install Cluster Autoscaler
resource "helm_release" "cluster_autoscaler" {
  name       = "cluster-autoscaler"
  repository = "https://kubernetes.github.io/autoscaler"
  chart      = "cluster-autoscaler"
  namespace  = "kube-system"
  version    = "9.29.1"
  
  set {
    name  = "autoDiscovery.clusterName"
    value = aws_eks_cluster.main.name
  }
  
  set {
    name  = "awsRegion"
    value = var.aws_region
  }
  
  set {
    name  = "rbac.serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = aws_iam_role.cluster_autoscaler.arn
  }
  
  depends_on = [
    aws_eks_cluster.main,
    aws_eks_node_group.main
  ]
}
```

---

## 🛠️ Step 3: Create Kubernetes Application Deployment

### **📝 TODO: Create k8s/base/deployment.yml**
```yaml
# k8s/base/deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kotlin-kickstarter
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
          protocol: TCP
        env:
        # TODO: Add environment variables
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-url
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
        - name: REDIS_HOST
          value: "redis-service"
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
        # TODO: Configure resource limits
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
            ephemeral-storage: "1Gi"
          limits:
            memory: "1Gi"
            cpu: "500m"
            ephemeral-storage: "2Gi"
        # TODO: Add health checks
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 90
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
          successThreshold: 1
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
          successThreshold: 1
        startupProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
        # TODO: Configure security context
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
        # TODO: Mount volumes
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: app-config
          mountPath: /app/config
          readOnly: true
        - name: logs
          mountPath: /app/logs
      volumes:
      - name: tmp
        emptyDir: {}
      - name: app-config
        configMap:
          name: app-config
      - name: logs
        emptyDir: {}
      # TODO: Configure node selection and affinity
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
```

### **📝 TODO: Create k8s/base/hpa.yml**
```yaml
# k8s/base/hpa.yml - Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: kotlin-kickstarter-hpa
  labels:
    app: kotlin-kickstarter
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: kotlin-kickstarter
  minReplicas: 3
  maxReplicas: 20
  metrics:
  # TODO: CPU-based scaling
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  # TODO: Memory-based scaling  
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  # TODO: Custom metrics scaling
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  # TODO: Configure scaling behavior
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
      - type: Pods
        value: 2
        periodSeconds: 60
      selectPolicy: Min

---
# Pod Disruption Budget
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: kotlin-kickstarter-pdb
  labels:
    app: kotlin-kickstarter
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: kotlin-kickstarter

---
# Vertical Pod Autoscaler (for recommendations)
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: kotlin-kickstarter-vpa
  labels:
    app: kotlin-kickstarter
spec:
  targetRef:
    apiVersion: "apps/v1"
    kind: Deployment
    name: kotlin-kickstarter
  updatePolicy:
    updateMode: "Off"  # Only provide recommendations
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

## 🛠️ Step 4: Setup Monitoring and Observability

### **📝 TODO: Create monitoring/prometheus.yml**
```yaml
# monitoring/prometheus.yml
apiVersion: v1
kind: Namespace
metadata:
  name: monitoring
  labels:
    name: monitoring

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: monitoring
  labels:
    app: prometheus
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
        - '--web.route-prefix=/'
        ports:
        - containerPort: 9090
          name: web
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
        livenessProbe:
          httpGet:
            path: /-/healthy
            port: 9090
          initialDelaySeconds: 30
          timeoutSeconds: 30
        readinessProbe:
          httpGet:
            path: /-/ready
            port: 9090
          initialDelaySeconds: 30
          timeoutSeconds: 30
      volumes:
      - name: prometheus-config
        configMap:
          name: prometheus-config
      - name: prometheus-storage
        persistentVolumeClaim:
          claimName: prometheus-pvc

---
# TODO: Prometheus Configuration
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
      external_labels:
        cluster: 'kotlin-kickstarter'
        environment: 'production'
    
    rule_files:
    - "/etc/prometheus/rules/*.yml"
    
    alerting:
      alertmanagers:
      - static_configs:
        - targets:
          - alertmanager:9093
    
    scrape_configs:
    # TODO: Kubernetes API Server monitoring
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
    
    # TODO: Kubernetes nodes monitoring
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
      - target_label: __address__
        replacement: kubernetes.default.svc:443
      - source_labels: [__meta_kubernetes_node_name]
        regex: (.+)
        target_label: __metrics_path__
        replacement: /api/v1/nodes/${1}/proxy/metrics
    
    # TODO: Application monitoring
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
      metrics_path: /actuator/prometheus

---
# TODO: Prometheus Service
apiVersion: v1
kind: Service
metadata:
  name: prometheus-service
  namespace: monitoring
  labels:
    app: prometheus
spec:
  selector:
    app: prometheus
  ports:
  - protocol: TCP
    port: 9090
    targetPort: 9090
    name: web
  type: ClusterIP

---
# TODO: Prometheus PVC
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: prometheus-pvc
  namespace: monitoring
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 50Gi
  storageClassName: gp3
```

### **📝 TODO: Create monitoring/grafana.yml**
```yaml
# monitoring/grafana.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: monitoring
  labels:
    app: grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      securityContext:
        runAsUser: 472
        runAsGroup: 472
        fsGroup: 472
      containers:
      - name: grafana
        image: grafana/grafana:10.0.3
        ports:
        - containerPort: 3000
          name: web
        env:
        # TODO: Configure Grafana
        - name: GF_SECURITY_ADMIN_PASSWORD
          valueFrom:
            secretKeyRef:
              name: grafana-secrets
              key: admin-password
        - name: GF_INSTALL_PLUGINS
          value: "grafana-piechart-panel,grafana-worldmap-panel"
        - name: GF_AUTH_ANONYMOUS_ENABLED
          value: "false"
        - name: GF_SECURITY_ALLOW_EMBEDDING
          value: "true"
        resources:
          requests:
            cpu: 200m
            memory: 256Mi
          limits:
            cpu: 500m
            memory: 512Mi
        volumeMounts:
        - name: grafana-storage
          mountPath: /var/lib/grafana
        - name: grafana-config
          mountPath: /etc/grafana/provisioning/datasources
        - name: grafana-dashboards-config
          mountPath: /etc/grafana/provisioning/dashboards
        - name: grafana-dashboards
          mountPath: /var/lib/grafana/dashboards
        livenessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 30
          timeoutSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 30
          timeoutSeconds: 30
      volumes:
      - name: grafana-storage
        persistentVolumeClaim:
          claimName: grafana-pvc
      - name: grafana-config
        configMap:
          name: grafana-datasources
      - name: grafana-dashboards-config
        configMap:
          name: grafana-dashboards-config
      - name: grafana-dashboards
        configMap:
          name: kotlin-kickstarter-dashboard

---
# TODO: Grafana Configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-datasources
  namespace: monitoring
data:
  datasources.yml: |
    apiVersion: 1
    datasources:
    - name: Prometheus
      type: prometheus
      access: proxy
      url: http://prometheus-service:9090
      isDefault: true
      editable: true

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboards-config
  namespace: monitoring
data:
  dashboards.yml: |
    apiVersion: 1
    providers:
    - name: 'default'
      orgId: 1
      folder: ''
      type: file
      disableDeletion: false
      updateIntervalSeconds: 10
      allowUiUpdates: true
      options:
        path: /var/lib/grafana/dashboards

---
# TODO: Grafana Service
apiVersion: v1
kind: Service
metadata:
  name: grafana-service
  namespace: monitoring
  labels:
    app: grafana
spec:
  selector:
    app: grafana
  ports:
  - protocol: TCP
    port: 3000
    targetPort: 3000
    name: web
  type: ClusterIP

---
# TODO: Grafana PVC
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: grafana-pvc
  namespace: monitoring
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: gp3

---
# TODO: Grafana Secrets
apiVersion: v1
kind: Secret
metadata:
  name: grafana-secrets
  namespace: monitoring
type: Opaque
data:
  admin-password: YWRtaW4xMjM=  # admin123 base64 encoded
```

---

## 🛠️ Step 5: Create Deployment Scripts

### **📝 TODO: Create scripts/deploy-infrastructure.sh**
```bash
#!/bin/bash
# scripts/deploy-infrastructure.sh

set -e

# Configuration
ENVIRONMENT=${1:-staging}
AWS_REGION=${2:-us-west-2}
PROJECT_NAME="kotlin-kickstarter"

echo "🚀 Deploying infrastructure for ${ENVIRONMENT} environment"
echo "Region: ${AWS_REGION}"
echo "Project: ${PROJECT_NAME}"

# TODO: Validate prerequisites
echo "📋 Validating prerequisites..."

# Check if AWS CLI is installed and configured
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI is not installed"
    exit 1
fi

# Check if Terraform is installed
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform is not installed"
    exit 1
fi

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed"
    exit 1
fi

# TODO: Verify AWS credentials
echo "🔑 Verifying AWS credentials..."
aws sts get-caller-identity

# TODO: Create S3 bucket for Terraform state if it doesn't exist
BUCKET_NAME="${PROJECT_NAME}-terraform-state"
if ! aws s3 ls "s3://${BUCKET_NAME}" 2>/dev/null; then
    echo "📦 Creating S3 bucket for Terraform state..."
    aws s3 mb "s3://${BUCKET_NAME}" --region ${AWS_REGION}
    
    # Enable versioning
    aws s3api put-bucket-versioning \
        --bucket ${BUCKET_NAME} \
        --versioning-configuration Status=Enabled
    
    # Enable encryption
    aws s3api put-bucket-encryption \
        --bucket ${BUCKET_NAME} \
        --server-side-encryption-configuration '{
            "Rules": [
                {
                    "ApplyServerSideEncryptionByDefault": {
                        "SSEAlgorithm": "AES256"
                    }
                }
            ]
        }'
fi

# TODO: Create DynamoDB table for state locking if it doesn't exist
TABLE_NAME="terraform-state-lock"
if ! aws dynamodb describe-table --table-name ${TABLE_NAME} --region ${AWS_REGION} 2>/dev/null; then
    echo "🔒 Creating DynamoDB table for Terraform state locking..."
    aws dynamodb create-table \
        --table-name ${TABLE_NAME} \
        --attribute-definitions AttributeName=LockID,AttributeType=S \
        --key-schema AttributeName=LockID,KeyType=HASH \
        --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
        --region ${AWS_REGION}
    
    # Wait for table to be created
    aws dynamodb wait table-exists --table-name ${TABLE_NAME} --region ${AWS_REGION}
fi

# TODO: Deploy infrastructure with Terraform
echo "🏗️ Deploying infrastructure with Terraform..."
cd terraform/environments/${ENVIRONMENT}

# Initialize Terraform
terraform init

# Validate configuration
terraform validate

# Plan deployment
terraform plan -out=tfplan

# Apply deployment
echo "Applying Terraform configuration..."
terraform apply tfplan

# TODO: Get cluster information
CLUSTER_NAME=$(terraform output -raw cluster_name)
echo "✅ EKS Cluster created: ${CLUSTER_NAME}"

# TODO: Configure kubectl
echo "🔧 Configuring kubectl..."
aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}

# TODO: Verify cluster access
echo "🔍 Verifying cluster access..."
kubectl get nodes

# TODO: Install essential addons
echo "🔌 Installing essential addons..."

# Install metrics-server if not present
if ! kubectl get deployment metrics-server -n kube-system &>/dev/null; then
    kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
fi

# Wait for nodes to be ready
echo "⏳ Waiting for nodes to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=300s

echo "✅ Infrastructure deployment completed successfully!"
echo "📊 Cluster Info:"
kubectl get nodes -o wide
```

### **📝 TODO: Create scripts/deploy-application.sh**
```bash
#!/bin/bash
# scripts/deploy-application.sh

set -e

ENVIRONMENT=${1:-staging}
VERSION=${2:-latest}
PROJECT_NAME="kotlin-kickstarter"
NAMESPACE="${PROJECT_NAME}"

echo "🚀 Deploying application to ${ENVIRONMENT}"
echo "Version: ${VERSION}"
echo "Namespace: ${NAMESPACE}"

# TODO: Validate prerequisites
if ! kubectl cluster-info &>/dev/null; then
    echo "❌ kubectl is not configured or cluster is not accessible"
    exit 1
fi

# TODO: Create namespace if it doesn't exist
echo "📦 Creating namespace..."
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# TODO: Label namespace for monitoring
kubectl label namespace ${NAMESPACE} name=${NAMESPACE} --overwrite

# TODO: Deploy secrets
echo "🔐 Deploying secrets..."
kubectl apply -f k8s/base/secret.yml -n ${NAMESPACE}

# TODO: Deploy configmaps
echo "⚙️ Deploying configuration..."
kubectl apply -f k8s/base/configmap.yml -n ${NAMESPACE}

# TODO: Deploy RBAC
echo "🛡️ Deploying RBAC..."
kubectl apply -f k8s/base/rbac.yml -n ${NAMESPACE}

# TODO: Deploy application
echo "🚢 Deploying application..."

# Update image tag if version is specified
if [ "${VERSION}" != "latest" ]; then
    export IMAGE_TAG="ghcr.io/your-org/${PROJECT_NAME}:${VERSION}"
    envsubst < k8s/base/deployment.yml | kubectl apply -f - -n ${NAMESPACE}
else
    kubectl apply -f k8s/base/deployment.yml -n ${NAMESPACE}
fi

# TODO: Deploy service
kubectl apply -f k8s/base/service.yml -n ${NAMESPACE}

# TODO: Deploy ingress
kubectl apply -f k8s/base/ingress.yml -n ${NAMESPACE}

# TODO: Deploy HPA and PDB
kubectl apply -f k8s/base/hpa.yml -n ${NAMESPACE}

# TODO: Deploy monitoring resources
echo "📊 Deploying monitoring resources..."
kubectl apply -f k8s/base/servicemonitor.yml -n ${NAMESPACE}

# TODO: Wait for deployment to be ready
echo "⏳ Waiting for deployment to be ready..."
kubectl rollout status deployment/${PROJECT_NAME} -n ${NAMESPACE} --timeout=300s

# TODO: Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app=${PROJECT_NAME} -n ${NAMESPACE} --timeout=300s

# TODO: Get service information
echo "🌐 Service Information:"
kubectl get services -n ${NAMESPACE}

# TODO: Get ingress information
echo "🔗 Ingress Information:"
kubectl get ingress -n ${NAMESPACE}

# TODO: Run health checks
echo "🏥 Running health checks..."
SERVICE_NAME="${PROJECT_NAME}-service"

# Get service cluster IP
CLUSTER_IP=$(kubectl get service ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.spec.clusterIP}')

if [ -n "${CLUSTER_IP}" ]; then
    # Test health endpoint from within cluster
    kubectl run curl-test --image=curlimages/curl:latest --rm -i --restart=Never -- \
        curl -f "http://${CLUSTER_IP}/actuator/health" || echo "Health check failed"
else
    echo "⚠️ Could not get service cluster IP"
fi

# TODO: Check HPA status
echo "📈 HPA Status:"
kubectl get hpa -n ${NAMESPACE}

# TODO: Check pod metrics
echo "📊 Pod Metrics:"
kubectl top pods -n ${NAMESPACE} || echo "Metrics not available yet"

echo "✅ Application deployment completed successfully!"

# TODO: Display useful information
echo ""
echo "📋 Deployment Summary:"
echo "Namespace: ${NAMESPACE}"
echo "Deployment: ${PROJECT_NAME}"
echo "Replicas: $(kubectl get deployment ${PROJECT_NAME} -n ${NAMESPACE} -o jsonpath='{.status.readyReplicas}')/$(kubectl get deployment ${PROJECT_NAME} -n ${NAMESPACE} -o jsonpath='{.spec.replicas}')"
echo ""
echo "🔧 Useful Commands:"
echo "View pods: kubectl get pods -n ${NAMESPACE}"
echo "View logs: kubectl logs -f deployment/${PROJECT_NAME} -n ${NAMESPACE}"
echo "Scale app: kubectl scale deployment ${PROJECT_NAME} --replicas=5 -n ${NAMESPACE}"
echo "Port forward: kubectl port-forward service/${SERVICE_NAME} 8080:80 -n ${NAMESPACE}"
```

---

## 🛠️ Step 6: Deploy and Test Your Cloud Infrastructure

### **1. Initialize and Deploy Infrastructure**
```bash
cd class/workshop/lesson_19

# TODO: Configure AWS credentials
aws configure
# Enter your AWS Access Key ID, Secret Access Key, Region, and output format

# TODO: Set environment variables
export ENVIRONMENT="staging"
export AWS_REGION="us-west-2"
export PROJECT_NAME="kotlin-kickstarter"

# TODO: Deploy infrastructure
chmod +x scripts/deploy-infrastructure.sh
./scripts/deploy-infrastructure.sh staging us-west-2
```

### **2. Deploy Application to Kubernetes**
```bash
# TODO: Deploy application
chmod +x scripts/deploy-application.sh
./scripts/deploy-application.sh staging latest

# TODO: Verify deployment
kubectl get all -n kotlin-kickstarter

# TODO: Check pod status
kubectl get pods -n kotlin-kickstarter -o wide

# TODO: View application logs
kubectl logs -f deployment/kotlin-kickstarter -n kotlin-kickstarter
```

### **3. Setup Monitoring Stack**
```bash
# TODO: Deploy Prometheus
kubectl apply -f monitoring/prometheus.yml

# TODO: Deploy Grafana
kubectl apply -f monitoring/grafana.yml

# TODO: Wait for monitoring pods to be ready
kubectl wait --for=condition=ready pod -l app=prometheus -n monitoring --timeout=300s
kubectl wait --for=condition=ready pod -l app=grafana -n monitoring --timeout=300s

# TODO: Port forward to access monitoring
kubectl port-forward service/prometheus-service 9090:9090 -n monitoring &
kubectl port-forward service/grafana-service 3000:3000 -n monitoring &

echo "Prometheus: http://localhost:9090"
echo "Grafana: http://localhost:3000 (admin/admin123)"
```

### **4. Test Auto-Scaling**
```bash
# TODO: Generate load to test auto-scaling
kubectl run load-generator --image=busybox --rm -i --restart=Never -- \
  /bin/sh -c "while true; do wget -q -O- http://kotlin-kickstarter-service.kotlin-kickstarter/actuator/health; done"

# TODO: Watch HPA in action
kubectl get hpa -n kotlin-kickstarter -w

# TODO: Monitor pod scaling
kubectl get pods -n kotlin-kickstarter -w
```

### **5. Test Ingress and SSL**
```bash
# TODO: Get ingress information
kubectl get ingress -n kotlin-kickstarter

# TODO: Test application endpoints
INGRESS_URL=$(kubectl get ingress kotlin-kickstarter-ingress -n kotlin-kickstarter -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

if [ -n "${INGRESS_URL}" ]; then
    echo "Testing application at: https://${INGRESS_URL}"
    curl -k "https://${INGRESS_URL}/actuator/health"
    curl -k "https://${INGRESS_URL}/api/health"
else
    echo "Ingress URL not available yet"
fi
```

---

## 🛠️ Step 7: Configure Cost Monitoring and Optimization

### **📝 TODO: Create cost-optimization/budget.tf**
```hcl
# cost-optimization/budget.tf
resource "aws_budgets_budget" "monthly_cost" {
  name         = "${var.project_name}-monthly-budget"
  budget_type  = "COST"
  limit_amount = var.cost_budget_limit
  limit_unit   = "USD"
  time_unit    = "MONTHLY"
  
  cost_filters {
    tag {
      key = "Project"
      values = [var.project_name]
    }
  }
  
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                 = var.cost_alert_threshold
    threshold_type            = "PERCENTAGE"
    notification_type         = "ACTUAL"
    subscriber_email_addresses = [var.owner_email]
  }
  
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                 = 100
    threshold_type            = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = [var.owner_email]
  }
}

resource "aws_ce_anomaly_detector" "cost_anomaly" {
  name         = "${var.project_name}-cost-anomaly-detector"
  monitor_type = "DIMENSIONAL"
  
  monitor_specification = jsonencode({
    DimensionKey = "SERVICE"
    DimensionValueList = ["Amazon Elastic Kubernetes Service", "Amazon EC2-Instance"]
    MatchOptions = ["EQUALS"]
  })
}

resource "aws_ce_anomaly_subscription" "cost_anomaly_subscription" {
  name      = "${var.project_name}-cost-anomaly-subscription"
  frequency = "DAILY"
  
  monitor_arn_list = [aws_ce_anomaly_detector.cost_anomaly.arn]
  
  subscriber {
    type    = "EMAIL"
    address = var.owner_email
  }
  
  threshold_expression {
    and {
      dimension {
        key           = "ANOMALY_TOTAL_IMPACT_ABSOLUTE"
        values        = ["100"]
      }
    }
  }
}
```

### **📝 TODO: Create scripts/cost-optimization.sh**
```bash
#!/bin/bash
# scripts/cost-optimization.sh

set -e

echo "💰 Running cost optimization analysis..."

# TODO: Get current AWS costs
echo "📊 Current month costs:"
aws ce get-cost-and-usage \
    --time-period Start=$(date -d "$(date +%Y-%m-01)" +%Y-%m-%d),End=$(date +%Y-%m-%d) \
    --granularity MONTHLY \
    --metrics BlendedCost \
    --group-by Type=DIMENSION,Key=SERVICE \
    --query 'ResultsByTime[0].Groups[?Metrics.BlendedCost.Amount>`0`].[Keys[0],Metrics.BlendedCost.Amount]' \
    --output table

# TODO: Analyze EKS costs
echo "🎛️ EKS cluster costs:"
aws ce get-cost-and-usage \
    --time-period Start=$(date -d "7 days ago" +%Y-%m-%d),End=$(date +%Y-%m-%d) \
    --granularity DAILY \
    --metrics BlendedCost \
    --group-by Type=DIMENSION,Key=SERVICE \
    --filter file://<(cat << 'EOF'
{
  "Dimensions": {
    "Key": "SERVICE",
    "Values": ["Amazon Elastic Kubernetes Service"]
  }
}
EOF
) \
    --query 'ResultsByTime[].[TimePeriod.Start,Total.BlendedCost.Amount]' \
    --output table

# TODO: Get resource utilization recommendations
echo "🔧 Resource utilization recommendations:"

# Get VPA recommendations
kubectl get vpa -A -o json | jq -r '
  .items[] | 
  select(.status.recommendation) |
  "Pod: \(.metadata.namespace)/\(.metadata.name) - CPU: \(.status.recommendation.containerRecommendations[0].target.cpu) Memory: \(.status.recommendation.containerRecommendations[0].target.memory)"
'

# TODO: Check for unused resources
echo "🗑️ Checking for unused resources..."

# Check for persistent volumes without claims
kubectl get pv -o json | jq -r '
  .items[] | 
  select(.status.phase == "Available") |
  "Unused PV: \(.metadata.name) - Size: \(.spec.capacity.storage)"
'

# Check for load balancers without backends
kubectl get services --all-namespaces -o json | jq -r '
  .items[] | 
  select(.spec.type == "LoadBalancer" and (.status.loadBalancer.ingress | length) == 0) |
  "LoadBalancer without backends: \(.metadata.namespace)/\(.metadata.name)"
'

# TODO: Spot instance opportunities
echo "💡 Spot instance opportunities:"
aws ec2 describe-spot-price-history \
    --instance-types t3.medium t3.large \
    --product-descriptions "Linux/UNIX" \
    --max-items 5 \
    --query 'SpotPriceHistory[].[InstanceType,SpotPrice,Timestamp]' \
    --output table

echo "✅ Cost optimization analysis complete!"
echo ""
echo "💡 Recommendations:"
echo "1. Consider using Spot instances for non-critical workloads"
echo "2. Review VPA recommendations for right-sizing"
echo "3. Clean up unused persistent volumes"
echo "4. Monitor cost alerts and anomalies"
echo "5. Use cluster autoscaler to minimize idle nodes"
```

---

## 🎯 Expected Results

### **Infrastructure Deployment**
- **EKS Cluster**: Fully configured with 3 nodes across multiple AZs
- **Networking**: VPC with public/private subnets and NAT gateways
- **Security**: IAM roles, security groups, and pod security policies
- **Monitoring**: CloudWatch logs and metrics collection

### **Application Deployment**
- **Pods**: 3 application replicas running with health checks
- **Service**: Load balancer distributing traffic across pods
- **Ingress**: HTTPS termination with SSL certificate
- **Auto-scaling**: HPA configured for CPU/memory-based scaling

### **Monitoring Stack**
```bash
# Verify monitoring components
kubectl get all -n monitoring

NAME                           READY   STATUS    RESTARTS   AGE
pod/prometheus-xxx             1/1     Running   0          5m
pod/grafana-xxx                1/1     Running   0          5m

NAME                             TYPE        CLUSTER-IP     PORT(S)    AGE
service/prometheus-service       ClusterIP   10.100.1.1     9090/TCP   5m
service/grafana-service          ClusterIP   10.100.1.2     3000/TCP   5m
```

### **Cost Monitoring**
- **Budget Alerts**: Email notifications when 80% of budget is reached
- **Anomaly Detection**: Automated detection of unusual spending patterns
- **Resource Optimization**: VPA recommendations for right-sizing
- **Spot Instance Analysis**: Cost savings opportunities identified

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: Multi-Region Deployment**
Deploy the application across multiple AWS regions with cross-region load balancing.

### **🔥 Bonus Challenge 2: GitOps with ArgoCD**
Implement GitOps deployment pipeline using ArgoCD for automated deployments.

### **🔥 Bonus Challenge 3: Service Mesh**
Add Istio service mesh for advanced traffic management and security.

### **🔥 Bonus Challenge 4: Chaos Engineering**
Implement chaos testing with AWS Fault Injection Simulator.

### **🔥 Bonus Challenge 5: Advanced Monitoring**
Add distributed tracing with Jaeger and custom business metrics.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Deployed production infrastructure** using Infrastructure as Code with Terraform  
✅ **Configured Kubernetes cluster** with auto-scaling, security, and monitoring  
✅ **Implemented comprehensive observability** with Prometheus, Grafana, and CloudWatch  
✅ **Established security controls** with RBAC, network policies, and pod security standards  
✅ **Set up cost optimization** with budgets, alerts, and resource right-sizing  
✅ **Created disaster recovery** procedures with backup and multi-AZ deployment

**🚀 Next Lesson**: Capstone Project - Complete Booking System integrating all learned concepts!