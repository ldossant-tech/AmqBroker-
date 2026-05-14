package com.redhat.amq;

final class AmqDestinations {

    static final String TOPIC_PIX_TRANSACTION_CHANGED = "topic-pix-transaction-changed";
    static final String QUEUE_CREDITO_TRANSACTION_CHANGED = "queue-credito-transactionchanged";
    static final String QUEUE_MAX_DELIVERY_ATTEMPTS_INFINITE = "queue-max-delivery-attempts-infinite";
    static final String ADDITIONAL_ID_PROPERTY = "additionalID";
    static final String ADDITIONAL_ID_FILTER = ADDITIONAL_ID_PROPERTY + " IS NOT NULL";

    private AmqDestinations() {
    }
}
