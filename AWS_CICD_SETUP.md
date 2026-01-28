# AWS CodePipeline CI/CD Setup Guide

## Overview

This project is configured for automated CI/CD using AWS CodePipeline, CodeBuild, and ECS/EC2 deployment.

## Architecture

```
GitHub → CodePipeline → CodeBuild → ECR → ECS/EC2
```

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **AWS CLI** installed and configured
3. **GitHub Repository** with webhook access
4. **Amazon ECR Repository** created
5. **ECS Cluster** or EC2 instances for deployment

## Setup Instructions

### 1. Create ECR Repository

```bash
aws ecr create-repository \
  --repository-name issue-tracker \
  --region us-east-1
```

### 2. Create CodeBuild Project

```bash
aws codebuild create-project \
  --name issue-tracker-build \
  --source type=GITHUB,location=https://github.com/YOUR_USERNAME/issue-tracker.git \
  --artifacts type=NO_ARTIFACTS \
  --environment type=LINUX_CONTAINER,image=aws/codebuild/standard:7.0,computeType=BUILD_GENERAL1_MEDIUM,privilegedMode=true \
  --service-role arn:aws:iam::ACCOUNT_ID:role/CodeBuildServiceRole \
  --region us-east-1
```

### 3. Create CodePipeline

```bash
aws codepipeline create-pipeline \
  --cli-input-json file://pipeline-config.json \
  --region us-east-1
```

**pipeline-config.json:**
```json
{
  "pipeline": {
    "name": "issue-tracker-pipeline",
    "roleArn": "arn:aws:iam::ACCOUNT_ID:role/CodePipelineServiceRole",
    "artifactStore": {
      "type": "S3",
      "location": "codepipeline-us-east-1-BUCKET"
    },
    "stages": [
      {
        "name": "Source",
        "actions": [
          {
            "name": "SourceAction",
            "actionTypeId": {
              "category": "Source",
              "owner": "ThirdParty",
              "provider": "GitHub",
              "version": "1"
            },
            "configuration": {
              "Owner": "YOUR_GITHUB_USERNAME",
              "Repo": "issue-tracker",
              "Branch": "main",
              "OAuthToken": "YOUR_GITHUB_TOKEN"
            },
            "outputArtifacts": [
              {
                "name": "SourceOutput"
              }
            ]
          }
        ]
      },
      {
        "name": "Build",
        "actions": [
          {
            "name": "BuildAction",
            "actionTypeId": {
              "category": "Build",
              "owner": "AWS",
              "provider": "CodeBuild",
              "version": "1"
            },
            "configuration": {
              "ProjectName": "issue-tracker-build"
            },
            "inputArtifacts": [
              {
                "name": "SourceOutput"
              }
            ],
            "outputArtifacts": [
              {
                "name": "BuildOutput"
              }
            ]
          }
        ]
      },
      {
        "name": "Deploy",
        "actions": [
          {
            "name": "DeployAction",
            "actionTypeId": {
              "category": "Deploy",
              "owner": "AWS",
              "provider": "ECS",
              "version": "1"
            },
            "configuration": {
              "ClusterName": "issue-tracker-cluster",
              "ServiceName": "issue-tracker-service",
              "FileName": "imagedefinitions.json"
            },
            "inputArtifacts": [
              {
                "name": "BuildOutput"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

### 4. Configure IAM Roles

#### CodeBuild Service Role (CodeBuildServiceRole)

Attach policies:
- `AWSCodeBuildAdminAccess`
- `AmazonEC2ContainerRegistryPowerUser`
- Custom policy for S3 artifacts

**Custom Policy:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "*"
    }
  ]
}
```

#### CodePipeline Service Role

Attach policies:
- `AWSCodePipelineFullAccess`
- `AmazonECS_FullAccess`

### 5. Setup ECS Task Definition

Create **task-definition.json:**
```json
{
  "family": "issue-tracker",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "issue-tracker",
      "image": "ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/issue-tracker:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "docker"
        },
        {
          "name": "SPRING_DATA_MONGODB_URI",
          "value": "mongodb://mongodb-service:27017/issuetracker"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/issue-tracker",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

Register task:
```bash
aws ecs register-task-definition \
  --cli-input-json file://task-definition.json
```

### 6. Create ECS Service

```bash
aws ecs create-service \
  --cluster issue-tracker-cluster \
  --service-name issue-tracker-service \
  --task-definition issue-tracker \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:region:account:targetgroup/issue-tracker/xxx,containerName=issue-tracker,containerPort=8080"
