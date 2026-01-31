# CI/CD Configuration Summary

## Overview

The Issue Tracker application now has **3 CI/CD deployment options**:

1. **AWS CodePipeline** (Primary) - Enterprise-grade AWS native solution
2. **GitHub Actions** (Alternative) - Modern GitHub-integrated workflows
3. **Manual Docker** (Backup) - Direct deployment option

---

## Files Created

### 1. buildspec.yml
**Purpose**: AWS CodeBuild configuration for ECR deployment

**Features**:
- ✅ Java 17 (Corretto) runtime
- ✅ Maven build with dependency caching
- ✅ Unit test execution with JUnit reports
- ✅ Docker multi-stage build
- ✅ Automatic ECR login and image push
- ✅ Git commit-based image tagging
- ✅ ECS deployment artifacts (imagedefinitions.json)

**When to use**: 
- AWS-hosted applications
- Amazon ECR + ECS/EKS deployments
- Enterprise environments with AWS infrastructure

### 2. buildspec-dockerhub.yml
**Purpose**: Alternative buildspec for Docker Hub registry

**Features**:
- ✅ Docker Hub authentication via Secrets Manager
- ✅ Public/private registry support
- ✅ Simplified deployment for non-AWS environments

**When to use**:
- Docker Hub as container registry
- Multi-cloud deployments
- Open-source projects with public images

### 3. .github/workflows/ci-cd.yml
**Purpose**: GitHub Actions workflow for automated CI/CD

**Features**:
- ✅ Multi-job pipeline (test → build → docker → deploy)
- ✅ Parallel builds for AMD64 and ARM64
- ✅ Docker layer caching
- ✅ Staging and production environments
- ✅ Security scanning with Trivy
- ✅ Test result reporting
- ✅ Automatic deployment on branch push

**When to use**:
- GitHub-hosted repositories
- Teams already using GitHub ecosystem
- Need for free CI/CD minutes
- Multi-platform container builds

### 4. AWS_CICD_SETUP.md
**Purpose**: Comprehensive AWS deployment guide

**Includes**:
- Step-by-step AWS setup instructions
- IAM role configurations
- ECS task definitions
- CodePipeline JSON templates
- Environment variable management
- Monitoring and troubleshooting guides
- Security best practices

---

## Quick Comparison

| Feature | AWS CodePipeline | GitHub Actions |
|---------|------------------|----------------|
| **Cost** | Pay per minute | Free tier (2000 min/month) |
| **Setup** | Complex (IAM, roles) | Simple (YAML only) |
| **Integration** | AWS native | GitHub native |
| **Best for** | AWS deployments | Multi-cloud |
| **Monitoring** | CloudWatch | GitHub UI |
| **Artifact Storage** | S3 + ECR | GitHub Packages + ECR |

---

## Setup Instructions

### Option 1: AWS CodePipeline (Recommended for Production)

1. **Create ECR Repository**:
   ```bash
   aws ecr create-repository --repository-name issue-tracker --region us-east-1
   ```

2. **Create CodeBuild Project**:
   - Source: GitHub repository
   - Buildspec: Use `buildspec.yml` from repository
   - Environment: Linux, Standard 7.0, Privileged mode enabled

3. **Create CodePipeline**:
   - Source: GitHub (with webhook)
   - Build: CodeBuild project created above
   - Deploy: Amazon ECS

4. **Configure IAM Roles**:
   - CodeBuild role: ECR push, S3 access, CloudWatch logs
   - CodePipeline role: ECS deploy, S3 access

5. **Setup ECS**:
   - Create cluster, task definition, and service
   - Configure load balancer and health checks

**Full details**: See `AWS_CICD_SETUP.md`

### Option 2: GitHub Actions

1. **Add GitHub Secrets**:
   - `DOCKER_USERNAME` - Docker Hub username
   - `DOCKER_PASSWORD` - Docker Hub token
   - `AWS_ACCESS_KEY_ID` - AWS credentials
   - `AWS_SECRET_ACCESS_KEY` - AWS credentials
   - `AWS_REGION` - Target AWS region
   - `AWS_ACCOUNT_ID` - Your AWS account ID

2. **Enable GitHub Actions**:
   - Workflow file is already at `.github/workflows/ci-cd.yml`
   - Push to `main` or `develop` branch to trigger

3. **Configure Environments** (Optional):
   - Create `staging` and `production` environments in GitHub
   - Add protection rules and approvals

**That's it!** GitHub Actions will automatically:
- Run tests on every PR
- Build Docker images on branch push
- Deploy to staging (develop branch)
- Deploy to production (main branch)

### Option 3: Manual Docker Deployment

```bash
# Build
docker build -t issue-tracker:1.0 .

# Tag for registry
docker tag issue-tracker:1.0 your-registry/issue-tracker:1.0

# Push
docker push your-registry/issue-tracker:1.0
```

---

## Pipeline Workflow

### AWS CodePipeline Flow
```
1. GitHub Commit
   ↓
2. CodePipeline Triggered (webhook)
   ↓
3. Source Stage: Pull code from GitHub
   ↓
4. Build Stage: CodeBuild executes buildspec.yml
   ├── Install Java 17 & Maven
   ├── Run Maven build (mvn clean package)
   ├── Run tests (mvn test)
   ├── Build Docker image
   └── Push to ECR
   ↓
5. Deploy Stage: Update ECS service
   ├── Pull new image from ECR
   ├── Update task definition
   ├── Rolling deployment
   └── Health checks
```

