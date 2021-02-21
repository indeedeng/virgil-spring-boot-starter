docker run -d --name virgil-example -p 8080:8080 virgil/virgil-example-docker
docker run -d --hostname virgil-rabbit --name virgil-rabbit rabbitmq:3
docker run -d --hostname virgil-rabbit --name virgil-rabbit-management -p 15672:15672 rabbitmq:3-management
