# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json .
COPY package-lock.json .
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the application using Nginx
FROM nginx:stable-alpine
# KLJUČNI POPRAVAK: Mijenjamo /app/build u /app/dist
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```
---

### Završni Korak

**AKCIJA:** Ažurirajte **`frontend/Dockerfile`** ovim kodom, spremite ga i gurnite promjene:

```bash
git add frontend/Dockerfile .
git commit -m "fix: Frontend Dockerfile path from /app/build to /app/dist"
git push origin develop