# amq-filter-demo

Demo Quarkus para validar filtro de fila no AMQ Broker.

- Producer publica no topico `topic-pix-transaction-changed`.
- Consumer consome diretamente da fila `queue-credito-transactionchanged`.
- O broker usa um divert filtrado com `additionalID IS NOT NULL`.
- O topico fica `MULTICAST`, e a fila fica `ANYCAST`.

O endpoint positivo cria uma `TextMessage` e define a property JMS:

```java
message.setStringProperty("additionalID", "123");
```

O filtro nao interpreta campos dentro do JSON do payload.

## Fluxo de teste

Enviar mensagem que deve passar pelo filtro:

```shell script
curl http://localhost:8080/send/with-property
curl http://localhost:8080/consume
```

O consumo deve retornar:

```text
Mensagem COM additionalID
```

Enviar mensagem que nao deve passar pelo filtro:

```shell script
curl http://localhost:8080/send/without-property
curl 'http://localhost:8080/consume?timeout=1000'
```

O consumo deve retornar:

```text
Nenhuma mensagem recebida
```

## Fluxo de teste max-delivery-attempts=-1

Esse teste usa a fila independente `queue-max-delivery-attempts-infinite`, configurada no broker com:

```yaml
- addressSettings.queue-max-delivery-attempts-infinite.maxDeliveryAttempts=-1
```

Enviar mensagem:

```shell script
curl http://localhost:8080/max-delivery/send
```

Forcar rollback algumas vezes:

```shell script
curl http://localhost:8080/max-delivery/fail
curl http://localhost:8080/max-delivery/fail
curl http://localhost:8080/max-delivery/fail
```

O consumo deve retornar a mesma mensagem com `JMSXDeliveryCount` aumentando:

```text
Rollback aplicado. JMSXDeliveryCount=1. Body=Mensagem para testar max-delivery-attempts=-1
Rollback aplicado. JMSXDeliveryCount=2. Body=Mensagem para testar max-delivery-attempts=-1
Rollback aplicado. JMSXDeliveryCount=3. Body=Mensagem para testar max-delivery-attempts=-1
```

Consumir com commit para remover a mensagem da fila:

```shell script
curl http://localhost:8080/max-delivery/consume
```

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/amq-filter-demo-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