### GitHub Actions Flow
```
1. GitHub Push/PR
   ↓
2. Workflow Triggered
   ↓
3. Test Job: Run unit tests
   ├── Checkout code
   ├── Setup Java 17
   ├── Run mvn test
   └── Upload test results
   ↓
4. Build Job: Build JAR
   ├── Maven package
   └── Upload artifact
   ↓
5. Docker Build Job: Create image
   ├── Multi-platform build
   ├── Push to Docker Hub
   └── Push to ECR
   ↓
6. Deploy Job: Update ECS
   ├── AWS credentials
   ├── Update service
   └── Wait for stability
```

---

## Environment Variables

Both pipelines require these environment variables:

### Application Environment
```yaml
SPRING_PROFILES_ACTIVE: docker
SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/issuetracker
JWT_SECRET: <from-secrets-manager>
```

### Build Environment
```yaml
JAVA_HOME: /usr/lib/jvm/java-17-amazon-corretto
MAVEN_OPTS: -Xmx1024m
AWS_REGION: us-east-1
AWS_ACCOUNT_ID: <your-account-id>
```

---

## Monitoring & Logs

### AWS CodePipeline
```bash
# Pipeline status
aws codepipeline get-pipeline-state --name issue-tracker-pipeline

# Build logs
aws logs tail /aws/codebuild/issue-tracker-build --follow

# Application logs
aws logs tail /ecs/issue-tracker --follow
```

### GitHub Actions
- View in GitHub UI: Actions tab
- Download artifacts: Build artifacts page
- Real-time logs: Click on running workflow

---

## Cost Estimation

### AWS CodePipeline
- **Pipeline**: $1/month per active pipeline
- **CodeBuild**: $0.005/min (general1.medium)
- **ECR Storage**: $0.10/GB/month
- **ECS**: Depends on Fargate/EC2 pricing
- **Estimated**: ~$20-50/month for small project

### GitHub Actions
- **Free Tier**: 2000 minutes/month (public repos unlimited)
- **Paid**: $0.008/min after free tier
- **Estimated**: Free for most projects

---

## Security Considerations

### Implemented
✅ Docker image scanning (Trivy in GitHub Actions)
✅ Secrets stored in AWS Secrets Manager / GitHub Secrets
✅ IAM roles with least privilege
✅ Private ECR repository
✅ VPC isolation for ECS tasks
✅ HTTPS only (via load balancer)

### Recommended Additions
🔲 Enable AWS GuardDuty for threat detection
🔲 Add SAST tools (SonarQube, Snyk)
🔲 Implement image signing (Docker Content Trust)
🔲 Add runtime security (Falco, AWS Security Hub)
🔲 Enable CloudTrail for audit logs

---

## Rollback Strategy

### AWS ECS Rollback
```bash
# View task definition revisions
aws ecs list-task-definitions --family-prefix issue-tracker

# Rollback to previous revision
aws ecs update-service \
  --cluster issue-tracker-production \
  --service issue-tracker-service \
  --task-definition issue-tracker:PREVIOUS_REVISION \
  --force-new-deployment
```

### GitHub Actions Rollback
- Revert commit in GitHub
- Workflow automatically redeploys previous version
- Or manually trigger workflow on older commit

---

## Testing the Pipeline

### Test AWS CodePipeline
1. Make a small change to README.md
2. Commit and push to main branch
3. Check AWS Console → CodePipeline
4. Monitor build progress
5. Verify deployment in ECS

### Test GitHub Actions
1. Make a small change to any file
2. Push to main or create PR
3. Go to GitHub → Actions tab
4. Watch workflow execution
5. Check deployment logs

---

## Troubleshooting

### Build Fails
**Symptom**: Maven build error
**Solution**: 
- Check Java version (must be 17)
- Verify pom.xml dependencies
- Clear Maven cache

**Symptom**: Docker build error
**Solution**:
- Check Dockerfile syntax
- Verify base image availability
- Enable privileged mode in CodeBuild

### Deployment Fails
**Symptom**: ECS service won't stabilize
**Solution**:
- Check health check endpoint
- Verify environment variables
- Check security group rules
- Review CloudWatch logs

**Symptom**: ECR push unauthorized
**Solution**:
- Verify IAM permissions
- Check ECR repository exists
- Re-authenticate with ECR

---

## Next Steps

1. **Setup MongoDB in AWS**:
   - Use Amazon DocumentDB (MongoDB-compatible)
   - Or deploy MongoDB on ECS/EKS
   - Configure connection string in Secrets Manager

2. **Add Application Load Balancer**:
   - Create ALB with HTTPS listener
   - Configure SSL certificate (ACM)
   - Setup health checks on `/actuator/health`

3. **Enable Auto-scaling**:
   - ECS service auto-scaling based on CPU/memory
   - ALB target tracking scaling

4. **Add Monitoring**:
   - CloudWatch dashboards
   - Custom metrics from application
   - Set up alarms for errors

5. **Implement Blue/Green Deployment**:
   - Use CodeDeploy for zero-downtime updates
   - Configure traffic shifting strategies

---

## Resources

- **AWS CodePipeline**: https://docs.aws.amazon.com/codepipeline/
- **GitHub Actions**: https://docs.github.com/en/actions
- **Docker Best Practices**: https://docs.docker.com/develop/dev-best-practices/
- **ECS Deployment**: https://docs.aws.amazon.com/AmazonECS/latest/developerguide/

---

**Status**: ✅ CI/CD Configuration Complete - Ready for Production Deployment
