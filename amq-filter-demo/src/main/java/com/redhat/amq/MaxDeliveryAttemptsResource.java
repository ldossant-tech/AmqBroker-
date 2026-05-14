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

@Path("/max-delivery")
public class MaxDeliveryAttemptsResource {

    @Inject
    ConnectionFactory connectionFactory;

    @GET
    @Path("/send")
    public String send() throws JMSException {
        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue(AmqDestinations.QUEUE_MAX_DELIVERY_ATTEMPTS_INFINITE);
            Message message = context.createTextMessage("Mensagem para testar max-delivery-attempts=-1");

            context.createProducer().send(queue, message);

            return "Mensagem enviada para teste de max-delivery-attempts=-1";
        }
    }

    @GET
    @Path("/fail")
    public String fail(@QueryParam("timeout") Long timeout) throws JMSException {
        long timeoutMillis = timeout != null ? timeout : 5000L;

        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = context.createQueue(AmqDestinations.QUEUE_MAX_DELIVERY_ATTEMPTS_INFINITE);
            JMSConsumer consumer = context.createConsumer(queue);
            Message message = consumer.receive(timeoutMillis);

            if (message == null) {
                context.rollback();
                return "Nenhuma mensagem recebida";
            }

            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            String body = message.getBody(String.class);

            context.rollback();

            return "Rollback aplicado. JMSXDeliveryCount=" + deliveryCount + ". Body=" + body;
        }
    }

    @GET
    @Path("/consume")
    public String consume(@QueryParam("timeout") Long timeout) throws JMSException {
        long timeoutMillis = timeout != null ? timeout : 5000L;

        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = context.createQueue(AmqDestinations.QUEUE_MAX_DELIVERY_ATTEMPTS_INFINITE);
            JMSConsumer consumer = context.createConsumer(queue);
            Message message = consumer.receive(timeoutMillis);

            if (message == null) {
                context.rollback();
                return "Nenhuma mensagem recebida";
            }

            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            String body = message.getBody(String.class);

            context.commit();

            return "Mensagem consumida com commit. JMSXDeliveryCount=" + deliveryCount + ". Body=" + body;
        }
    }
}
