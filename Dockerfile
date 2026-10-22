# Root Dockerfile for Argo Workflows / Kaniko building from repo root context

# Stage 1: Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package descriptors explicitly (avoids Kaniko wildcard glob bug with package*.json)
COPY app/package.json app/package-lock.json ./

# Install dependencies
RUN npm ci || npm install

# Copy application source code from app/ directory
COPY app/ .

# Build production bundle using Vite
RUN npm run build

# Stage 2: Production web server stage
FROM nginxinc/nginx-unprivileged:1.30-alpine AS runner

# Copy built static assets from builder stage into Nginx html directory
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose HTTP port 8080 (nginx-unprivileged runs as uid 101)
EXPOSE 8080

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
