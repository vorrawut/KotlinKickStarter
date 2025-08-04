# 🚀 Lesson 18 Workshop: CI/CD Pipeline Setup

## 🎯 Workshop Objective

Build a complete CI/CD pipeline using GitHub Actions with automated testing, security scanning, artifact management, and deployment automation. You'll create a production-ready pipeline that automatically tests, builds, and deploys your Spring Boot application across multiple environments with monitoring and rollback capabilities.

**⏱️ Estimated Time**: 60 minutes

---

## 🏗️ What You'll Build

### **Complete CI/CD Automation System**
- **Automated Testing Pipeline**: Unit, integration, and end-to-end testing with coverage reporting
- **Security & Quality Gates**: Code scanning, dependency checks, vulnerability assessment
- **Artifact Management**: Semantic versioning, multi-registry publishing, signed container images
- **Deployment Automation**: Multi-environment deployment with blue-green and canary strategies
- **Monitoring & Observability**: Pipeline metrics, deployment tracking, automated notifications
- **Rollback Mechanisms**: Automated rollback procedures and incident response

### **Real-World Features**
```yaml
# Complete pipeline triggering on code changes
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  release:
    types: [published]

# Automated testing with quality gates
- Unit Tests (Coverage > 80%)
- Integration Tests (Database + Redis)
- Security Scanning (SAST, Dependency Check)
- Container Vulnerability Scanning
- Performance Testing (Load + E2E)

# Deployment automation
- Development: Auto-deploy on develop branch
- Staging: Auto-deploy on main branch
- Production: Deploy on release tags with approval
```

**🎯 Success Metrics**: 
- Complete automation from commit to production deployment
- Zero-downtime deployments with automated rollback
- Comprehensive security scanning and quality gates
- Pipeline completion time under 15 minutes

---

## 📁 Project Structure

```
class/workshop/lesson_18/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml              # TODO: Main CI pipeline
│   │   ├── cd.yml              # TODO: Deployment pipeline
│   │   ├── security.yml        # TODO: Security scanning
│   │   ├── release.yml         # TODO: Release management
│   │   └── cleanup.yml         # TODO: Resource cleanup
│   ├── environments/
│   │   ├── development.yml     # TODO: Dev environment config
│   │   ├── staging.yml         # TODO: Staging environment config
│   │   └── production.yml      # TODO: Production environment config
│   └── dependabot.yml          # TODO: Dependency automation
├── scripts/
│   ├── deploy.sh               # TODO: Deployment script
│   ├── rollback.sh             # TODO: Rollback procedures
│   └── health-check.sh         # TODO: Health verification
├── tests/
│   ├── integration/            # TODO: Integration test configuration
│   ├── e2e/                    # TODO: End-to-end tests
│   └── performance/            # TODO: Performance testing
├── k8s/
│   ├── base/                   # TODO: Base Kubernetes manifests
│   ├── overlays/               # TODO: Environment overlays
│   │   ├── development/
│   │   ├── staging/
│   │   └── production/
│   └── policies/               # TODO: Security policies
├── build.gradle.kts            # ✅ Existing Spring Boot project
├── Dockerfile                  # ✅ From previous lesson
├── docker-compose.yml          # ✅ From previous lesson
└── src/main/                   # ✅ Spring Boot application
```

---

## 🛠️ Step 1: Setup Repository and Base Configuration

