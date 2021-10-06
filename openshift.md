## Deploying the app on Openshift

- Login into the cluster using `oc login`.

- Create a new project.

```
oc new-project sf-quarkus
```

- As a pre-requisite, `inventory-ms-quarkus` application should be deployed. For steps on how to deploy the same, refer this [doc](https://github.com/ibm-garage-ref-storefront/inventory-ms-quarkus/blob/master/openshift.md).

- Clone the `catalog-ms-quarkus` repo.

```bash
git clone https://github.com/ibm-garage-ref-storefront/catalog-ms-quarkus.git
cd catalog-ms-quarkus
```

- Setup the database.

```bash
cd c
./setup_database.sh sf-quarkus
cd ..
```

- Include the OpenShift extension like this:

```
./mvnw quarkus:add-extension -Dextensions="openshift"
```

This will add the below dependency to your pom.xml

```
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-openshift</artifactId>
</dependency>
```

- Now, navigate to `src/main/resources/application.properties` and add the below.

```
quarkus.openshift.env.vars.elasticsearch-host=elasticsearch
quarkus.openshift.env.vars.elasticsearch-port=9200
quarkus.openshift.env.vars.inventory-host-name=inventory-ms-quarkus
quarkus.openshift.env.vars.inventory-port=8080
```

- To trigger a build and deployment in a single step, run the below command.

```
./mvnw clean package -Dquarkus.kubernetes.deploy=true
```

If it is run successfully, you will see something like this.

```
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Push successful
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Deploying to openshift server: https://c103-e.jp-tok.containers.cloud.ibm.com:31780/ in namespace: sf-quarkus-openshift.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Service catalog-ms-quarkus.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream catalog-ms-quarkus.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream openjdk-11.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: BuildConfig catalog-ms-quarkus.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: DeploymentConfig catalog-ms-quarkus.
[INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 172106ms
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  03:04 min
[INFO] Finished at: 2021-03-22T18:21:29+05:30
[INFO] ------------------------------------------------------------------------
```

- Now create the route as follows.

```
oc expose svc catalog-ms-quarkus
```

- Grab the route.

```
oc get route catalog-ms-quarkus --template='{{.spec.host}}'
```

You will see something like below.

```
$ oc get route catalog-ms-quarkus --template='{{.spec.host}}'
catalog-ms-quarkus-sf-quarkus-openshift.storefront-cn-6ccd7f378ae819553d37d5f2ee142bd6-0000.che01.containers.appdomain.cloud
```

- Now access the endpoint using `http://<route_url>/micro/items`.

For instance if using the above route, it will be http://catalog-ms-quarkus-sf-quarkus-openshift.storefront-cn-6ccd7f378ae819553d37d5f2ee142bd6-0000.che01.containers.appdomain.cloud/micro/items.
