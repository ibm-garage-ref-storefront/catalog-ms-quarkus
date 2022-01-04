##### catalog-ms-quarkus

# Microservice Apps Integration with Elasticsearch Database

*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://cloudnativereference.dev/*

## Table of Contents

* [Introduction](#introduction)
    + [APIs](#apis)
* [Pre-requisites](#pre-requisites)
* [Running the application](#running-the-application)
    + [Get the Catalog application](#get-the-catalog-application)
    + [Run the Elasticsearch Docker Container](#run-the-elasticsearch-docker-container)
    + [Run the Jaeger Docker Container](#run-the-jaeger-docker-container)
    + [Run the SonarQube Docker Container](#run-the-sonarqube-docker-container)
    + [Run the Catalog application](#run-the-catalog-application)
    + [Validating the application](#validating-the-application)
    + [Exiting the application](#exiting-the-application)
* [Conclusion](#conclusion)
* [References](#references)

## Introduction

This project will demonstrate how to deploy a Quarkus Application with an Elasticsearch database.  At the same time, it will also demonstrate how to deploy a dependency Microservice (Inventory) and its MySQL datastore. To know more about Inventory microservices, check [this](https://github.com/ibm-garage-ref-storefront/inventory-ms-quarkus) out.

![Application Architecture](static/catalog.png?raw=true)

Here is an overview of the project's features:
- Leverages [`Quarkus`](https://quarkus.io/), the Supersonic Subatomic Java Framework.
- Uses [`Elasticsearch`](https://www.elastic.co/products/elasticsearch) to persist Catalog data to Elasticsearch database.

### APIs

* Get all items in catalog:
    + `http://localhost:8080/micro/items`
* Get item from catalog using id:
    + `http://localhost:8080/micro/items/{item-id}`

## Pre-requisites:

* [Java](https://www.java.com/en/)

* Clone inventory repository:

```bash
git clone https://github.com/ibm-garage-ref-storefront/inventory-ms-quarkus.git
cd inventory-ms-quarkus
```

* Run the MySQL Docker Container

Run the below command to get MySQL running via a Docker container.

```bash
# Start a MySQL Container with a database user, a password, and create a new database
docker run --name inventorymysql \
    -e MYSQL_ROOT_PASSWORD=admin123 \
    -e MYSQL_USER=dbuser \
    -e MYSQL_PASSWORD=password \
    -e MYSQL_DATABASE=inventorydb \
    -p 3306:3306 \
    -d mysql
```

If it is successfully deployed, you will see something like below.

```
$ docker ps
CONTAINER ID   IMAGE     COMMAND                  CREATED       STATUS       PORTS                               NAMES
e87f041c7da7   mysql     "docker-entrypoint.s…"   2 hours ago   Up 2 hours   0.0.0.0:3306->3306/tcp, 33060/tcp   inventorymysql
```
* Populate the MySQL Database

Now let us populate the MySQL with data.

- Firstly ssh into the MySQL container.

```
docker exec -it inventorymysql bash
```

* Now, run the below command for table creation.

```
mysql -udbuser -ppassword
```

* This will take you to something like below.

```
root@e87f041c7da7:/# mysql -udbuser -ppassword
mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 13
Server version: 8.0.23 MySQL Community Server - GPL

Copyright (c) 2000, 2021, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

* Go to `scripts > mysql_data.sql`. Copy the contents from [mysql_data.sql](./scripts/mysql_data.sql) and paste the contents in the console.

* You can exit from the console using `exit`.

```
mysql> exit
Bye
```

* To come out of the container, enter `exit`.

```
root@d88a6e5973de:/# exit
```

* Run the Inventory application as follows.

```
# Package the application
./mvnw package -Pnative -Dquarkus.native.container-build=true

# Build inventory docker image
docker build -f src/main/docker/Dockerfile.native -t inventory-ms-quarkus-native .

# Run the inventory
docker run -it -d --rm -e quarkus.datasource.jdbc.url=jdbc:mysql://host.docker.internal:3306/inventorydb?useSSL=true -e quarkus.datasource.username=dbuser -e quarkus.datasource.password=password -p 8082:8080 inventory-ms-quarkus-native
```

* You can also verify it as follows.

```
$ docker ps
CONTAINER ID   IMAGE                                                 COMMAND                  CREATED       STATUS       PORTS                                            NAMES
8789d85ba003   inventory-ms-quarkus-native                           "./application -Dqua…"   4 hours ago   Up 4 hours   0.0.0.0:8081->8080/tcp                           objective_dirac
0457127aaf9a   mysql                                                 "docker-entrypoint.s…"   6 days ago    Up 6 days    0.0.0.0:3306->3306/tcp, 33060/tcp                inventorymysql
```

## Running the application

### Get the Catalog application

- Clone Catalog repository:

```bash
git clone https://github.com/ibm-garage-ref-storefront/catalog-ms-quarkus.git
cd catalog-ms-quarkus
```

### Run the Elasticsearch Docker Container

Run the below command to get Elasticsearch running via a Docker container.

```bash
# Start an Elasticsearch Container
docker run --name catalogelasticsearch \
      -e "discovery.type=single-node" \
      -p 9200:9200 \
      -p 9300:9300 \
      -d docker.elastic.co/elasticsearch/elasticsearch:7.16.2
```

If it is successfully deployed, you will see something like below.

```
$ docker ps
CONTAINER ID        IMAGE                                                 COMMAND                  CREATED             STATUS              PORTS                                                                                              NAMES
cea3360f24d1   docker.elastic.co/elasticsearch/elasticsearch:6.3.2   "/usr/local/bin/dock…"   5 hours ago   Up 5 hours   0.0.0.0:9200->9200/tcp, 0.0.0.0:9300->9300/tcp   catalogelasticsearch
```

### Run the Jaeger Docker Container

Set up Jaegar for opentracing. This enables distributed tracing in your application.

```
docker run -d -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one:latest
```

If it is successfully run, you will see something like this.

```
$ docker run -d -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one:latest
1c127fd5dfd1f4adaf892f041e4db19568ebfcc0b1961bec52a567f963014411
```

### Run the SonarQube Docker Container

Set up SonarQube for code quality analysis. This will allow you to detect bugs in the code automatically and alerts the developer to fix them.

```
docker run -d --name sonarqube -p 9000:9000 sonarqube
```

If it is successfully run, you will see something like this.

```
$ docker run -d --name sonarqube -p 9000:9000 sonarqube
1b4ca4e26ceaeacdfd1f4adaf892f041e4db19568ebfcc0b1961b4ca4e26ceae
```

### Run the Catalog application

#### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev -Dquarkus.elasticsearch.hosts=http://localhost:9200 -Dibm.cn.application.client.InventoryServiceClient/mp-rest/url=http://localhost:8082/micro/inventory -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=6831 -DJAEGER_SERVICE_NAME=catalog-ms-quarkus -DJAEGER_SAMPLER_TYPE=const -DJAEGER_SAMPLER_PARAM=1
```

If it is successful, you will see something like this.

```
13412: {"description":"Unveiled in 1961, the revolutionary Selectric typewriter eliminated the need for conventional type bars and movable carriages by using an innovative typing element on a head-and-rocker assembly, which, in turn, was mounted on a small carrier to move from left to right while typing.","id":13412,"img":"selectric.jpg","imgAlt":"Selectric Typewriter","name":"Selectric Typewriter","price":2199,"stock":1000}
resp_string:
{"took":35,"errors":false,"items":[{"index":{"_index":"micro","_type":"items","_id":"13401","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":30,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13402","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":60,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13403","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":61,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13404","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":75,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13405","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":76,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13406","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":15,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13407","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":31,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13408","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":77,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13409","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":78,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13410","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":62,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13411","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":79,"_primary_term":1,"status":200}},{"index":{"_index":"micro","_type":"items","_id":"13412","_version":16,"result":"updated","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":63,"_primary_term":1,"status":200}}]}
rows loaded
```

#### Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `catalog-ms-quarkus-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using the below command.

```
java -jar -Dquarkus.elasticsearch.hosts=http://localhost:9200 -Dibm.cn.application.client.InventoryServiceClient/mp-rest/url=http://localhost:8082/micro/inventory -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=6831 -DJAEGER_SERVICE_NAME=catalog-ms-quarkus -DJAEGER_SAMPLER_TYPE=const -DJAEGER_SAMPLER_PARAM=1 -jar target/catalog-ms-quarkus-1.0.0-SNAPSHOT-runner.jar
```

If it is run successfully, you will see something like below.

```
$ java -jar -Dquarkus.elasticsearch.hosts=http://localhost:9200 -Dibm.cn.application.client.InventoryServiceClient/mp-rest/url=http://localhost:8082/micro/inventory -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=6831 -DJAEGER_SERVICE_NAME=catalog-ms-quarkus -DJAEGER_SAMPLER_TYPE=const -DJAEGER_SAMPLER_PARAM=1 -jar target/catalog-ms-quarkus-1.0.0-SNAPSHOT-runner.jar
Running main method
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2021-02-11 16:01:17,953 INFO  [io.quarkus] (main) catalog-ms-quarkus 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.11.1.Final) started in 5.869s. Listening on: http://0.0.0.0:8080
2021-02-11 16:01:17,956 INFO  [io.quarkus] (main) Profile prod activated.
2021-02-11 16:01:17,956 INFO  [io.quarkus] (main) Installed features: [cdi, elasticsearch-rest-client, rest-client, rest-client-jackson, resteasy, resteasy-jackson, resteasy-jsonb]
Querying Inventory Service for all items ...
2021-02-11 16:01:18,336 INFO  [ibm.cn.app.cli.BaseInventoryException] (Thread-4) Inventory Response Status = 200
Adding/updating item:
13401: {"description":"Punched-card tabulating machines and time clocks were not the only products offered by the young IBM. Seen here in 1930, manufacturing employees of IBM's Dayton Scale Company are assembling Dayton Safety Electric Meat Choppers. These devices, which won the Gold Medal at the 1926 Sesquicentennial International Exposition in Philadelphia, were produced in both counter base and pedestal styles (5000 and 6000 series, respectively). They included one-quarter horsepower models, one-third horsepower machines (Styles 5113, 6113F and 6213F), one-half horsepower types (Styles 5117, 6117F and 6217F) and one horsepower choppers (Styles 5128, 6128F and 6228F). Prices in 1926 varied from admin80 to bluemix-sandbox-dal-9-portal.5.dblayer.com75. Three years after this photograph was taken, the Dayton Scale Company became an IBM division, and was sold to the Hobart Manufacturing Company in 1934.","id":13401,"img":"meat-chopper.jpg","imgAlt":"Dayton Meat Chopper","name":"Dayton Meat Chopper","price":4599,"stock":1000}
```

#### Creating a native executable

Note: In order to run the native executable, you need to install GraalVM. For instructions on how to install it, refer [this](https://quarkus.io/guides/building-native-image).

You can create a native executable using:
```shell script
./mvnw package -Pnative
```

You can then execute your native executable with the below command:

```
./target/catalog-ms-quarkus-1.0.0-SNAPSHOT-runner -Dquarkus.elasticsearch.hosts=http://localhost:9200 -Dibm.cn.application.client.InventoryServiceClient/mp-rest/url=http://localhost:8082/micro/inventory -DJAEGER_AGENT_HOST=localhost -DJAEGER_AGENT_PORT=6831 -DJAEGER_SERVICE_NAME=catalog-ms-quarkus -DJAEGER_SAMPLER_TYPE=const -DJAEGER_SAMPLER_PARAM=1
```

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

#### Running the application using docker

- Build the JVM docker image and run the application.

Package the application.
```shell script
./mvnw package -Dquarkus.native.container-build=true
```

Build the docker image using `Dockerfile.jvm`.
```shell script
docker build -f src/main/docker/Dockerfile.jvm -t catalog-ms-quarkus .
```

Run the application.
```shell script
docker run -it -d --rm -e quarkus.elasticsearch.hosts=http://host.docker.internal:9200 -e ibm.cn.application.client.InventoryServiceClient/mp-rest/url=http://host.docker.internal:8082/micro/inventory -e JAEGER_AGENT_HOST=host.docker.internal -e JAEGER_AGENT_PORT=6831 -e JAEGER_SERVICE_NAME=catalog-ms-quarkus -e JAEGER_SAMPLER_TYPE=const -e JAEGER_SAMPLER_PARAM=1 -p 8083:8080 catalog-ms-quarkus
```

- Build the native docker image and run the application.

For native docker image, package the application using native profile.
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

Build the docker image using `Dockerfile.native`.
```shell script
docker build -f src/main/docker/Dockerfile.native -t catalog-ms-quarkus-native .
```

Run the application.
```shell script
docker run -it -d --rm -e quarkus.elasticsearch.hosts=http://host.docker.internal:9200 -e ibm.cn.application.client.InventoryServiceClient/mp-rest/url=http://host.docker.internal:8082/micro/inventory -e JAEGER_AGENT_HOST=host.docker.internal -e JAEGER_AGENT_PORT=6831 -e JAEGER_SERVICE_NAME=catalog-ms-quarkus -e JAEGER_SAMPLER_TYPE=const -e JAEGER_SAMPLER_PARAM=1 -p 8083:8080 catalog-ms-quarkus-native
```

### Validating the application

Now, you can validate the application as follows.

Note: If you are running using docker, use `8083` instead of `8080` as port.

- Try to hit http://localhost:8080/micro/items/ and you should be able to see a list of items.

- You can also do it using the below command.

```
curl http://localhost:8080/micro/items/
```

![Catalog api](static/catalog_api_result.png?raw=true)

- You can access the swagger api at http://localhost:8080/q/swagger-ui/

![Catalog swagger api](static/catalog_swagger_api.png?raw=true)

Note: If you are running using docker, use `8083` instead of `8080` as port.

- To access Jaeger UI, use http://localhost:16686/ and point the service to `catalog-ms-quarkus` to access the traces.

![Catalog Jaeger traces](static/catalog_jaeger_traces.png?raw=true)

![Catalog Jaeger trace details](static/catalog_jaeger_trace_details.png?raw=true)

- To perform code quality checks, run the below commands.

Do a clean install to generate necessary artifacts.

```
./mvnw clean install
```

If it is successful, you will see something like this.

```
[INFO] --- maven-install-plugin:2.4:install (default-install) @ catalog-ms-quarkus ---
[INFO] Installing /Users/Hemankita1/IBM/CN_Ref/Quarkus/catalog-ms-quarkus/target/catalog-ms-quarkus-1.0.0-SNAPSHOT.jar to /Users/Hemankita1/.m2/repository/ibm/cn/catalog-ms-quarkus/1.0.0-SNAPSHOT/catalog-ms-quarkus-1.0.0-SNAPSHOT.jar
[INFO] Installing /Users/Hemankita1/IBM/CN_Ref/Quarkus/catalog-ms-quarkus/pom.xml to /Users/Hemankita1/.m2/repository/ibm/cn/catalog-ms-quarkus/1.0.0-SNAPSHOT/catalog-ms-quarkus-1.0.0-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  30.069 s
[INFO] Finished at: 2021-03-22T17:09:23+05:30
[INFO] ------------------------------------------------------------------------
```

Now run sonar as follows.

```
./mvnw sonar:sonar -Dsonar.host.url=http://<sonarqube_host>:<sonarqube_port> -Dsonar.login=<sonarqube_access_token>
```

To get the sonarqube access token, login to the sonarqube ui. Then go to `User` > `My Account`. Now, select `Security` and then generate a token.

If it is successful, you will see something like this.

```
$ ./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=64207e7dc1c28e995bb8bc28b25bdf1bbadf970f
[INFO] Scanning for projects...
[INFO]
[INFO] ---------------------< ibm.cn:catalog-ms-quarkus >----------------------
[INFO] Building catalog-ms-quarkus 1.0.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- sonar-maven-plugin:3.7.0.1746:sonar (default-cli) @ catalog-ms-quarkus ---
[INFO] User cache: /Users/Hemankita1/.sonar/cache
[INFO] SonarQube version: 8.7.1
..........
..........
[INFO] ANALYSIS SUCCESSFUL, you can browse http://localhost:9000/dashboard?id=ibm.cn%3Acatalog-ms-quarkus
[INFO] Note that you will be able to access the updated dashboard once the server has processed the submitted analysis report
[INFO] More about the report processing at http://localhost:9000/api/ce/task?id=AXhZvNKq4vdRH1rH8FPm
[INFO] Analysis total time: 12.963 s
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  16.661 s
[INFO] Finished at: 2021-03-22T17:10:50+05:30
[INFO] ------------------------------------------------------------------------
```

- Now, access http://localhost:9000/, login using the credentials admin/admin, and then you will see something like below.

![Catalog SonarQube](static/catalog_sonarqube.png?raw=true)

![Catalog SonarQube details](static/catalog_sonarqube_details.png?raw=true)

### Exiting the application

To exit the application, just press `Ctrl+C`.

If using docker, use `docker stop <container_id>`

## Conclusion

You have successfully developed and deployed the Catalog Microservice and an Elasticsearch database locally using Quarkus framework.

## References

- [Quarkus starter template](https://quarkus.io/guides/getting-started)
- [Quarkus Configuration](https://quarkus.io/guides/config)
- [Building native image using Quarkus](https://quarkus.io/guides/building-native-image)
- [Enabling Opentracing for Quarkus example](https://quarkus.io/guides/opentracing)
- [Enabling Openapi for Quarkus example](https://quarkus.io/guides/openapi-swaggerui)
- [Measuring the coverage of tests](https://quarkus.io/guides/tests-with-coverage)
- [Building native image using a multi stage docker build](https://quarkus.io/guides/building-native-image#using-a-multi-stage-docker-build)
