gradle bootJar
docker build --build-arg JAR_FILE=build/libs/\*.jar -t virgil/virgil-example-docker .
docker run -d --name virgil-example -p 8080:8080 -p 5005:5005 virgil/virgil-example-docker
