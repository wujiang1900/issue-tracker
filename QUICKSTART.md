# Issue Tracker - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Start the Application
```bash
docker-compose up
```

Wait for these messages:
- ✅ `mongodb    | Waiting for connections on port 27017`
- ✅ `issueTracker | Started IssueTrackerApplication`
- ✅ `issueTracker | Database seeding completed successfully!`

### Step 2: Open Swagger UI
Navigate to: **http://localhost:8080/swagger-ui.html**

### Step 3: Test the API

#### Option A: Using Swagger UI (Easiest)

1. **Login** to get JWT token:
   - Expand `Authentication` → `POST /api/auth/login`
   - Click "Try it out"
   - Use credentials: `john@example.com` / `password123`
   - Click "Execute"
   - Copy the `token` from response

2. **Authorize**:
   - Click the 🔒 "Authorize" button at top-right
   - Enter: `Bearer YOUR_TOKEN_HERE`
   - Click "Authorize" then "Close"

3. **Try Endpoints**:
   - `GET /api/projects` - View all projects
   - `GET /api/issues` - View all issues (already seeded with sample data)
   - `POST /api/issues` - Create a new issue

#### Option B: Using Postman

1. Import the collection: `IssueTracker-API.postman_collection.json`
2. Run the "Login" request (variables auto-update)
3. All authenticated requests will use the token automatically

#### Option C: Using cURL

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}' \
  | jq -r '.token')

# 2. Get all issues
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/issues?page=0&size=10

# 3. Get all projects
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/projects
```

---

## 📋 Pre-Seeded Test Data

The application comes with sample data ready to use:

### Test Users
| Email | Password | Role |
|-------|----------|------|
| admin@issuetracker.com | admin123 | ADMIN |
| john@example.com | password123 | PROJECT_OWNER |
| jane@example.com | password123 | USER |
| bob@example.com | password123 | USER |

### Sample Projects
- **Issue Tracker Application** (6 issues with various statuses)
- **E-Commerce Platform** (1 issue)

### Sample Issues
Issues include:
- Different statuses: OPEN, IN_PROGRESS, CLOSED
- Different priorities: LOW, MEDIUM, HIGH, CRITICAL
- Tags: backend, frontend, bug, feature, security
- Comments and activity logs

---

## 🔍 Common Use Cases

### 1. View All Open High-Priority Issues
```
GET /api/issues?status=OPEN&priority=HIGH
```

### 2. Search Issues by Text
```
GET /api/issues?search=authentication
```

### 3. Filter by Project
```
GET /api/issues?projectId=<PROJECT_ID>
```

### 4. Create a New Issue
```json
POST /api/issues
{
  "title": "Add dark mode support",
  "description": "Implement dark mode for better user experience",
  "status": "OPEN",
  "priority": "MEDIUM",
  "projectId": "<PROJECT_ID>",
  "tags": ["feature", "ui"]
}
```

### 5. Update Issue Status
```json
PUT /api/issues/<ISSUE_ID>
{
  "title": "Add dark mode support",
  "description": "Implement dark mode for better user experience",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "projectId": "<PROJECT_ID>",
  "tags": ["feature", "ui"]
}
```

### 6. Add Comment to Issue
```json
POST /api/issues/<ISSUE_ID>/comments
{
  "text": "Started working on this. Expected completion by Friday."
}
```

---

## 🌐 Real-Time Updates (WebSocket)

Connect to WebSocket for live issue updates:

```javascript
// JavaScript example
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
  // Subscribe to project updates
  stompClient.subscribe('/topic/issues/<PROJECT_ID>', function(message) {
    const event = JSON.parse(message.body);
    console.log('Issue event:', event.type, event.issue);
  });
  
  // Subscribe to specific issue updates
  stompClient.subscribe('/topic/issue/<ISSUE_ID>', function(message) {
    const event = JSON.parse(message.body);
    console.log('Issue updated:', event.issue);
  });
});
```

Events:
- `CREATED` - New issue created
- `UPDATED` - Issue modified or comment added
- `DELETED` - Issue removed

---

## 🔧 Troubleshooting

### Application won't start
```bash
# Check if ports are in use
netstat -ano | findstr :8080
netstat -ano | findstr :27017

# Kill process if needed (replace <PID> with actual PID)
taskkill /PID <PID> /F

# Restart
docker-compose down
docker-compose up
```

### Can't login / JWT token invalid
- Token expires after 24 hours
- Get a fresh token by logging in again
- Make sure you're using `Bearer <TOKEN>` format in Authorization header

### MongoDB connection issues
```bash
# Check MongoDB is running
docker ps | grep mongodb

# View MongoDB logs
docker logs mongodb

# Connect to MongoDB shell
docker exec -it mongodb mongosh
use issuetracker
db.users.find()
```

### Application logs
```bash
# View real-time logs
docker logs -f issueTracker

# View last 100 lines
docker logs --tail 100 issueTracker
```

---

## 🛑 Stop the Application

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (deletes all data)
docker-compose down -v
```

---

## 📝 Next Steps

1. **Explore Swagger UI**: Try all endpoints interactively
2. **Import Postman Collection**: Use the provided collection for API testing
3. **Connect Frontend**: Angular app can connect to `http://localhost:8080`
4. **WebSocket Integration**: Test real-time updates
5. **Read Full Documentation**: See `IMPLEMENTATION_SUMMARY.md` for complete details

---

## 💡 Tips

- Use Swagger UI's "Authorize" button to set your JWT token once for all requests
- The token is automatically refreshed when you login again
- All timestamps are in ISO-8601 format (UTC)
- Page numbers are 0-indexed (first page is 0)
- Default page size is 20 items

---

## 🆘 Need Help?

1. Check `IMPLEMENTATION_SUMMARY.md` for detailed documentation
2. View Swagger UI for API specifications
3. Check application logs: `docker logs issueTracker`
4. Verify MongoDB data: `docker exec -it mongodb mongosh`

---

**Happy Testing! 🎉**
