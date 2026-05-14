package com.redhat.amq;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Topic;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/send")
public class ProducerResource {

    @Inject
    ConnectionFactory connectionFactory;

    @GET
    @Path("/with-property")
    public String sendWithProperty() throws JMSException {

        try (JMSContext context = connectionFactory.createContext()) {

            Topic topic = context.createTopic(AmqDestinations.TOPIC_PIX_TRANSACTION_CHANGED);

            var message = context.createTextMessage("Mensagem COM additionalID");

            message.setStringProperty(AmqDestinations.ADDITIONAL_ID_PROPERTY, "123");

            context.createProducer().send(topic, message);

            return "Mensagem enviada COM property";
        }
    }

    @GET
    @Path("/without-property")
    public String sendWithoutProperty() {

        try (JMSContext context = connectionFactory.createContext()) {

            Topic topic = context.createTopic(AmqDestinations.TOPIC_PIX_TRANSACTION_CHANGED);

            context.createProducer().send(
                    topic,
                    "Mensagem SEM additionalID");

            return "Mensagem enviada SEM property";
        }
    }
}
