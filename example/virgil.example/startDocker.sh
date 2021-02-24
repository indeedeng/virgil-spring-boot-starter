docker run -d --hostname virgil-rabbit --name virgil-rabbit-management -p 15672:15672 -p 5672:5672 rabbitmq:3-management
docker run -d --name virgil-example -p 8080:8080 -p 5005:5005 virgil/virgil-example-docker
