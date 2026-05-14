package com.redhat.amq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingResourceTest {

    @Test
    void destinationsMatchBrokerFilterConfiguration() {
        assertEquals("topic-pix-transaction-changed", AmqDestinations.TOPIC_PIX_TRANSACTION_CHANGED);
        assertEquals("queue-credito-transactionchanged", AmqDestinations.QUEUE_CREDITO_TRANSACTION_CHANGED);
        assertEquals("queue-max-delivery-attempts-infinite", AmqDestinations.QUEUE_MAX_DELIVERY_ATTEMPTS_INFINITE);
        assertEquals("additionalID", AmqDestinations.ADDITIONAL_ID_PROPERTY);
        assertEquals("additionalID IS NOT NULL", AmqDestinations.ADDITIONAL_ID_FILTER);
    }
}
