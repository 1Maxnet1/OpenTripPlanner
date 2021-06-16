package org.opentripplanner.updater.siri;

import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.ReadinessBlockingUpdater;
import org.opentripplanner.updater.stoptime.TimetableSnapshotSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Common functionalities for connecting to Azure Service Bus. Implement abstract functions with business logic to consume messages and errors.
 * Override {@link #configure(Graph, JsonNode)} to configure queue specific parameters.
 */
public abstract class AbstractAzureSiriUpdater extends ReadinessBlockingUpdater implements GraphUpdater {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    protected GraphUpdaterManager updaterManager;
    private ServiceBusProcessorClient eventProcessor;
    private String topicName;
    private String subscriptionName;
    private String serviceBusUrl;

    /**
     * The URL used to fetch all initial updates
     */
    private String dataInitializationUrl;

    private final Consumer<ServiceBusReceivedMessageContext> messageConsumer = this::messageConsumer;
    private final Consumer<ServiceBusErrorContext> errorConsumer = this::errorConsumer;
    private ServiceBusAdministrationAsyncClient serviceBusAdmin;

    /**
     * Consume Service Bus topic message and implement business logic.
     * @param messageContext The Service Bus processor message context that holds a received message and additional methods to settle the message.
     */
    protected abstract void messageConsumer(ServiceBusReceivedMessageContext messageContext);

    /**
     * Consume error and decide how to manage it.
     * @param errorContext Context for errors handled by the ServiceBusProcessorClient.
     */
    protected abstract void errorConsumer(ServiceBusErrorContext errorContext);

    /**
     * Get common parameters always required to connect to Azure Service Bus. Specific parameters for ET, SX or VM topics should be extracted by
     * overriding this function in child classes.
     * @param graph Reference to the Graph database instance.
     * @param jsonNode JSON input parameters from router-config.json file
     */
    @Override
    public void configure(Graph graph, JsonNode jsonNode) throws Exception {
        Preconditions.checkNotNull(jsonNode.path("topic"), "'topic' must be set");
        Preconditions.checkNotNull(jsonNode.path("servicebus-url"), "'servicebus-url' must be set");

        topicName = jsonNode.path("topic").asText();
        serviceBusUrl = jsonNode.path("servicebus-url").asText();
        dataInitializationUrl = jsonNode.path("servicebus-history-url").asText();

        if (graph.timetableSnapshotSource == null) {
            // Add snapshot source to graph
            graph.timetableSnapshotSource = new TimetableSnapshotSource(graph);
        }
    }

    /**
     * Start the subscription to given topic, configure has already been invoked before this.
     */
    @Override
    public void run() throws Exception {
        // In Kubernetes this should be the POD identifier
        subscriptionName = System.getenv("HOSTNAME");
        if (subscriptionName == null || subscriptionName.isBlank()) {
            subscriptionName = "otp-"+UUID.randomUUID().toString();
        }

        // Client with permissions to create subscription
        serviceBusAdmin = new ServiceBusAdministrationClientBuilder()
                .connectionString(serviceBusUrl)
                .buildAsyncClient();

        // If Idle more then one day, then delete subscription so we don't have old obsolete subscriptions on Azure Service Bus
        var options = new CreateSubscriptionOptions();
        options.setAutoDeleteOnIdle(Duration.ofDays(1));

        serviceBusAdmin.createSubscription(topicName, subscriptionName, options).block();

        LOG.info("Service Bus created subscription {}", subscriptionName);

        // Initialize historical Siri data
        initializeData();

        eventProcessor = new ServiceBusClientBuilder()
                .connectionString(serviceBusUrl)
                .processor()
                .topicName(topicName)
                .subscriptionName(subscriptionName)
                .processError(errorConsumer)
                .processMessage(messageConsumer)
                .buildProcessorClient();

        eventProcessor.start();
        LOG.info("Service Bus processor started for topic {} and subscription {}", topicName, subscriptionName);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(this::teardown));
        } catch (IllegalStateException e) {
            LOG.error(e.getLocalizedMessage(), e);
            teardown();
        }
    }

    /**
     * InitializeData - wrapping method that calls an implementation of initialize data - and blocks readiness till finished
     */
    private void initializeData() {
        int sleepPeriod = 1000;
        int attemptCounter = 1;
        while (!isInitialized) {
            try {
                initializeData(dataInitializationUrl, messageConsumer);
                isInitialized = true;
            } catch (Exception e) {
                sleepPeriod = sleepPeriod * 2;

                LOG.warn("Caught exception while initializing data will retry after {} ms - attempt {}. ({})", sleepPeriod, attemptCounter++, e.toString());
                try {
                    Thread.sleep(sleepPeriod);
                } catch (InterruptedException interruptedException) {
                    //Ignore
                }
            }
        }
    }

    protected abstract void initializeData(String url, Consumer<ServiceBusReceivedMessageContext> consumer) throws IOException;

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.updaterManager = updaterManager;
    }

    @Override
    public void teardown() {
        eventProcessor.stop();
        serviceBusAdmin.deleteSubscription(topicName, subscriptionName).block();
        LOG.info("Subscription {} deleted on topic {}", subscriptionName, topicName);
    }

    @Override
    public void setup() throws Exception { }

    /**
     * Make some sensible logging on error and if Service Bus is busy, sleep for some time before try again to get messages.
     * This code snippet is taken from Microsoft example https://docs.microsoft.com/sv-se/azure/service-bus-messaging/service-bus-java-how-to-use-queues.
     * @param errorContext Context for errors handled by the ServiceBusProcessorClient.
     */
    protected void defaultErrorConsumer(ServiceBusErrorContext errorContext) {
        LOG.error("Error when receiving messages from namespace={}, Entity={}", errorContext.getFullyQualifiedNamespace(), errorContext.getEntityPath());

        if(!(errorContext.getException() instanceof ServiceBusException)) {
            LOG.error("Non-ServiceBusException occurred!", errorContext.getException());
            return;
        }

        var e = (ServiceBusException) errorContext.getException();
        var reason = e.getReason();

        if(reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                || reason == ServiceBusFailureReason.UNAUTHORIZED) {

            LOG.error("An unrecoverable error occurred. Stopping processing with reason {} {}", reason, e.getMessage());
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            LOG.error("Message lock lost for message", e);
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            LOG.error("Service Bus is busy, wait and try again");
            try {
                // Choosing an arbitrary amount of time to wait until trying again.
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e2) {
                LOG.error("Unable to sleep for period of time");
            }
        } else {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @return the current datetime adjusted to the current timezone
     */
    protected long now() {
        return ZonedDateTime.now().toInstant().toEpochMilli();
    }
}
