#!/bin/bash

# Enterprise CRM Production Deployment Script
set -e

echo "🚀 Starting Enterprise CRM Production Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Stop existing containers
print_status "Stopping existing containers..."
docker-compose down || true

# Remove old images
print_status "Removing old images..."
docker-compose down --rmi all || true

# Build and start services
print_status "Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
print_status "Waiting for services to be ready..."
sleep 30

# Health check
print_status "Performing health checks..."

# Check backend
if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
    print_status "✅ Backend is healthy"
else
    print_error "❌ Backend health check failed"
    exit 1
fi

# Check frontend
if curl -f http://localhost:3000 > /dev/null 2>&1; then
    print_status "✅ Frontend is healthy"
else
    print_error "❌ Frontend health check failed"
    exit 1
fi

# Check nginx
if curl -f http://localhost > /dev/null 2>&1; then
    print_status "✅ Nginx is healthy"
else
    print_warning "⚠️  Nginx health check failed (may need SSL configuration)"
fi

print_status "🎉 Deployment completed successfully!"
print_status "📱 Frontend: http://localhost:3000"
print_status "🔧 Backend API: http://localhost:8080"
print_status "📚 Swagger UI: http://localhost:8080/swagger-ui.html"
print_status "🌐 Production URL: http://localhost"

# Show running containers
print_status "Running containers:"
docker-compose ps
