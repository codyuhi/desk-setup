# Root Dockerfile for Argo Workflows / Kaniko building from repo root context

# Stage 1: Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package descriptors from app/ directory
COPY app/package*.json ./

# Install dependencies
RUN npm ci || npm install

# Copy application source code from app/ directory
COPY app/ .

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
