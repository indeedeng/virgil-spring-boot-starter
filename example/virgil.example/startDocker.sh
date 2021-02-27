gradle bootJar
docker build --build-arg JAR_FILE=build/libs/\*.jar -t virgil/virgil-example-docker .

docker network create virgil_net
docker run -d --network virgil_net --hostname virgil-rabbit --name virgil-rabbit-management -p 15672:15672 -p 5672:5672 -p 5671:5671 rabbitmq:3-management
docker run -d --network virgil_net --name virgil-example -p 8080:8080 -p 5005:5005 virgil/virgil-example-docker
