# 🚀 Quick Start - MongoDB Setup

## 1️⃣ Install MongoDB (5 minutes)

### Download & Install:
1. Go to: https://www.mongodb.com/try/download/community
2. Download Windows MSI installer
3. Run installer, choose **"Complete"** installation
4. ✅ Check **"Install as Windows Service"**
5. Click Install

---

## 2️⃣ Verify Installation (30 seconds)

```powershell
# Check service is running
Get-Service MongoDB

# Should show:
# Status   Name      DisplayName
# ------   ----      -----------
# Running  MongoDB   MongoDB
```

---

## 3️⃣ Run Tests (1 minute)

```powershell
cd C:\Users\rc19636\IdeaProjects\issue-tracker
mvn test
```

**Expected Result:** ✅ All tests pass!

---

## 4️⃣ Build Project

```powershell
mvn clean package
```

---

## 🆘 Quick Troubleshooting

### MongoDB Not Running?
```powershell
# Start it
net start MongoDB

# Or check status
Get-Service MongoDB
```

### Tests Failing?
```powershell
# Restart MongoDB
net stop MongoDB
net start MongoDB

# Clear test database
mongosh
use issuetracker_test
db.dropDatabase()
exit
```

---

## ✅ What Changed

- ❌ **Removed:** Embedded MongoDB (was causing SSL errors)
- ✅ **Added:** External MongoDB connection
- ✅ **Result:** All tests can now run!

---

## 📚 More Help

- **Detailed Setup:** See `MONGODB_SETUP_GUIDE.md`
- **Full Migration Info:** See `MIGRATION_COMPLETE.md`

---

## 🎯 Summary

1. Install MongoDB (5 min)
2. Run `mvn test` ✅
3. You're done! 🎉
