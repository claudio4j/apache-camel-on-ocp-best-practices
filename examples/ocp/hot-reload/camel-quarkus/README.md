1. Setup minikube

Install minikube with the registry addon

Run
```
eval $(minikube docker-env)
```

2. Setup the postgresql service

kubectl apply -f postgres-ephemeral.yaml

- open minikube tunnel to access postgresql

```
minikube tunnel
```

Open the psql prompt
```
psql --host=`minikube ip` --port=`k get svc/postgresql -ojsonpath='{.spec.ports[0].nodePort}'` --username=postgres_user --dbname=testdb
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
psql --host=`minikube ip` --port=`k get svc/postgresql -ojsonpath='{.spec.ports[0].nodePort}'` --username=user1 --dbname=testdb
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

```
kubectl apply -f secrets.yaml
```

4. Build and deploy the code

```
mvn clean package -Pminikube -Dquarkus.kubernetes.deploy=true
```

Once the camel route is running, update the kubernetes secret
```
kubectl apply -f secrets2.yaml
```
5. To verify what is the user connected to the postgresql

Connect to psql as postgresql admin user, password `admin123`
```
psql --host=`minikube ip` --port=`k get svc/postgresql -ojsonpath='{.spec.ports[0].nodePort}'` --username=postgres_user --dbname=testdb
```

Run this sql code to show the connected user:
```
select usename,application_name,client_addr from pg_stat_activity WHERE datname = 'testdb';
```