### **📝 TODO: Create .github/workflows/ci.yml**
```yaml
# .github/workflows/ci.yml
name: Continuous Integration

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  JAVA_VERSION: '17'
  GRADLE_CACHE_KEY: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}

jobs:
  # Job 1: Code Quality and Unit Tests
  test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        fetch-depth: 0  # Needed for SonarCloud
    
    # TODO: Setup Java environment
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
    
    # TODO: Setup Gradle caching
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ env.GRADLE_CACHE_KEY }}
        restore-keys: |
          gradle-${{ runner.os }}-
    
    # TODO: Make gradlew executable
    - name: Make gradlew executable
      run: chmod +x ./gradlew
    
    # TODO: Run unit tests with coverage
    - name: Run unit tests
      run: |
        ./gradlew test jacocoTestReport
    
    # TODO: Upload test results
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: |
          build/reports/tests/test/
          build/reports/jacoco/test/
    
    # TODO: Upload coverage to Codecov
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: build/reports/jacoco/test/jacocoTestReport.xml
        fail_ci_if_error: true

  # Job 2: Integration Tests
  integration-test:
    runs-on: ubuntu-latest
    needs: test
    
    services:
      # TODO: Setup PostgreSQL service
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: testpass
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
      
      # TODO: Setup Redis service
      redis:
        image: redis:7
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379
    
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v3
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ env.GRADLE_CACHE_KEY }}
    
    # TODO: Run integration tests
    - name: Run integration tests
      run: ./gradlew integrationTest
      env:
        SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
        SPRING_DATASOURCE_USERNAME: testuser
        SPRING_DATASOURCE_PASSWORD: testpass
        SPRING_DATA_REDIS_HOST: localhost
        SPRING_DATA_REDIS_PORT: 6379
    
    # TODO: Upload integration test results
    - name: Upload integration test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: integration-test-results
        path: build/reports/tests/integrationTest/

  # Job 3: Build Application
  build:
    runs-on: ubuntu-latest
    needs: [test, integration-test]
    outputs:
      artifact-name: ${{ steps.build.outputs.artifact-name }}
      version: ${{ steps.version.outputs.version }}
    
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v3
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ env.GRADLE_CACHE_KEY }}
    
    # TODO: Generate version
    - name: Generate version
      id: version
      run: |
        if [[ $GITHUB_REF == refs/tags/* ]]; then
          VERSION=${GITHUB_REF#refs/tags/v}
        else
          VERSION=${GITHUB_SHA:0:8}
        fi
        echo "version=$VERSION" >> $GITHUB_OUTPUT
        echo "Version: $VERSION"
    
    # TODO: Build application
    - name: Build application
      id: build
      run: |
        ./gradlew build -x test
        ARTIFACT_NAME="cicd-workshop-${{ steps.version.outputs.version }}.jar"
        cp build/libs/*.jar "build/libs/$ARTIFACT_NAME"
        echo "artifact-name=$ARTIFACT_NAME" >> $GITHUB_OUTPUT
    
    # TODO: Upload build artifacts
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: application-jar
        path: build/libs/${{ steps.build.outputs.artifact-name }}
        retention-days: 30
```

---

## 🛠️ Step 2: Create Security Scanning Pipeline

### **📝 TODO: Create .github/workflows/security.yml**
```yaml
# .github/workflows/security.yml
name: Security Scanning

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    # Run security scan daily at 2 AM UTC
    - cron: '0 2 * * *'

jobs:
  # Job 1: Dependency Security Check
  dependency-check:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    # TODO: Setup Java for dependency analysis
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    # TODO: Run OWASP Dependency Check
    - name: Run OWASP Dependency Check
      run: |
        ./gradlew dependencyCheckAnalyze
    
    # TODO: Upload dependency check results
    - name: Upload dependency check results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: dependency-check-report
        path: build/reports/dependency-check/

  # Job 2: Static Application Security Testing (SAST)
  sast:
    runs-on: ubuntu-latest
    permissions:
      security-events: write
      contents: read
      actions: read
    
    steps:
    - uses: actions/checkout@v4
      with:
        fetch-depth: 0
    
    # TODO: Initialize CodeQL
    - name: Initialize CodeQL
      uses: github/codeql-action/init@v2
      with:
        languages: java
        queries: security-extended,security-and-quality
    
    # TODO: Build project for analysis
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build project
      run: |
        ./gradlew build -x test
    
    # TODO: Perform CodeQL Analysis
    - name: Perform CodeQL Analysis
      uses: github/codeql-action/analyze@v2
      with:
        category: "/language:java"

  # Job 3: Secret Scanning
  secret-scan:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
      with:
        fetch-depth: 0
    
    # TODO: Run TruffleHog secret scanner
    - name: Run TruffleHog
      uses: trufflesecurity/trufflehog@main
      with:
        path: ./
        base: main
        head: HEAD
        extra_args: --debug --only-verified

  # Job 4: License Compliance Check
  license-check:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    # TODO: Check license compliance
    - name: Generate license report
      run: |
        ./gradlew generateLicenseReport
    
    # TODO: Upload license report
    - name: Upload license report
      uses: actions/upload-artifact@v3
      with:
        name: license-report
        path: build/reports/license/
```

---

## 🛠️ Step 3: Create Container Build and Security Pipeline

