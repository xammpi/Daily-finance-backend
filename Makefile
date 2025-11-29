.PHONY: help install run test build clean docker-build docker-up docker-down

help:
	@echo "Available commands:"
	@echo "  make install      - Install dependencies"
	@echo "  make run          - Run the application in local profile"
	@echo "  make test         - Run tests"
	@echo "  make build        - Build the application"
	@echo "  make clean        - Clean build artifacts"
	@echo "  make docker-build - Build Docker image"
	@echo "  make docker-up    - Start application with Docker Compose"
	@echo "  make docker-down  - Stop Docker Compose services"

install:
	./mvnw clean install

run:
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local

test:
	./mvnw test

build:
	./mvnw clean package

clean:
	./mvnw clean

docker-build:
	docker build -t expense-tracker-api:latest .

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down