```

## Environment Variables

Set in AWS Systems Manager Parameter Store or Secrets Manager:

```bash
# JWT Secret
aws ssm put-parameter \
  --name /issuetracker/jwt-secret \
  --value "YOUR_SECRET_KEY_HERE" \
  --type SecureString

# MongoDB URI
aws ssm put-parameter \
  --name /issuetracker/mongodb-uri \
  --value "mongodb://admin:password@mongodb-host:27017/issuetracker" \
  --type SecureString
```

Update application-docker.yml to use:
```yaml
jwt:
  secret: ${JWT_SECRET}

spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

## Buildspec Files

### buildspec.yml (ECR Deployment)
- Default file for AWS ECR deployment
- Builds with Maven, creates Docker image, pushes to ECR
- Includes test reports and caching

### buildspec-dockerhub.yml (Docker Hub Deployment)
- Alternative for Docker Hub registry
- Uses Secrets Manager for credentials
- Suitable for non-AWS deployments

## Deployment Workflow

1. **Push to GitHub**: Developer commits to main branch
2. **Pipeline Triggers**: GitHub webhook triggers CodePipeline
3. **Source Stage**: CodePipeline pulls latest code
4. **Build Stage**: CodeBuild executes buildspec.yml
   - Runs Maven build
   - Executes unit tests
   - Builds Docker image
   - Pushes to ECR
5. **Deploy Stage**: ECS pulls new image and updates service
6. **Health Check**: ECS performs rolling deployment with health checks

## Monitoring

### CloudWatch Logs

```bash
# View build logs
aws logs tail /aws/codebuild/issue-tracker-build --follow

# View application logs
aws logs tail /ecs/issue-tracker --follow
```

### Build Status

```bash
# Get pipeline status
aws codepipeline get-pipeline-state \
  --name issue-tracker-pipeline

# Get build history
aws codebuild list-builds-for-project \
  --project-name issue-tracker-build
```

## Rollback

```bash
# Rollback to previous task definition
aws ecs update-service \
  --cluster issue-tracker-cluster \
  --service issue-tracker-service \
  --task-definition issue-tracker:PREVIOUS_REVISION
```

## Cost Optimization

1. **Use Fargate Spot** for non-production environments
2. **Enable build caching** (Maven dependencies cached)
3. **Schedule scaling** for ECS service based on traffic patterns
4. **Use reserved capacity** for predictable workloads

## Troubleshooting

### Build Fails

```bash
# Check build logs
aws codebuild batch-get-builds \
  --ids CODEBUILD_ID

# Common issues:
# - Insufficient ECR permissions
# - Maven build failures (check pom.xml)
# - Docker build context issues
```

### Deployment Fails

```bash
# Check ECS service events
aws ecs describe-services \
  --cluster issue-tracker-cluster \
  --services issue-tracker-service

# Common issues:
# - Health check failures
# - Port mapping conflicts
# - Environment variable issues
```

## Security Best Practices

1. ✅ Use IAM roles with least privilege
2. ✅ Store secrets in AWS Secrets Manager
3. ✅ Enable VPC for ECS tasks
4. ✅ Use security groups to restrict traffic
5. ✅ Enable container image scanning in ECR
6. ✅ Rotate credentials regularly
7. ✅ Enable AWS CloudTrail for audit logs

## Alternative: GitHub Actions

See `.github/workflows/deploy.yml` for GitHub Actions alternative to CodePipeline.

## Manual Deployment (Emergency)

```bash
# Build locally
docker build -t issue-tracker:manual .

# Tag for ECR
docker tag issue-tracker:manual ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/issue-tracker:manual

# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Push
docker push ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/issue-tracker:manual

# Update service
aws ecs update-service \
  --cluster issue-tracker-cluster \
  --service issue-tracker-service \
  --force-new-deployment
```

## Resources

- [AWS CodePipeline Documentation](https://docs.aws.amazon.com/codepipeline/)
- [AWS CodeBuild Documentation](https://docs.aws.amazon.com/codebuild/)
- [Amazon ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Amazon ECR Documentation](https://docs.aws.amazon.com/ecr/)

---

**Note**: Replace placeholders like `ACCOUNT_ID`, `YOUR_USERNAME`, `YOUR_GITHUB_TOKEN` with actual values.
