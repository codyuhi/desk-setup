# Multi-stage Dockerfile for DESKForge Studio Web Application

# Stage 1: Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package descriptors explicitly (avoids Kaniko wildcard glob bug with package*.json)
COPY package.json package-lock.json ./

# Install dependencies cleanly
RUN npm ci || npm install

# Copy application source code
COPY . .

# Build production bundle using Vite
RUN npm run build

# Stage 2: Production web server stage
FROM nginx:1.25-alpine AS runner

# Copy built static assets from builder stage into Nginx html directory
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose HTTP port 80
EXPOSE 80

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
