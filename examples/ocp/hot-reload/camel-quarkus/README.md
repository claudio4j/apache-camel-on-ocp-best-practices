1. Setup minikube

Install minikube with the registry addon

Run
```
eval $(minikube docker-env)
```

2. Setup the postgresql service

```
kubectl apply -f postgres-ephemeral.yaml
```

- open minikube tunnel to access postgresql

```
minikube tunnel
```

Open the psql prompt
```
psql --host=`minikube ip` --port=`kubectl get svc/postgresql -ojsonpath='{.spec.ports[0].nodePort}'` --username=postgres_user --dbname=testdb
```

Run this psql code
```
CREATE USER user1 WITH ENCRYPTED PASSWORD '1admin123';
CREATE USER user2 WITH ENCRYPTED PASSWORD '2admin123';
GRANT ALL PRIVILEGES ON DATABASE testdb TO user1;
GRANT ALL PRIVILEGES ON DATABASE testdb TO user2;
GRANT ALL ON ALL TABLES IN SCHEMA public TO user1;
GRANT ALL ON ALL TABLES IN SCHEMA public TO user2;
```

logout and login as `user1`
```
psql --host=`minikube ip` --port=`kubectl get svc/postgresql -ojsonpath='{.spec.ports[0].nodePort}'` --username=user1 --dbname=testdb
```

run this psql code
```
create table foo(username varchar,city varchar);
insert into foo(username,city) values('foo', 'bar');
insert into foo(username,city) values('test1', 'bar2');
insert into foo(username,city) values('java', 'bang');
insert into foo(username,city) values('jiu', 'jitsu');
```

3. Add the kubernetes secret

3.1 If ou are deploying the route to kubernetes
```
kubectl apply -f secrets1.yaml
```

3.2 If ou are running the route locally

Get the minikube port for the postgresql service and update the port in the secret*-standalone.yaml

```
kubectl get svc/postgresql -oyaml|grep nodePort
```

Add the secret.
```
kubectl apply -f secrets1-standalone.yaml
```

4. Build and run the camel route

The camel route can be run locally or build a container image, generate the kubernetes manifest and deploy to a kubernetes cluster.

4.1 Build and run the route locally

* Update the application.properties for your minikube environment, look at the section `# quarkus kubernetes client`

```
mvn clean quarkus:dev
```

Once the camel route is running, update the kubernetes secret
```
kubectl apply -f secrets2-standalone.yaml
```

4.2 Build and deploy the code to minikube

```
eval $(minikube docker-env)
mvn clean package -Pminikube -Dquarkus.kubernetes.deploy=true
```

Once the camel route is running, update the kubernetes secret
```
kubectl apply -f secrets2.yaml
```

5. To verify what is the user connected to the postgresql

5.1 from the camel route log

The console should display the username used by the datasource to connect to the postgresql.
```
[sam.JdbcLog] (Camel thread #9 - timer://JAVA) >> datasource user: user1
```

Once the camel route is running, update the kubernetes secret as outlined in the previous section.
```
kubectl apply -f <secret2>.yaml
```

The console log should display the `SecretsReloadTriggerTask` detecting the change in the secret custom resource and reloading it, then triggers the camel context reload.
```
[org.apa.cam.com.kub.sec.vau.SecretsReloadTriggerTask] Matching secret id: secret-basic-auth=secret-basic-auth -> true
[org.apa.cam.com.kub.sec.vau.SecretsReloadTriggerTask] Update for Kubernetes Secret: secret-basic-auth detected, triggering CamelContext reload
[org.apa.cam.sup.DefaultContextReloadStrategy] Reloading CamelContext (camel-2) triggered by: org.apache.camel.component.kubernetes.secrets.vault.SecretsReloadTriggerTask$1@7e53b383
```

However the next route run, shows the previous username
```
[sam.JdbcLog] (Camel thread #9 - timer://JAVA) >> datasource user: user1
```

5.2 From postgresql

Connect to psql as postgresql admin user, password `admin123`
```
psql --host=`minikube ip` --port=`kubectl get svc/postgresql -ojsonpath='{.spec.ports[0].nodePort}'` --username=postgres_user --dbname=testdb
```

Run this sql code to show the connected user:
```
select usename,application_name,client_addr from pg_stat_activity WHERE datname = 'testdb';
```
