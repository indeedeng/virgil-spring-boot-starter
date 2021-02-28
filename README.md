# Virgil Spring Boot Starter

![OSS Lifecycle](https://img.shields.io/osslifecycle/indeedeng/virgil-spring-boot-starter.svg)

Virgil is a Spring Boot Starter that is developed as a Spring Boot Admin extension and serves as a generic message
manager. It supports RabbitMQ with actions such as count/display/republish/drop messages on the queue.

For future releases we will consider adding in additional actions such as download/etc along with Kafka support.

Virgil implements a collection of Actuator endpoints to support the queue actions, it also provides UI as Admin
extension to interact with those Actuator endpoints.

Name reference: https://en.wikipedia.org/wiki/Virgil

## Getting Started

```
compile 'com.indeed:virgil-spring-boot-starter'
```

### Build Virgil UI Component

#### Dev Build
```
gradle buildFrontEndDev
```

#### Prod Build
```
gradle buildFrontEndProd
```

### Setup Virgil In Your Project

#### List of settings you can configure in `application.yml`
* virgil
    * queues
        * \<queue\>
            * readName
            * readBinderName
            * [Optional] republishName
            * [Optional] republishBinderName
            * [Optional] republishBindingRoutingKey
    * binders
        * \<binderName\>
            * name
            * type: rabbit
            * rabbitProperties
                * [Optional] addresses
                * [Optional] host
                * [Optional] port
                * username
                * password
                * [Optional] virtual-host

Example with Single DLQ:
```yaml
virgil:
  queues:
    primary:
  binders:
    uniqueBinderKey:

```

Example with Many DLQs:
```yaml
virgil:
  queues:
    primary:
    secondary:
  binders:
    uniqueBinderKey:
    secondaryBinderKey:
```


#### Configuration Details
* `binders.<binderName>.rabbitSetings`: if `addresses` is provided it will be used as priority over `host` and `port`.
If `addresses` is blank, `host` and `port` will be used. `Port` will default to `5672`.

* `queues.queue`: if `republishName` and `republishBinderName` is not present, we will disable `republish` option
per message


* If you are using Spring Cloud Stream:
  * If you configure `spring.cloud.stream.rabbit.bindings.input.consumer.auto-bind-dlq=true` and
  `spring.cloud.stream.bindings.input.group=myGroup`, the format of your `DLQ`
  should be `input.myGroup.dlq`;

## Supported Functionality
#### V1
* One queue per application instance;
* RabbitMQ;
* Display total count of messages in queue;
* Parse out text based body to be displayed
    * Pluggable MessageConverter allows you to replace default utf8 parser with custom message parser
        * By registering a Bean with IMessageConverter as its return type
* Republish 1 message at a time from queue;
* Drop 1 or all messages at a time from queue;
* UI as Spring Boot Admin extension backed by Actuator endpoints;

#### V2
Stay tuned!

## UI Demo
![alt text](https://github.com/indeedeng/virgil-spring-boot-starter/blob/master/images/home.png "Spring Boot Admin UI")
![alt text](https://github.com/indeedeng/virgil-spring-boot-starter/blob/master/images/virgil.png "Virgil UI")

## Visit actuator endpoints
* http://localhost:8080/private/actuator-endpoint-id
* e.g.:
  * http://localhost:8080/private/virgil/get-queue-size
  * http://localhost:8080/private/virgil/publish-message
  * http://localhost:8080/private/virgil/drop-all-messages
  * http://localhost:8080/private/virgil/drop-message
  * http://localhost:8080/private/virgil/get-dlq-messages

## How To Contribute

If you’d like to contribute, please open an issue describing what you want to change and why, or comment on an existing issue. We’d love to have you.

## Project Maintainers

* [Richard Cen](https://github.com/RichardCen), Indeed Software Engineers
* [reedyrm](https://github.com/reedyrm), Indeed Software Engineers


## Code of Conduct
This project is governed by the [Contributor Covenant v 1.4.1](CODE_OF_CONDUCT.md).

## License
This project uses the [Apache 2.0](LICENSE) license.
