#!/bin/bash

# Restart database for Borges
docker stop rabbitmq
docker stop postgres
docker rm rabbitmq
docker rm postgres
docker run -d --name postgres -e POSTGRES_PASSWORD=testing -p 5432:5432 -e POSTGRES_USER=testing postgres:9.6-alpine
docker run -d --hostname rabbitmq --name rabbitmq -p 15672:15672 -p 5672:5672 rabbitmq:3-management

echo "Restarted!"
