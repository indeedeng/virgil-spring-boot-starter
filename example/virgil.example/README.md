
This project was created via Spring Initializer [link](https://start.spring.io/#!type=gradle-project&language=java&platformVersion=2.4.4.BUILD-SNAPSHOT&packaging=jar&jvmVersion=1.8&groupId=com.indeed&artifactId=virgil.example&name=virgil-example&description=Example%20project%20for%20Spring%20Boot%20%2B%20Virgil&packageName=com.indeed.virgil.example&dependencies=codecentric-spring-boot-admin-client,codecentric-spring-boot-admin-server,web,cloud-stream)

# Getting Started



API:
* POST /message
    * Payload: { id: \<number\>, content: \<string\> }
* POST /generatemessages
    * Payload: { num: \<number\> }



# Building Docker container

```shell
docker build --build-arg JAR_FILE=build/libs/\*.jar -t virgil/virgil-example-docker .
```

# Running environment

```shell
./runDocker.sh
```