### **📝 TODO: Create .github/workflows/container.yml**
```yaml
# .github/workflows/container.yml
name: Container Build and Security

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # Job 1: Build Container Image
  build-image:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    outputs:
      image-tag: ${{ steps.meta.outputs.tags }}
      image-digest: ${{ steps.build.outputs.digest }}
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    # TODO: Setup Docker Buildx
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
    
    # TODO: Login to Container Registry
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    # TODO: Extract metadata for image
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=semver,pattern={{version}}
          type=sha,prefix={{branch}}-
    
    # TODO: Build and push Docker image
    - name: Build and push Docker image
      id: build
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max

  # Job 2: Container Security Scan
  container-security:
    runs-on: ubuntu-latest
    needs: build-image
    permissions:
      security-events: write
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    # TODO: Run Trivy vulnerability scanner
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: ${{ needs.build-image.outputs.image-tag }}
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    # TODO: Upload Trivy scan results to GitHub Security tab
    - name: Upload Trivy scan results to GitHub Security tab
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
    
    # TODO: Fail build on high severity vulnerabilities
    - name: Fail on high severity vulnerabilities
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: ${{ needs.build-image.outputs.image-tag }}
        format: 'table'
        exit-code: '1'
        ignore-unfixed: true
        vuln-type: 'os,library'
        severity: 'CRITICAL,HIGH'

  # Job 3: Container Image Signing
  sign-image:
    runs-on: ubuntu-latest
    needs: [build-image, container-security]
    if: github.event_name != 'pull_request'
    permissions:
      contents: read
      packages: write
      id-token: write
    
    steps:
    # TODO: Install Cosign
    - name: Install Cosign
      uses: sigstore/cosign-installer@v3
    
    # TODO: Sign container image
    - name: Sign container image
      run: |
        echo "Signing image: ${{ needs.build-image.outputs.image-tag }}"
        cosign sign --yes ${{ needs.build-image.outputs.image-tag }}
      env:
        COSIGN_EXPERIMENTAL: 1
```

---

## 🛠️ Step 4: Create Deployment Pipeline

### **📝 TODO: Create .github/workflows/deploy.yml**
```yaml
# .github/workflows/deploy.yml
name: Deployment Pipeline

on:
  workflow_run:
    workflows: ["Continuous Integration", "Container Build and Security"]
    types:
      - completed
    branches: [main, develop]
  release:
    types: [published]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # Job 1: Deploy to Development
  deploy-dev:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop' && github.event.workflow_run.conclusion == 'success'
    environment: development
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    # TODO: Setup kubectl
    - name: Setup kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'latest'
    
    # TODO: Configure kubectl for development cluster
    - name: Configure kubectl
      run: |
        echo "${{ secrets.DEV_KUBECONFIG }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig
        kubectl config current-context
    
    # TODO: Deploy to development environment
    - name: Deploy to development
      run: |
        IMAGE_TAG="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:develop-${{ github.sha }}"
        
        # Update deployment with new image
        kubectl set image deployment/cicd-workshop-dev \
          app=$IMAGE_TAG \
          --namespace=development
        
        # Wait for rollout to complete
        kubectl rollout status deployment/cicd-workshop-dev \
          --namespace=development \
          --timeout=300s
      env:
        KUBECONFIG: kubeconfig
    
    # TODO: Run smoke tests
    - name: Run smoke tests
      run: |
        # Wait for service to be ready
        kubectl wait --for=condition=ready pod \
          -l app=cicd-workshop \
          --namespace=development \
          --timeout=300s
        
        # Get service URL and test health endpoint
        SERVICE_URL=$(kubectl get service cicd-workshop-dev-service \
          --namespace=development \
          -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        
        curl -f http://$SERVICE_URL:8080/actuator/health
        curl -f http://$SERVICE_URL:8080/api/health

  # Job 2: Deploy to Staging
  deploy-staging:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && github.event.workflow_run.conclusion == 'success'
    environment: staging
    needs: []  # Can run independently
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Setup kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'latest'
    
    # TODO: Configure kubectl for staging cluster
    - name: Configure kubectl
      run: |
        echo "${{ secrets.STAGING_KUBECONFIG }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig
    
    # TODO: Deploy to staging with blue-green strategy
    - name: Blue-Green Deploy to Staging
      run: |
        export KUBECONFIG=kubeconfig
        IMAGE_TAG="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:main-${{ github.sha }}"
        
        # Determine current and target environments
        CURRENT=$(kubectl get service cicd-workshop-staging-service \
          -o jsonpath='{.spec.selector.version}' --namespace=staging || echo "blue")
        
        if [ "$CURRENT" = "blue" ]; then
          TARGET="green"
        else
          TARGET="blue"
        fi
        
        echo "Deploying to $TARGET environment (current: $CURRENT)"
        
        # Deploy to target environment
        kubectl set image deployment/cicd-workshop-staging-$TARGET \
          app=$IMAGE_TAG \
          --namespace=staging
        
        # Wait for deployment
        kubectl rollout status deployment/cicd-workshop-staging-$TARGET \
          --namespace=staging \
          --timeout=300s
        
        # Switch traffic
        kubectl patch service cicd-workshop-staging-service \
          --namespace=staging \
          -p "{\"spec\":{\"selector\":{\"version\":\"$TARGET\"}}}"
        
        echo "Traffic switched to $TARGET environment"
    
    # TODO: Run end-to-end tests against staging
    - name: Run E2E tests
      run: |
        # Install Newman for API testing
        npm install -g newman
        
        # Run Postman collection against staging
        newman run tests/e2e/api-tests.postman_collection.json \
          --environment tests/e2e/staging.postman_environment.json \
          --reporters cli,junit \
          --reporter-junit-export results/e2e-results.xml
    
    # TODO: Upload E2E test results
    - name: Upload E2E test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: e2e-test-results
        path: results/e2e-results.xml

  # Job 3: Deploy to Production
  deploy-production:
    runs-on: ubuntu-latest
    if: github.event_name == 'release'
    environment: production
    needs: []  # Independent job for release events
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Setup kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'latest'
    
    # TODO: Configure kubectl for production cluster
    - name: Configure kubectl
      run: |
        echo "${{ secrets.PROD_KUBECONFIG }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig
    
    # TODO: Canary deployment to production
    - name: Canary Deploy to Production
      run: |
        export KUBECONFIG=kubeconfig
        IMAGE_TAG="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.ref_name }}"
        
        # Deploy canary with 10% traffic
        kubectl apply -f k8s/overlays/production/canary-deployment.yml
        kubectl set image deployment/cicd-workshop-canary \
          app=$IMAGE_TAG \
          --namespace=production
        
        # Wait for canary deployment
        kubectl rollout status deployment/cicd-workshop-canary \
          --namespace=production \
          --timeout=300s
        
        echo "Canary deployment completed"
    
    # TODO: Monitor canary metrics
    - name: Monitor canary metrics
      run: |
        echo "Monitoring canary for 5 minutes..."
        sleep 300
        
        # Check error rate (this would typically query Prometheus)
        # For demo purposes, we'll assume metrics are healthy
        echo "Canary metrics look healthy, proceeding with full rollout"
    
    # TODO: Complete production rollout
    - name: Complete production rollout
      run: |
        export KUBECONFIG=kubeconfig
        IMAGE_TAG="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.ref_name }}"
        
        # Update main production deployment
        kubectl set image deployment/cicd-workshop-production \
          app=$IMAGE_TAG \
          --namespace=production
        
        # Wait for rollout
        kubectl rollout status deployment/cicd-workshop-production \
          --namespace=production \
          --timeout=600s
        
        # Remove canary deployment
        kubectl delete deployment cicd-workshop-canary \
          --namespace=production
        
        echo "Production deployment completed successfully"
    
    # TODO: Post-deployment verification
    - name: Post-deployment verification
      run: |
        # Verify production health
        SERVICE_URL=$(kubectl get service cicd-workshop-production-service \
          --namespace=production \
          -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        
        # Health checks
        curl -f https://$SERVICE_URL/actuator/health/readiness
        curl -f https://$SERVICE_URL/api/health
        
        echo "Production deployment verified successfully"
```

