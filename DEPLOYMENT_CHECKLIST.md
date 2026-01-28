# Deployment Checklist

## Pre-Deployment Checklist

### 🔧 Development
- [ ] All features implemented and tested locally
- [ ] Unit tests passing (`mvn test`)
- [ ] Integration tests passing
- [ ] Code reviewed and approved
- [ ] No hardcoded credentials or secrets
- [ ] Environment-specific configs externalized
- [ ] Docker build successful (`docker build -t issue-tracker .`)
- [ ] Docker compose works (`docker-compose up`)

### 📦 Build & Dependencies
- [ ] Maven build successful (`mvn clean package`)
- [ ] All dependencies updated to stable versions
- [ ] No critical security vulnerabilities (run `mvn dependency-check:check`)
- [ ] JAR file size reasonable (<50MB)
- [ ] Docker image size optimized (<200MB)

### 🔐 Security
- [ ] JWT secret changed from default (use strong random string)
- [ ] MongoDB credentials secured
- [ ] CORS origins configured for production domain
- [ ] SSL/TLS certificate obtained (for HTTPS)
- [ ] API rate limiting considered
- [ ] Input validation on all endpoints
- [ ] SQL/NoSQL injection prevention verified
- [ ] XSS protection enabled

### 🗄️ Database
- [ ] MongoDB instance provisioned
- [ ] Database connection string configured
- [ ] Database indexes created (compound index on issues)
- [ ] Backup strategy defined
- [ ] Seed data strategy decided (enable/disable in production)

### ☁️ Infrastructure
- [ ] AWS account setup (if using AWS)
- [ ] ECR repository created
- [ ] ECS cluster provisioned
- [ ] VPC and subnets configured
- [ ] Security groups defined
- [ ] Load balancer created
- [ ] Domain name registered
- [ ] DNS configured

---

## AWS CodePipeline Deployment

### Step 1: AWS Prerequisites
- [ ] AWS CLI installed and configured
- [ ] IAM user/role created with appropriate permissions
- [ ] ECR repository created: `issue-tracker`
- [ ] ECS cluster created: `issue-tracker-cluster`
- [ ] S3 bucket for CodePipeline artifacts

### Step 2: Create Build Project
```bash
# Run this command
aws codebuild create-project \
  --name issue-tracker-build \
  --source type=GITHUB,location=https://github.com/YOUR_USERNAME/issue-tracker.git \
  --artifacts type=NO_ARTIFACTS \
  --environment type=LINUX_CONTAINER,image=aws/codebuild/standard:7.0,computeType=BUILD_GENERAL1_MEDIUM,privilegedMode=true \
  --service-role arn:aws:iam::ACCOUNT_ID:role/CodeBuildServiceRole
```

**Verify**:
- [ ] CodeBuild project visible in AWS Console
- [ ] Service role has ECR push permissions
- [ ] GitHub webhook configured

### Step 3: Create Pipeline
- [ ] CodePipeline created with 3 stages (Source, Build, Deploy)
- [ ] GitHub source configured with OAuth token
- [ ] Build stage linked to CodeBuild project
- [ ] Deploy stage configured for ECS

### Step 4: Configure Secrets
```bash
# Store JWT secret
aws secretsmanager create-secret \
  --name /issuetracker/jwt-secret \
  --secret-string "YOUR_STRONG_RANDOM_SECRET_HERE"

# Store MongoDB URI
aws secretsmanager create-secret \
  --name /issuetracker/mongodb-uri \
  --secret-string "mongodb://admin:password@mongodb-host:27017/issuetracker"
```

**Verify**:
- [ ] Secrets created in AWS Secrets Manager
- [ ] ECS task definition references secrets
- [ ] IAM role can read secrets

### Step 5: Deploy ECS Service
- [ ] Task definition registered
- [ ] Service created with desired count = 2
- [ ] Load balancer configured
- [ ] Target group health checks passing
- [ ] Auto-scaling policies configured

### Step 6: Test Pipeline
- [ ] Push commit to main branch
- [ ] Pipeline triggers automatically
- [ ] Source stage completes successfully
- [ ] Build stage: Maven build succeeds
- [ ] Build stage: Docker image pushed to ECR
- [ ] Deploy stage: ECS service updated
- [ ] Health checks passing
- [ ] Application accessible via load balancer

---

## GitHub Actions Deployment

### Step 1: GitHub Setup
- [ ] Repository created on GitHub
- [ ] Code pushed to repository
- [ ] Workflow file exists: `.github/workflows/ci-cd.yml`

### Step 2: Configure GitHub Secrets
Navigate to: Repository → Settings → Secrets and variables → Actions

Add these secrets:
- [ ] `DOCKER_USERNAME` - Docker Hub username
- [ ] `DOCKER_PASSWORD` - Docker Hub access token
- [ ] `AWS_ACCESS_KEY_ID` - AWS credentials
- [ ] `AWS_SECRET_ACCESS_KEY` - AWS credentials
- [ ] `AWS_REGION` - e.g., us-east-1
- [ ] `AWS_ACCOUNT_ID` - Your AWS account ID

### Step 3: Configure Environments
- [ ] Create `staging` environment
- [ ] Create `production` environment
- [ ] Add protection rules (require approvals)
- [ ] Set environment-specific secrets

### Step 4: Test Workflow
- [ ] Push commit to develop branch
- [ ] GitHub Actions workflow triggers
- [ ] Test job passes
- [ ] Build job completes
- [ ] Docker build succeeds
- [ ] Image pushed to registries
- [ ] Staging deployment successful

### Step 5: Production Deployment
- [ ] Merge to main branch (or push directly)
- [ ] Production workflow triggers
- [ ] All checks pass
- [ ] Production deployment successful
- [ ] Verify application at production URL

