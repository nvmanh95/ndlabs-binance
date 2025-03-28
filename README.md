# Scalable Microservice with Sharding for processing Binance Trade Data

This project is a scalable microservice built with Kotlin and Spring Boot, designed to process real-time trade data from
Binance via WebSocket. It uses dynamic sharding to distribute trading pairs (e.g., BTC_USDT, ETH_USDT) across multiple
instances (pods) in a Kubernetes cluster, ensuring efficient load balancing and fault tolerance. Redis is used for
coordinating shard assignments and maintaining consistency across the system.

**Note:** When the system reassigns shards, it briefly unsubscribes from the WebSocket feeds for the affected trading pairs after
30 seconds, a safeguard to prevent data gaps or overlaps during the transition. Finally, when sending processed data to
a Kafka producer, the microservice guarantees idempotence

# Technologies

**Kotlin**: Programming language for the microservice.
**Spring** Boot: Framework for building the microservice.
**Redis**: In-memory data store for shard coordination.
**Kubernetes**: Container orchestration for deployment and scaling.
**Docker**: Containerization of the application.
**Gradle**: Build tool for the project.

# Local Setup

Steps to Run Locally

### Clone the repository:

```shell
    git clone https://github.com/yourusername/your-repo-name.git
```

### Build the project:

```shell
  ./gradlew clean build
```

### Run the whole project with docker compose

```shell
    docker compose up
```

This will also start redis, kafka and the app itself as well.

# Run with kubernetes

### Build the jar

```shell
    ./gradlew clean build
```

### Build docker image

```shell
    minikube start
    eval $(minikube docker-env)
    docker build -t nblabs-binance:latest .
```

### Deploy to kubernetes

```shell
    kubectl apply -f deploy/deployment.yaml
```

To see the status of the pods in kubernetes:

```shell
    kubectl get pod
```

After all the pods up and running, open kafka ui to see the processed data

```shell
  minikube service kafka-ui-service
```

## Some screenshots added to the `result` folder for the review