---

## 🛠️ Step 5: Create Rollback and Recovery Pipeline

### **📝 TODO: Create .github/workflows/rollback.yml**
```yaml
# .github/workflows/rollback.yml
name: Rollback Pipeline

on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to rollback'
        required: true
        type: choice
        options:
        - staging
        - production
      version:
        description: 'Version to rollback to (optional)'
        required: false
        type: string

jobs:
  rollback:
    runs-on: ubuntu-latest
    environment: ${{ github.event.inputs.environment }}
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Setup kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'latest'
    
    # TODO: Configure kubectl based on environment
    - name: Configure kubectl
      run: |
        if [ "${{ github.event.inputs.environment }}" = "production" ]; then
          echo "${{ secrets.PROD_KUBECONFIG }}" | base64 -d > kubeconfig
        else
          echo "${{ secrets.STAGING_KUBECONFIG }}" | base64 -d > kubeconfig
        fi
        export KUBECONFIG=kubeconfig
    
    # TODO: Perform rollback
    - name: Rollback deployment
      run: |
        export KUBECONFIG=kubeconfig
        NAMESPACE="${{ github.event.inputs.environment }}"
        
        if [ -n "${{ github.event.inputs.version }}" ]; then
          # Rollback to specific version
          VERSION="${{ github.event.inputs.version }}"
          IMAGE_TAG="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:$VERSION"
          
          kubectl set image deployment/cicd-workshop-$NAMESPACE \
            app=$IMAGE_TAG \
            --namespace=$NAMESPACE
        else
          # Rollback to previous version
          kubectl rollout undo deployment/cicd-workshop-$NAMESPACE \
            --namespace=$NAMESPACE
        fi
        
        # Wait for rollback to complete
        kubectl rollout status deployment/cicd-workshop-$NAMESPACE \
          --namespace=$NAMESPACE \
          --timeout=300s
    
    # TODO: Verify rollback
    - name: Verify rollback
      run: |
        export KUBECONFIG=kubeconfig
        NAMESPACE="${{ github.event.inputs.environment }}"
        
        # Wait for pods to be ready
        kubectl wait --for=condition=ready pod \
          -l app=cicd-workshop \
          --namespace=$NAMESPACE \
          --timeout=300s
        
        # Health check
        SERVICE_URL=$(kubectl get service cicd-workshop-$NAMESPACE-service \
          --namespace=$NAMESPACE \
          -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        
        curl -f http://$SERVICE_URL:8080/actuator/health
        
        echo "Rollback completed and verified successfully"
    
    # TODO: Send notification
    - name: Send rollback notification
      uses: 8398a7/action-slack@v3
      with:
        status: custom
        custom_payload: |
          {
            "text": "🔄 Rollback completed for ${{ github.event.inputs.environment }}",
            "attachments": [{
              "color": "warning",
              "fields": [{
                "title": "Environment",
                "value": "${{ github.event.inputs.environment }}",
                "short": true
              }, {
                "title": "Triggered by",
                "value": "${{ github.actor }}",
                "short": true
              }]
            }]
          }
      env:
        SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

---

## 🛠️ Step 6: Create Performance and E2E Testing

### **📝 TODO: Create tests/e2e/api-tests.postman_collection.json**
```json
{
  "info": {
    "name": "CI/CD Workshop API Tests",
    "description": "End-to-end API tests for the CI/CD workshop application",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "event": [
    {
      "listen": "prerequest",
      "script": {
        "exec": [
          "// Set base URL from environment",
          "pm.globals.set('baseUrl', pm.environment.get('baseUrl') || 'http://localhost:8080');"
        ]
      }
    }
  ],
  "item": [
    {
      "name": "Health Check",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', function () {",
              "    pm.response.to.have.status(200);",
              "});",
              "",
              "pm.test('Response contains status UP', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.status).to.eql('UP');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/actuator/health",
          "host": ["{{baseUrl}}"],
          "path": ["actuator", "health"]
        }
      }
    },
    {
      "name": "Register User",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', function () {",
              "    pm.response.to.have.status(200);",
              "});",
              "",
              "pm.test('Response contains success true', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.success).to.be.true;",
              "    pm.globals.set('userId', jsonData.userId);",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test{{$timestamp}}@example.com\",\n  \"password\": \"testpassword123\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/users/register",
          "host": ["{{baseUrl}}"],
          "path": ["api", "users", "register"]
        }
      }
    },
    {
      "name": "Login User",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 200', function () {",
              "    pm.response.to.have.status(200);",
              "});",
              "",
              "pm.test('Response contains JWT token', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.token).to.exist;",
              "    pm.globals.set('authToken', jsonData.token);",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test{{$timestamp}}@example.com\",\n  \"password\": \"testpassword123\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/users/login",
          "host": ["{{baseUrl}}"],
          "path": ["api", "users", "login"]
        }
      }
    }
  ]
}
```

### **📝 TODO: Create tests/performance/load-test.js**
```javascript
// tests/performance/load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export let options = {
  stages: [
    { duration: '2m', target: 10 }, // Ramp up to 10 users
    { duration: '5m', target: 10 }, // Stay at 10 users
    { duration: '2m', target: 20 }, // Ramp up to 20 users
    { duration: '5m', target: 20 }, // Stay at 20 users
    { duration: '2m', target: 0 },  // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    http_req_failed: ['rate<0.02'],   // Error rate under 2%
    errors: ['rate<0.02'],            // Custom error rate under 2%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function() {
  // TODO: Test user registration
  let registerPayload = JSON.stringify({
    email: `user${__VU}${__ITER}@example.com`,
    password: 'testpassword123'
  });
  
  let registerResponse = http.post(`${BASE_URL}/api/users/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  check(registerResponse, {
    'registration status is 200': (r) => r.status === 200,
    'registration response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);
  
  sleep(1);
  
  // TODO: Test user login
  let loginPayload = JSON.stringify({
    email: `user${__VU}${__ITER}@example.com`,
    password: 'testpassword123'
  });
  
  let loginResponse = http.post(`${BASE_URL}/api/users/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  check(loginResponse, {
    'login status is 200': (r) => r.status === 200,
    'login response time < 300ms': (r) => r.timings.duration < 300,
    'login response contains token': (r) => r.json('token') !== undefined,
  }) || errorRate.add(1);
  
  sleep(1);
  
  // TODO: Test health endpoint
  let healthResponse = http.get(`${BASE_URL}/actuator/health`);
  
  check(healthResponse, {
    'health check status is 200': (r) => r.status === 200,
    'health check response time < 100ms': (r) => r.timings.duration < 100,
  }) || errorRate.add(1);
  
  sleep(1);
}

export function handleSummary(data) {
  return {
    'results/performance-summary.json': JSON.stringify(data),
  };
}
```

---

## 🛠️ Step 7: Create Monitoring and Notifications

### **📝 TODO: Create .github/workflows/monitoring.yml**
```yaml
# .github/workflows/monitoring.yml
name: Pipeline Monitoring

on:
  workflow_run:
    workflows: ["Continuous Integration", "Deployment Pipeline"]
    types: [completed]

jobs:
  collect-metrics:
    runs-on: ubuntu-latest
    if: always()
    
    steps:
    - name: Collect pipeline metrics
      run: |
        # Calculate pipeline duration and status
        WORKFLOW_STATUS="${{ github.event.workflow_run.conclusion }}"
        PIPELINE_ID="${{ github.event.workflow_run.id }}"
        
        echo "Pipeline ID: $PIPELINE_ID"
        echo "Status: $WORKFLOW_STATUS"
        echo "Repository: ${{ github.repository }}"
        echo "Branch: ${{ github.event.workflow_run.head_branch }}"
    
    # TODO: Send metrics to monitoring system
    - name: Send metrics
      run: |
        # This would typically send to Prometheus, DataDog, etc.
        echo "Sending pipeline metrics to monitoring system..."
        
        curl -X POST "${{ secrets.METRICS_ENDPOINT }}" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer ${{ secrets.METRICS_TOKEN }}" \
          -d '{
            "pipeline_id": "${{ github.event.workflow_run.id }}",
            "repository": "${{ github.repository }}",
            "branch": "${{ github.event.workflow_run.head_branch }}",
            "status": "${{ github.event.workflow_run.conclusion }}",
            "duration": "${{ github.event.workflow_run.updated_at - github.event.workflow_run.created_at }}",
            "triggered_by": "${{ github.event.workflow_run.actor.login }}"
          }' || echo "Metrics endpoint not configured"

  notify-teams:
    runs-on: ubuntu-latest
    if: always()
    
    steps:
    # TODO: Notify Slack on deployment
    - name: Slack Notification
      uses: 8398a7/action-slack@v3
      with:
        status: ${{ github.event.workflow_run.conclusion }}
        channel: '#deployments'
        webhook_url: ${{ secrets.SLACK_WEBHOOK }}
        fields: repo,message,commit,author,action,eventName,ref,workflow
        custom_payload: |
          {
            "attachments": [{
              "color": "${{ github.event.workflow_run.conclusion }}" === "success" ? "good" : "danger",
              "fields": [{
                "title": "Pipeline Status",
                "value": "${{ github.event.workflow_run.conclusion }}",
                "short": true
              }, {
                "title": "Repository",
                "value": "${{ github.repository }}",
                "short": true
              }, {
                "title": "Branch",
                "value": "${{ github.event.workflow_run.head_branch }}",
                "short": true
              }, {
                "title": "Workflow",
                "value": "${{ github.event.workflow_run.name }}",
                "short": true
              }]
            }]
          }
    
    # TODO: Send email notification for production deployments
    - name: Email Notification
      if: contains(github.event.workflow_run.name, 'Deployment') && github.event.workflow_run.head_branch == 'main'
      uses: dawidd6/action-send-mail@v3
      with:
        server_address: smtp.gmail.com
        server_port: 587
        username: ${{ secrets.EMAIL_USERNAME }}
        password: ${{ secrets.EMAIL_PASSWORD }}
        subject: "🚀 Production Deployment: ${{ github.event.workflow_run.conclusion }}"
        to: ${{ secrets.DEPLOYMENT_EMAIL_LIST }}
        from: CI/CD Pipeline <noreply@example.com>
        html_body: |
          <h2>Production Deployment Update</h2>
          <p><strong>Status:</strong> ${{ github.event.workflow_run.conclusion }}</p>
          <p><strong>Repository:</strong> ${{ github.repository }}</p>
          <p><strong>Branch:</strong> ${{ github.event.workflow_run.head_branch }}</p>
          <p><strong>Commit:</strong> ${{ github.event.workflow_run.head_sha }}</p>
          <p><strong>Workflow:</strong> ${{ github.event.workflow_run.name }}</p>
          <p><a href="${{ github.event.workflow_run.html_url }}">View Pipeline Details</a></p>