---

## Post-Deployment Verification

### Application Health
- [ ] Health check endpoint responding: `GET /actuator/health`
- [ ] API accessible: `GET /api/projects` (with auth)
- [ ] Swagger UI working: `/swagger-ui.html`
- [ ] WebSocket connections successful: `/ws`

### Functionality Tests
- [ ] User signup works
- [ ] User login returns JWT token
- [ ] Protected endpoints require authentication
- [ ] Create project successful
- [ ] Create issue successful
- [ ] Search/filter issues working
- [ ] Add comment to issue working
- [ ] Real-time WebSocket updates functioning

### Performance Tests
- [ ] API response time <500ms for simple queries
- [ ] Pagination working for large datasets
- [ ] Database queries optimized (check logs)
- [ ] Memory usage stable (<1GB)
- [ ] CPU usage reasonable (<50% average)

### Monitoring Setup
- [ ] CloudWatch logs receiving data
- [ ] CloudWatch alarms configured
- [ ] Error rate monitoring enabled
- [ ] Custom metrics (if any) working
- [ ] Dashboard created for key metrics

### Security Verification
- [ ] HTTPS enabled (not HTTP)
- [ ] CORS only allows intended origins
- [ ] Authentication working correctly
- [ ] Authorization roles enforced
- [ ] No sensitive data in logs
- [ ] Error messages don't leak information

---

## Smoke Tests (Manual)

### Test 1: Authentication Flow
```bash
# Signup
curl -X POST https://your-domain.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@test.com","password":"test123"}'

# Expected: 200 OK with JWT token

# Login
curl -X POST https://your-domain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# Expected: 200 OK with JWT token
```

### Test 2: Create Project
```bash
TOKEN="your-jwt-token"

curl -X POST https://your-domain.com/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Project","description":"Test"}'

# Expected: 201 Created with project object
```

### Test 3: Create Issue
```bash
curl -X POST https://your-domain.com/api/issues \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Test Issue",
    "description":"Testing",
    "status":"OPEN",
    "priority":"HIGH",
    "projectId":"PROJECT_ID",
    "tags":["test"]
  }'

# Expected: 201 Created with issue object
```

### Test 4: Search Issues
```bash
curl "https://your-domain.com/api/issues?status=OPEN&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with paginated results
```

---

## Rollback Procedure

### If Deployment Fails

#### AWS ECS Rollback
```bash
# List task definitions
aws ecs list-task-definitions --family-prefix issue-tracker

# Rollback to previous version
aws ecs update-service \
  --cluster issue-tracker-cluster \
  --service issue-tracker-service \
  --task-definition issue-tracker:PREVIOUS_REVISION \
  --force-new-deployment

# Wait for rollback
aws ecs wait services-stable \
  --cluster issue-tracker-cluster \
  --services issue-tracker-service
```

#### GitHub Actions Rollback
1. [ ] Navigate to repository
2. [ ] Find last working commit
3. [ ] Create new branch from that commit
4. [ ] Push branch to trigger deployment
5. [ ] Or: Revert the breaking commit

### Emergency Rollback
If all else fails:
```bash
# Stop the service
aws ecs update-service \
  --cluster issue-tracker-cluster \
  --service issue-tracker-service \
  --desired-count 0

# Deploy known-good version manually
# Then restart service with desired count
```

---

## Monitoring & Alerting

### Alarms to Configure
- [ ] API error rate >5% (5 minutes)
- [ ] Response time >1000ms (5 minutes)
- [ ] Memory usage >80% (10 minutes)
- [ ] CPU usage >80% (10 minutes)
- [ ] Health check failures (3 consecutive)
- [ ] Database connection failures

### Logs to Monitor
- [ ] Application logs: `/ecs/issue-tracker`
- [ ] Build logs: `/aws/codebuild/issue-tracker-build`
- [ ] Access logs: Load balancer logs
- [ ] Error logs: Filter for ERROR/WARN levels

---

## Maintenance Tasks

### Daily
- [ ] Check CloudWatch dashboard
- [ ] Review error logs
- [ ] Monitor response times

### Weekly
- [ ] Review failed deployments
- [ ] Check disk/memory usage trends
- [ ] Update dependencies (if needed)
- [ ] Review security alerts

### Monthly
- [ ] Analyze cost reports
- [ ] Review and optimize resources
- [ ] Update documentation
- [ ] Security audit

---

## Documentation

- [ ] API documentation up to date (Swagger)
- [ ] README updated with deployment info
- [ ] Runbook created for operations team
- [ ] Architecture diagram created
- [ ] Disaster recovery plan documented

---

## Sign-off

**Deployed by**: _________________  
**Date**: _________________  
**Version**: _________________  
**Environment**: [ ] Staging [ ] Production  
**Verified by**: _________________  

### Notes
```
Add any deployment notes, issues encountered, or special configurations here.
```

---

## Quick Reference

### Important URLs
- **Production API**: https://api.issuetracker.example.com
- **Swagger UI**: https://api.issuetracker.example.com/swagger-ui.html
- **Health Check**: https://api.issuetracker.example.com/actuator/health
- **AWS Console**: https://console.aws.amazon.com/ecs/
- **GitHub Actions**: https://github.com/username/repo/actions

### Important Commands
```bash
# Check service status
aws ecs describe-services --cluster issue-tracker-cluster --services issue-tracker-service

# View logs
aws logs tail /ecs/issue-tracker --follow

# Force new deployment
aws ecs update-service --cluster issue-tracker-cluster --service issue-tracker-service --force-new-deployment

# Scale service
aws ecs update-service --cluster issue-tracker-cluster --service issue-tracker-service --desired-count 4
```

---

**✅ Deployment Complete!** 

Make sure all items are checked before marking deployment as successful.
