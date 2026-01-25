# todo: add project description


## Tech stack
- **Backend**
    - Spring Boot, Spring Web, Spring Cache, Spring Data
    - MongoDB
    - Maven
    - Libraries (Lombok, Modelmapper, Mockito, Junit 5 and Swagger Open Api)
- **DevOps**
    - Docker, AWS (CodePipeLine, ECR and ECS/EC2)
    
## Pre-requisites
 
 - Java 17
 - Maven  ([https://maven.apache.org/install.html](https://maven.apache.org/install.html))
 - MongoDB (will be run as docker container, no need to install locally)
 - Docker ([https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/))

## How to build

```shell
$ mvn clean package
```

## How to test

```shell
$ mvn clean test
```

## How to package application as docker image

```shell
$ docker build -t issueTracker:1.0 .
```

## How to run application

```shell
$ docker-compose up
```

You can then access to the crud endpoints at http://localhost:8080.
todo: add postman collections for testing endpoints

API Documentation can be accessed at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
Also see API Use Cases Section below for examples.

## How to stop application

- Mac: `ctrl + c`
- Windows: `ctrl + c`
- or from another terminal tab:

```shell
$ docker-compose down 
```

## How to view application logs
```shell
$ docker logs issueTracker
```

## How to connect to MongoDB Container
```shell
$ docker exec -it mongodb bash
```

## Design/Implementation notes:
1. ### The following are utilized in an effort to improve performance and scalability:
- Add pagination support for GET /fetchStudents which could return huge Json response.
- MongoDb is used so that DB can be easily scaled horizontally to support huge dataset. Document-based DB has the advantage of improved query performance too, because the related document has already been stored with the main document, therefore reducing overhead of table joins.
- Use of MongoTemplate to implement search functionality based on the different params the client passes in. This greatly improves data fetching performance over using java code that bring all data then filter.
- Spring cache is used to cache class and semester data.
- Concurrent Hashset implemented with ConcurrentHashMap are used for collections of classes for an enrollment and collections of enrollments for a student. AtomicInteger is used for total credits for a student. All of these concurrent data structure are used to support multi-threading and support any future concurrent enhancement needs.
- todo: add more details about concurrency and caching
- 
2.
3. Hibernate implementation of javax validations are utilized to validate the json input data.

4. ControllerAdvice is utilized to centralize error handling and construct error responses to the clients.

5. Dto objects are utilized to provide a clean separation of entity DB objects in favor of clean abstraction of persistence layer. ModelMapper library is utilized to provide data transformation of dto and entity objects.

6. Lombok library is utilized for compiling-time code auto generation (getters/setters/contrctors, etc...).

7. Comprehensive test cases are developed to provide unit testing and integration testing to ensure code quality and facilitate future enhancement/maintenance.

8. Spring properties/configuration/injection are utilized to ease future enhancement/maintenance efforts.

9. Open API (Swagger) is utilized to generate API documentation accessible through browser.

10. Future improvements:
    a. k8s to replace docker compose.
    b. Redis cache to replace spring cache.
    d. Spring webflux to handle possible very large # of concurrent requests in the future.

## todo: API Use Cases


## CI/CD Using AWS CodePipeline

If any code pushes to GitHub repository, the Git webhook initiates the AWS CodePipeline automatically, as defined in: 

***buildSpec.yml*** 

- Execute the maven test and build
- Create a docker image and push it to ECR
- Create an imagedefinitions.json file for the deployment

The code pipeline for this application consists of three stages:
- Code Source - Pull sourcecode from Github
- Code Build - Maven build and create a docker Image
- Code Deploy - Deploy into ECS/EC2 instance

## Requirements Description:
### Restraints
- todo: add restraints

### Required APIs:
- todo: add required APIs

### Non Functional Considerations
Performance and scalability aspects of your code will also be evaluated. Make sure the data structures that you use are chosen for scale and efficiency. For example, think about which APIs might be called more often than others with what parameters, and make sure those can handle the load efficiently.
todo: add more Non Functional on scalability, performance, reliability etc...
