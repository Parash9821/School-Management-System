# 🚀 School Management System - Hosting Guide

This guide covers deploying your application to **Vercel (Frontend)** and **Render (Backend)** for free.

---

## 📋 Prerequisites

- [GitHub](https://github.com) account (for hosting code)
- [Vercel](https://vercel.com) account (connected to GitHub)
- [Render](https://render.com) account (connected to GitHub)
- A MySQL database (e.g., [Clever Cloud](https://clever-cloud.com), [PlanetScale](https://planetscale.com), or [Supabase](https://supabase.com))

---

## 🖥️ Part 1: Backend → Render

### Step 1: Push Code to GitHub

```bash
# Initialize git (if not already done)
cd Backend
git init
git add .
git commit -m "Initial commit"

# Create GitHub repository and push
git remote add origin https://github.com/YOUR_USERNAME/school-management-system.git
git branch -M main
git push -u origin main
```

### Step 2: Configure Database

Since Render's free tier doesn't include MySQL, use one of these free alternatives:

| Service | Free Tier | Setup URL |
|---------|-----------|------------|
| **Clever Cloud** | 500MB | [clever-cloud.com](https://clever-cloud.com) |
| **PlanetScale** | 1 database | [planetscale.com](https://planetscale.com) |
| **Supabase** | 500MB | [supabase.com](https://supabase.com) |

### Step 3: Deploy to Render

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository
4. Configure the following:

| Setting | Value |
|---------|-------|
| **Name** | school-management-backend |
| **Root Directory** | Backend |
| **Build Command** | `./mvnw clean package -DskipTests` |
| **Start Command** | `java -jar target/School_Management_System-0.0.1-SNAPSHOT.jar` |
| **Instance Type** | Free |

### Step 4: Add Environment Variables

In Render, go to **"Environment"** tab and add:

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://YOUR_DB_HOST:3306/school_management_system
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

# JPA Configuration
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# JWT Secret (CHANGE THIS!)
APP_JWT_SECRET=YOUR_LONG_RANDOM_SECRET_32CHARS_MINIMUM

# Mail Configuration (optional)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
```

> ⚠️ **Important:** Generate a strong JWT secret (at least 32 characters).

### Step 5: Get Backend URL

After deployment, Render will provide a URL like:
```
https://school-management-backend.onrender.com
```

---

## 🌐 Part 2: Frontend → Vercel

### Step 1: Update API Configuration

Before deploying, update the frontend to point to your Render backend:

**File:** `frontend/src/api/api.js`

```javascript
// filepath: frontend/src/api/api.js
import axios from "axios";

const api = axios.create({
  // Change from localhost to your Render URL
  baseURL: "https://school-management-backend.onrender.com",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status;

    if (status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }

    return Promise.reject(err);
  }
);

export default api;
```

### Step 2: Push Updated Code to GitHub

```bash
cd frontend
git add .
git commit -m "Update API URL for production"
git push origin main
```

### Step 3: Deploy to Vercel

1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. Click **"Add New..."** → **"Project"**
3. Import your GitHub repository
4. Configure the following:

| Setting | Value |
|---------|-------|
| **Framework Preset** | Vite |
| **Root Directory** | frontend |
| **Build Command** | `npm run build` |
| **Output Directory** | `dist` |

5. Click **"Deploy"**

### Step 4: Get Frontend URL

Vercel will provide a URL like:
```
https://school-management-system.vercel.app
```

---

## 🔗 Connect Frontend to Backend

If you need to configure CORS on the backend for production:

**File:** `Backend/src/main/resources/application.properties`

Add this line (replace with your Vercel URL):
```properties
# For production - add your Vercel domain
# spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

Or configure CORS in your Spring Security configuration.

---

## ✅ Verification Steps

After deployment, test these:

1. **Backend Health:** Visit `https://your-backend.onrender.com/api/your-endpoints`
2. **Frontend Login:** Visit `https://your-frontend.vercel.app/login`
3. **API Connection:** Try logging in - should connect to backend successfully

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| **CORS Errors** | Add Vercel URL to backend CORS allowed origins |
| **Database Connection** | Verify environment variables in Render |
| **Build Fails** | Check Java version (use Java 17) |
| **502 Error** | Ensure start command is correct |

---

## 📝 Summary of Commands

### Backend (Render)
```bash
# Build command
./mvnw clean package -DskipTests

# Start command
java -jar target/School_Management_System-0.0.1-SNAPSHOT.jar
```

### Frontend (Vercel)
```bash
# Build command
npm run build

# Output directory
dist
```

---

**🎉 Your app is now live!**

- **Frontend:** `https://your-app.vercel.app`
- **Backend:** `https://your-app.onrender.com`