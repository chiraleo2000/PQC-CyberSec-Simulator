# =============================================================================
# PQC-CyberSec-Simulator Quantum Service Dockerfile
# =============================================================================
# 
# This Dockerfile supports two modes:
# 1. GPU Mode (NVIDIA) - Use NVIDIA Container Toolkit for full GPU acceleration
# 2. Fallback Mode - CPU-only simulation when GPU is unavailable
#
# For GPU mode, run with:
#   docker run --gpus all -p 8184:8184 pqc-quantum-simulator
#
# =============================================================================

# Use NVIDIA CUDA base image for GPU support (falls back gracefully on CPU)
FROM nvidia/cuda:12.3.1-runtime-ubuntu22.04 AS gpu-base

# Install Python and dependencies
RUN apt-get update && apt-get install -y \
    python3.11 \
    python3-pip \
    python3.11-dev \
    build-essential \
    && rm -rf /var/lib/apt/lists/* \
    && ln -s /usr/bin/python3.11 /usr/bin/python

WORKDIR /app

# Copy requirements first for caching
COPY requirements.txt .

# Install Python dependencies
# CuPy with CUDA support for GPU acceleration
RUN pip install --no-cache-dir \
    numpy \
    scipy \
    sympy \
    flask \
    flask-cors \
    requests \
    && pip install --no-cache-dir cupy-cuda12x || echo "CuPy GPU install failed, will use CPU fallback"

# Copy application code
COPY . .

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8184

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8184/api/quantum/health')" || exit 1

# Environment variables
ENV PYTHONUNBUFFERED=1
ENV GPU_REQUIRED=false
ENV FLASK_ENV=production

# Run the service
CMD ["python", "quantum_service.py"]

# =============================================================================
# CPU-Only Alternative (if GPU base image fails)
# =============================================================================
# FROM python:3.11-slim AS cpu-fallback
# 
# WORKDIR /app
# 
# RUN apt-get update && apt-get install -y build-essential && rm -rf /var/lib/apt/lists/*
# 
# COPY requirements.txt .
# RUN pip install --no-cache-dir numpy scipy sympy flask flask-cors requests
# 
# COPY . .
# RUN mkdir -p /app/logs
# 
# EXPOSE 8184
# ENV PYTHONUNBUFFERED=1 GPU_REQUIRED=false
# 
# CMD ["python", "quantum_service.py"]
# =============================================================================