```

---

## 🛠️ Step 8: Configure Environment Protection Rules

### **📝 TODO: Create .github/environments/production.yml**
```yaml
# .github/environments/production.yml
# This would be configured through GitHub UI, but documented here for reference

# Environment Configuration
name: production

# Protection Rules
protection_rules:
  - type: required_reviewers
    required_reviewers:
      - devops-team
      - tech-lead
  
  - type: wait_timer
    minutes: 10
  
  - type: deployment_branch_policy
    protected_branches: true

# Environment Secrets (configured in GitHub UI)
secrets:
  - name: PROD_KUBECONFIG
    description: "Production Kubernetes configuration"
  
  - name: PROD_DATABASE_URL
    description: "Production database connection string"
  
  - name: PROD_JWT_SECRET
    description: "Production JWT signing secret"
  
  - name: SLACK_WEBHOOK
    description: "Slack webhook for notifications"
  
  - name: EMAIL_USERNAME
    description: "SMTP username for email notifications"
  
  - name: EMAIL_PASSWORD
    description: "SMTP password for email notifications"

# Environment Variables
variables:
  - name: ENVIRONMENT
    value: production
  
  - name: REPLICAS
    value: "3"
  
  - name: RESOURCE_LIMITS
    value: "2Gi"
  
  - name: LOG_LEVEL
    value: "WARN"
