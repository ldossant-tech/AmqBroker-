# AMQ Broker - filtro por additionalID

O objetivo foi fazer a mensagem publicada no topico `topic-pix-transaction-changed` cair na fila `queue-credito-transactionchanged` somente quando possuir a property JMS `additionalID`.

## O que foi feito

No AMQ Broker, o topico ficou como `MULTICAST` e a fila como `ANYCAST`. Para ligar os dois e aplicar o filtro no broker, foi configurado um `divert`:

```yaml
- addressConfigurations.topic-pix-transaction-changed.routingTypes=MULTICAST
- addressConfigurations.queue-credito-transactionchanged.routingTypes=ANYCAST
- addressConfigurations.queue-credito-transactionchanged.queueConfigs.queue-credito-transactionchanged.address=queue-credito-transactionchanged
- addressConfigurations.queue-credito-transactionchanged.queueConfigs.queue-credito-transactionchanged.routingType=ANYCAST
- divertConfigurations.pix-transaction-changed-to-credito.address=topic-pix-transaction-changed
- divertConfigurations.pix-transaction-changed-to-credito.forwardingAddress=queue-credito-transactionchanged
- divertConfigurations.pix-transaction-changed-to-credito.routingType=ANYCAST
- divertConfigurations.pix-transaction-changed-to-credito.exclusive=false
- divertConfigurations.pix-transaction-changed-to-credito.filterString=additionalID IS NOT NULL
```

Linha a linha:

```yaml
- addressConfigurations.topic-pix-transaction-changed.routingTypes=MULTICAST
```

Declara o address `topic-pix-transaction-changed` como `MULTICAST`. Esse routing type representa o comportamento de topico/pub-sub, onde uma mensagem pode ser entregue para mais de uma assinatura/fila vinculada ao address.

```yaml
- addressConfigurations.queue-credito-transactionchanged.routingTypes=ANYCAST
```

Declara o address `queue-credito-transactionchanged` como `ANYCAST`. Esse routing type representa o comportamento de fila, onde uma mensagem e entregue para um consumidor da fila.

```yaml
- addressConfigurations.queue-credito-transactionchanged.queueConfigs.queue-credito-transactionchanged.address=queue-credito-transactionchanged
```

Cria a fila `queue-credito-transactionchanged` e associa essa fila ao address `queue-credito-transactionchanged`. O primeiro `queue-credito-transactionchanged` depois de `queueConfigs` e o nome da fila; o valor depois de `address=` e o address onde essa fila fica conectada.

```yaml
- addressConfigurations.queue-credito-transactionchanged.queueConfigs.queue-credito-transactionchanged.routingType=ANYCAST
```

Define que a fila `queue-credito-transactionchanged` usa routing type `ANYCAST`. Isso confirma que o destino final do divert sera tratado como fila, nao como topico.

```yaml
- divertConfigurations.pix-transaction-changed-to-credito.address=topic-pix-transaction-changed
```

Cria/configura o divert chamado `pix-transaction-changed-to-credito` e define o address de origem. O broker vai avaliar mensagens que chegam em `topic-pix-transaction-changed`.

```yaml
- divertConfigurations.pix-transaction-changed-to-credito.forwardingAddress=queue-credito-transactionchanged
```

Define o address de destino do divert. Quando a mensagem passar pelo filtro, o broker encaminha a mensagem para `queue-credito-transactionchanged`.

```yaml
- divertConfigurations.pix-transaction-changed-to-credito.routingType=ANYCAST
```

Define que o encaminhamento feito pelo divert para o destino usa `ANYCAST`. Na pratica, isso faz a mensagem encaminhada cair na fila configurada no address de destino.

```yaml
- divertConfigurations.pix-transaction-changed-to-credito.exclusive=false
```

Define que o divert nao e exclusivo. A mensagem pode continuar seguindo o fluxo normal do address original e, quando passar no filtro, tambem ser copiada/encaminhada para a fila de credito. Se fosse `true`, o divert poderia impedir o roteamento normal da mensagem apos o desvio.

```yaml
- divertConfigurations.pix-transaction-changed-to-credito.filterString=additionalID IS NOT NULL
```

Define o filtro do divert. O broker so encaminha a mensagem para `queue-credito-transactionchanged` quando a mensagem possui a property JMS `additionalID`. Esse filtro e avaliado em properties/header da mensagem, nao no JSON/body.

O filtro `additionalID IS NOT NULL` e avaliado pelo broker em cima de properties/header da mensagem JMS. Por isso, o producer precisa enviar assim:

```java
TextMessage message = context.createTextMessage("Mensagem COM additionalID");
message.setStringProperty("additionalID", "123");
context.createProducer().send(topic, message);
```

### Nao basta mandar `additionalID` apenas dentro do JSON do payload, porque o broker nao interpreta o conteudo interno do JSON para esse filtro.


# max-delivery-attempts=-1

```yaml
- addressConfigurations.queue-max-delivery-attempts-infinite.routingTypes=ANYCAST
- addressConfigurations.queue-max-delivery-attempts-infinite.queueConfigs.queue-max-delivery-attempts-infinite.address=queue-max-delivery-attempts-infinite
- addressConfigurations.queue-max-delivery-attempts-infinite.queueConfigs.queue-max-delivery-attempts-infinite.routingType=ANYCAST
- addressSettings.queue-max-delivery-attempts-infinite.maxDeliveryAttempts=-1
```

Com `maxDeliveryAttempts=-1`, o broker nao move a mensagem para DLQ por limite de tentativas. A mensagem continua sendo redelivered enquanto o consumidor fizer rollback.

Enviar mensagem para a fila de teste:

```shell
curl http://amq-filter-demo-amq.apps.ldossant.vmware.tamlab.rdu2.redhat.com/max-delivery/send
```

Forcar falha/rollback varias vezes:

```shell
curl http://amq-filter-demo-amq.apps.ldossant.vmware.tamlab.rdu2.redhat.com/max-delivery/fail
curl http://amq-filter-demo-amq.apps.ldossant.vmware.tamlab.rdu2.redhat.com/max-delivery/fail
curl http://amq-filter-demo-amq.apps.ldossant.vmware.tamlab.rdu2.redhat.com/max-delivery/fail
```

O resultado esperado e ver o `JMSXDeliveryCount` aumentando:

```text
Rollback aplicado. JMSXDeliveryCount=1. Body=Mensagem para testar max-delivery-attempts=-1
Rollback aplicado. JMSXDeliveryCount=2. Body=Mensagem para testar max-delivery-attempts=-1
Rollback aplicado. JMSXDeliveryCount=3. Body=Mensagem para testar max-delivery-attempts=-1
```

Consumir com commit para remover a mensagem da fila:

```shell
curl http://amq-filter-demo-amq.apps.ldossant.vmware.tamlab.rdu2.redhat.com/max-delivery/consume
```
