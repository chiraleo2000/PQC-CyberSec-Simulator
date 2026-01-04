FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for caching
COPY requirements.txt .

# Install Python dependencies (CPU versions for Docker)
RUN pip install --no-cache-dir numpy scipy sympy flask flask-cors requests

# Copy application code
COPY . .

# Expose port
EXPOSE 8184

# Run the service
CMD ["python", "quantum_service.py"]