```

---

## 🛠️ Step 9: Create Deployment Scripts

### **📝 TODO: Create scripts/deploy.sh**
```bash
#!/bin/bash
# scripts/deploy.sh

set -e

# Configuration
ENVIRONMENT=${1:-development}
VERSION=${2:-latest}
NAMESPACE=${ENVIRONMENT}

echo "🚀 Deploying to $ENVIRONMENT environment..."
echo "Version: $VERSION"
echo "Namespace: $NAMESPACE"

# TODO: Validate environment
case $ENVIRONMENT in
  development|staging|production)
    echo "✅ Valid environment: $ENVIRONMENT"
    ;;
  *)
    echo "❌ Invalid environment: $ENVIRONMENT"
    echo "Valid options: development, staging, production"
    exit 1
    ;;
esac

# TODO: Check prerequisites
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "❌ docker is not installed"
    exit 1
fi

# TODO: Build and tag image
IMAGE_TAG="ghcr.io/your-org/cicd-workshop:$VERSION"
echo "📦 Building image: $IMAGE_TAG"

docker build -t $IMAGE_TAG .
docker push $IMAGE_TAG

# TODO: Deploy to Kubernetes
echo "🚢 Deploying to Kubernetes..."

kubectl set image deployment/cicd-workshop-$ENVIRONMENT \
  app=$IMAGE_TAG \
  --namespace=$NAMESPACE

