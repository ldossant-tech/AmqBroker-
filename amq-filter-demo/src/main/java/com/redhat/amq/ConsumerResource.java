package com.redhat.amq;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/consume")
public class ConsumerResource {

    @Inject
    ConnectionFactory connectionFactory;

    @GET
    public String consume(@QueryParam("timeout") Long timeout) throws JMSException {
        long timeoutMillis = timeout != null ? timeout : 5000L;

        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue(AmqDestinations.QUEUE_CREDITO_TRANSACTION_CHANGED);
            JMSConsumer consumer = context.createConsumer(queue);
            Message message = consumer.receive(timeoutMillis);

            if (message == null) {
                return "Nenhuma mensagem recebida";
            }

            return message.getBody(String.class);
        }
    }
}
