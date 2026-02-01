# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app

ARG VITE_API_URL
# 2. Make it available to the build process
ENV VITE_API_URL=$VITE_API_URL

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