# TODO: Wait for rollout
echo "⏳ Waiting for rollout to complete..."
kubectl rollout status deployment/cicd-workshop-$ENVIRONMENT \
  --namespace=$NAMESPACE \
  --timeout=300s

# TODO: Health check
echo "🏥 Running health checks..."
kubectl wait --for=condition=ready pod \
  -l app=cicd-workshop \
  --namespace=$NAMESPACE \
  --timeout=300s

# TODO: Get service URL
SERVICE_URL=$(kubectl get service cicd-workshop-$ENVIRONMENT-service \
  --namespace=$NAMESPACE \
  -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")

if [ "$SERVICE_URL" != "localhost" ]; then
  echo "🌐 Service URL: http://$SERVICE_URL:8080"
  
  # Health check
  if curl -f http://$SERVICE_URL:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Health check passed"
  else
    echo "❌ Health check failed"
    exit 1
  fi
else
  echo "⚠️  Service URL not available (using port-forward for testing)"
fi

echo "✅ Deployment completed successfully!"
```

### **📝 TODO: Create scripts/rollback.sh**
```bash
#!/bin/bash
# scripts/rollback.sh

set -e

ENVIRONMENT=${1:-development}
VERSION=${2:-""}
NAMESPACE=${ENVIRONMENT}

echo "🔄 Rolling back $ENVIRONMENT environment..."

# TODO: Validate environment
case $ENVIRONMENT in
  development|staging|production)
    echo "✅ Valid environment: $ENVIRONMENT"
    ;;
  *)
    echo "❌ Invalid environment: $ENVIRONMENT"
    exit 1
    ;;
esac

# TODO: Rollback deployment
if [ -n "$VERSION" ]; then
  echo "📋 Rolling back to version: $VERSION"
  IMAGE_TAG="ghcr.io/your-org/cicd-workshop:$VERSION"
  
  kubectl set image deployment/cicd-workshop-$ENVIRONMENT \
    app=$IMAGE_TAG \
    --namespace=$NAMESPACE
