# Stop and remove local instance
CONTAINER_ID=$(docker ps -a | grep virgil-example | cut -d' ' -f1)

docker stop ${CONTAINER_ID}
docker rm ${CONTAINER_ID}
docker rmi $(docker images | grep virgil/virgil-example-docker | cut -d' ' -f13) -f


docker stop virgil-rabbit-management
docker rm virgil-rabbit-management
