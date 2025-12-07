#!/bin/bash
# startup-jenkins.sh

echo "Starting Jenkins with Docker..."
docker-compose up -d

echo "Waiting for Jenkins to start..."
sleep 30

echo "Jenkins is running at http://localhost:8080"
echo "Admin username: admin"
echo "Admin password: admin123"

# Инициализация Jenkins
echo "Setting up Jenkins..."

# Создание pipeline job через CLI (опционально)
docker exec jenkins bash -c 'java -jar /var/jenkins_home/war/WEB-INF/jenkins-cli.jar -s http://localhost:8080/ create-job myapp-pipeline < /var/jenkins_home/jobs/pipeline-config.xml'