else
  echo "📋 Rolling back to previous version"
  kubectl rollout undo deployment/cicd-workshop-$ENVIRONMENT \
    --namespace=$NAMESPACE
fi

# TODO: Wait for rollback
echo "⏳ Waiting for rollback to complete..."
kubectl rollout status deployment/cicd-workshop-$ENVIRONMENT \
  --namespace=$NAMESPACE \
  --timeout=300s

# TODO: Verify rollback
echo "🔍 Verifying rollback..."
kubectl wait --for=condition=ready pod \
  -l app=cicd-workshop \
  --namespace=$NAMESPACE \
  --timeout=300s

echo "✅ Rollback completed successfully!"
```

---

## 🚀 Step 10: Test Your Complete CI/CD Pipeline

### **1. Initialize Repository**
```bash
cd class/workshop/lesson_18

# Initialize git repository
git init
git add .
git commit -m "Initial CI/CD pipeline setup"

# Add GitHub remote (replace with your repository)
git remote add origin https://github.com/your-username/kotlin-kickstarter-cicd.git
git push -u origin main
```

### **2. Configure GitHub Secrets**
```bash
# Repository secrets to configure in GitHub UI:
# - STAGING_KUBECONFIG: Base64 encoded kubeconfig for staging
# - PROD_KUBECONFIG: Base64 encoded kubeconfig for production
# - SLACK_WEBHOOK: Slack webhook URL for notifications
# - EMAIL_USERNAME: SMTP username
# - EMAIL_PASSWORD: SMTP password
# - METRICS_ENDPOINT: Monitoring system endpoint
# - METRICS_TOKEN: Authentication token for metrics
```

### **3. Test CI Pipeline**
```bash
# Create a feature branch and make changes
git checkout -b feature/test-pipeline

# Make a simple change
echo "// Test comment" >> src/main/kotlin/Application.kt
git add .
git commit -m "Test CI pipeline"
git push origin feature/test-pipeline

# Create pull request to trigger CI pipeline
```

### **4. Test Deployment Pipeline**
```bash
# Merge to develop to trigger development deployment
git checkout develop
git merge feature/test-pipeline
git push origin develop

# Merge to main to trigger staging deployment
git checkout main
git merge develop
git push origin main

# Create release to trigger production deployment
git tag v1.0.0
git push origin v1.0.0
```

### **5. Monitor Pipeline Execution**
```bash
# Check GitHub Actions
open "https://github.com/your-username/kotlin-kickstarter-cicd/actions"

# Monitor deployments
kubectl get deployments --all-namespaces
kubectl get pods --all-namespaces -l app=cicd-workshop

# Check application health
curl http://your-service-url/actuator/health
```

---

## 🎯 Expected Results

### **Pipeline Execution Times**
- **CI Pipeline**: 8-12 minutes for complete testing and building
- **Security Scanning**: 3-5 minutes for comprehensive security checks
- **Container Build**: 5-8 minutes for multi-stage Docker build
- **Deployment**: 2-5 minutes per environment with health checks

### **Quality Gates**
- **Test Coverage**: >80% with automated reporting
- **Security Scanning**: Zero high/critical vulnerabilities
- **Code Quality**: SonarCloud quality gate passing
- **Performance**: Load tests passing defined thresholds

### **Deployment Success Metrics**
```bash
# Successful deployment indicators
✅ All tests passing
✅ Security scans clean
✅ Container image built and signed
✅ Deployment completed without errors
✅ Health checks passing
✅ Notifications sent to team
```

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: GitOps Integration**
Implement ArgoCD or Flux for GitOps-based deployment management.

### **🔥 Bonus Challenge 2: Advanced Monitoring**
Add Prometheus monitoring with custom metrics and Grafana dashboards.

### **🔥 Bonus Challenge 3: Chaos Engineering**
Implement chaos testing in the pipeline with automated failure scenarios.

### **🔥 Bonus Challenge 4: Multi-Cloud Deployment**
Configure deployment to multiple cloud providers with traffic splitting.

### **🔥 Bonus Challenge 5: Progressive Delivery**
Implement feature flags and A/B testing integration in the deployment pipeline.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Built complete CI/CD automation** reducing deployment time from hours to minutes  
✅ **Implemented comprehensive testing** with unit, integration, security, and performance tests  
✅ **Created deployment strategies** supporting blue-green and canary deployments with rollback  
✅ **Established security gates** with automated vulnerability scanning and compliance checks  
✅ **Set up monitoring and observability** with pipeline metrics and team notifications  
✅ **Designed scalable automation** supporting multiple environments and deployment targets

**🚀 Next Lesson**: Cloud Deployment - deploying to production cloud environments with scaling and monitoring!