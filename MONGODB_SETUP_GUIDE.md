# MongoDB Setup Guide for Issue Tracker

## ✅ Changes Made

The project has been updated to use **external MongoDB** instead of embedded MongoDB:

### Removed:
- ❌ `de.flapdoodle.embed.mongo.spring30x` dependency from pom.xml
- ❌ Embedded MongoDB configuration from application-test.yml
- ❌ `@Disabled` annotations from all MongoDB-dependent tests

### What This Means:
✅ No more SSL/download issues with embedded MongoDB  
✅ All tests can now run (once MongoDB is installed)  
✅ Better performance with native MongoDB  
✅ Same MongoDB instance can be used for development and testing  

---

## MongoDB Installation & Setup

### Option 1: MongoDB Community Edition (Recommended)

#### Step 1: Download MongoDB
1. Visit: https://www.mongodb.com/try/download/community
2. Select:
   - **Version:** 6.0 or 7.0 (both compatible)
   - **Platform:** Windows
   - **Package:** MSI
3. Download and run the installer

#### Step 2: Install MongoDB
1. Run the MSI installer
2. Choose **"Complete"** installation
3. **Install MongoDB as a Service** (recommended - check this option)
4. Use default data directory: `C:\Program Files\MongoDB\Server\{version}\data`
5. Complete the installation

#### Step 3: Verify Installation
Open PowerShell and run:
```powershell
# Check if MongoDB service is running
Get-Service MongoDB

# Should show:
# Status   Name               DisplayName
# ------   ----               -----------
# Running  MongoDB            MongoDB

# Connect to MongoDB (should work if service is running)
mongosh
# Type 'exit' to quit mongosh
```

#### Step 4: Start MongoDB (if not running as service)
```powershell
# If MongoDB is not running as a service, start it manually:
mongod --dbpath "C:\data\db"

# Or if installed as service:
net start MongoDB
```

---

### Option 2: MongoDB with Docker (If Docker Available)

#### Step 1: Pull and Run MongoDB
```powershell
# Pull MongoDB image
docker pull mongo:6.0

# Run MongoDB container
docker run -d `
  --name mongodb-test `
  -p 27017:27017 `
  -e MONGO_INITDB_DATABASE=issuetracker_test `
  mongo:6.0

# Verify it's running
docker ps | Select-String mongodb
```

#### Step 2: Verify Connection
```powershell
# Connect to MongoDB in Docker
docker exec -it mongodb-test mongosh

# Or use mongosh locally
mongosh mongodb://localhost:27017/issuetracker_test
```

---

## Configuration

### Current Test Configuration
The project is already configured to use external MongoDB in `src/test/resources/application-test.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/issuetracker_test
```

### Alternative Configurations

#### With Authentication
If your MongoDB requires authentication:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://username:password@localhost:27017/issuetracker_test
```

#### Remote MongoDB
If using MongoDB on a different machine:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://192.168.1.100:27017/issuetracker_test
```

#### MongoDB Atlas (Cloud)
If using MongoDB Atlas:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://username:password@cluster.mongodb.net/issuetracker_test?retryWrites=true&w=majority
```

---

## Running Tests

### All Tests
```powershell
mvn test
```

### Specific Test Classes
```powershell
# Run only unit tests (no MongoDB needed)
mvn test -Dtest=IssueServiceTest,ProjectServiceTest

# Run MongoDB-dependent tests
mvn test -Dtest=IssueRepositoryTest,IssueTrackerIntegrationTest

# Run all tests
mvn clean test
```

### Build Project
```powershell
mvn clean package
```

---

## Test Database Management

### Automatic Cleanup
Tests use a separate database (`issuetracker_test`) and clean up after themselves using:
- `@BeforeEach` methods that clear collections
- Spring's `@Transactional` (where applicable)

### Manual Cleanup (if needed)
```powershell
# Connect to MongoDB
mongosh

# Switch to test database
use issuetracker_test

# Drop the test database
db.dropDatabase()

# Exit
exit
```

---

## Troubleshooting

### MongoDB Service Not Starting
```powershell
# Check service status
Get-Service MongoDB

# If stopped, start it
Start-Service MongoDB

# Check logs (if issues)
Get-Content "C:\Program Files\MongoDB\Server\{version}\log\mongod.log" -Tail 50
```

### Connection Refused
```powershell
# Check if MongoDB is listening on port 27017
netstat -ano | Select-String "27017"

# Should show something like:
# TCP    0.0.0.0:27017    0.0.0.0:0    LISTENING    1234
```

### Can't Connect to MongoDB
1. **Check MongoDB is running:**
   ```powershell
   Get-Service MongoDB
   # Or for Docker:
   docker ps | Select-String mongodb
   ```

2. **Test connection:**
   ```powershell
   mongosh mongodb://localhost:27017
   ```

3. **Check firewall** (if remote MongoDB):
   - Ensure port 27017 is open
   - Check Windows Firewall settings

### Tests Still Failing
1. **Verify MongoDB is running:** `Get-Service MongoDB`
2. **Check connection string** in `application-test.yml`
3. **Check test database exists:** `mongosh` → `show dbs`
4. **Clear test database:** `use issuetracker_test` → `db.dropDatabase()`

---

## Quick Start Commands

```powershell
# 1. Install MongoDB Community Edition (one-time setup)
# Download from: https://www.mongodb.com/try/download/community

# 2. Verify MongoDB is running
Get-Service MongoDB

# 3. Run tests
cd C:\Users\rc19636\IdeaProjects\issue-tracker
mvn test

# 4. Build project
mvn clean package
```

---

## Benefits of External MongoDB

✅ **No corporate network issues** - No external downloads needed  
✅ **Better performance** - Native MongoDB is faster than embedded  
✅ **Easier debugging** - Can inspect data using mongosh or Compass  
✅ **Realistic testing** - Tests use same MongoDB as production  
✅ **Persistent data** - Can keep test data for debugging (if needed)  
✅ **Shared instance** - Same MongoDB for dev, test, and local prod  

---

## MongoDB Tools

### MongoDB Compass (GUI)
- Download: https://www.mongodb.com/try/download/compass
- Visual interface to browse and manage MongoDB
- Connect to: `mongodb://localhost:27017`

### mongosh (CLI)
- Included with MongoDB Community Edition
- Command-line interface for MongoDB
- More powerful than Compass for scripting

---

## Summary

Your project is now configured to use external MongoDB! 🎉

**Next Steps:**
1. ✅ Install MongoDB Community Edition
2. ✅ Verify service is running: `Get-Service MongoDB`
3. ✅ Run tests: `mvn test`
4. ✅ Build project: `mvn clean package`

All tests should now pass once MongoDB is installed and running!
