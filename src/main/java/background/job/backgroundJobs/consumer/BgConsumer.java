package background.job.backgroundJobs.consumer;

import background.job.backgroundJobs.event.AcknowledgementEvent;
import background.job.backgroundJobs.event.TicketCreateRequestEvent;
import background.job.backgroundJobs.event.TicketTransitionRequestEvent;
import background.job.backgroundJobs.event.UpdateRequestEvent;
import background.job.backgroundJobs.model.AcknowledgementPayload;
import background.job.backgroundJobs.model.StateRequest;
import background.job.backgroundJobs.model.Tenant;
import background.job.backgroundJobs.model.TenantTicket;
import background.job.backgroundJobs.repository.TenantRepository;
import background.job.backgroundJobs.repository.TenantTicketRepository;
import background.job.backgroundJobs.service.ElasticsearchService;
import background.job.backgroundJobs.service.GithubService;
import background.job.backgroundJobs.service.JiraService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class BgConsumer {
    private final TenantRepository tenantRepository;
    private final ElasticsearchService elasticsearchService;
    private final GithubService githubService;
    private final KafkaTemplate<String, Object> ackTemplate;
    private final ObjectMapper objectMapper;
    private final TenantTicketRepository tenantTicketRepository;
    private final JiraService jiraService;

    @Value("${app.kafka.topics.ack}")
    private String ackTopic;

    public BgConsumer(ElasticsearchService elasticsearchService, TenantRepository tenantRepository, KafkaTemplate<String, Object> ackTemplate, GithubService githubService, ObjectMapper objectMapper, TenantTicketRepository tenantTicketRepository, JiraService jiraService) {
        this.githubService=githubService;
        this.elasticsearchService = elasticsearchService;
        this.tenantRepository=tenantRepository;
        this.ackTemplate=ackTemplate;
        this.objectMapper=objectMapper;
        this.tenantTicketRepository=tenantTicketRepository;
        this.jiraService=jiraService;
    }

//    @KafkaListener(
//            topics = "${app.kafka.topics.update}",
//            groupId = "${spring.kafka.consumer.group-id}",
//            containerFactory = "updateRequestEventListenerContainerFactory"
//    )
    @KafkaListener(
            topics = "#{'${app.kafka.topics.update}'}", // e.g. "jfc.jobs"
            groupId = "jfc-ingestion-consumer",
            containerFactory = "unifiedListenerContainerFactory"
    )
    public void onMessage(String message) throws Exception {
        // 1) Read the raw JSON
        System.out.println(message);
        JsonNode root = objectMapper.readTree(message);
        // 2) Check for the "type" field
        String typeString = root.get("type").asText();
        // 3) Based on event type, deserialize into the correct DTO
        switch (typeString) {
            case "update":consumeUpdateRequestEvent(message);
                break;
            case "ticketCreate":consumeTicketCreateRequest(message);
                break;
            case "ticketTransition":consumeTransitionRequest(message);
                break;
            default : {
                // ignore or log error
                System.out.println("Unknown event type:");
            }
        }
    }

    private void consumeTransitionRequest(String message) throws JsonProcessingException{
        TicketTransitionRequestEvent event= objectMapper.readValue(message, TicketTransitionRequestEvent.class);
        String originalEventId = event.getEventId();
        try {
            System.out.println("Transition request received");
            int tenantId = event.getPayload().getTenantId();
            String ticketId = event.getPayload().getTicketId();

            Optional<TenantTicket> maybeTicket = tenantTicketRepository.findByTicketId(ticketId);

            if (maybeTicket.isEmpty() || !maybeTicket.get().getTenantId().equals(tenantId)) {
                throw new RuntimeException("Ticket not found or not linked to this tenant.");
            }

            jiraService.transitionToDone(ticketId, tenantId);

            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "SUCCESS");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
        } catch (Exception e) {
            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "FAIL");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
        }

    }

    private void consumeTicketCreateRequest(String message) throws JsonProcessingException {
        TicketCreateRequestEvent event= objectMapper.readValue(message, TicketCreateRequestEvent.class);
        String originalEventId = event.getEventId();
        try {
            String uuid = event.getPayload().getUuid();
            int tenantId = event.getPayload().getTenantId();
            String summary = event.getPayload().getSummary();
            String description = event.getPayload().getDescription();

            // 1. Check if there's already a ticket for this finding & tenant
            Optional<TenantTicket> existingTicket = tenantTicketRepository.findByFindingIdAndTenantId(uuid, tenantId);
            if (existingTicket.isPresent()) {
                throw new RuntimeException("Ticket already exists for this finding and tenant.");
            }

            // 2. (Optionally) get the finding from ES to verify existence or retrieve data
            // Map<String, Object> findingData = elasticsearchService.getFinding(uuid, tenantId);
            // if (findingData.isEmpty()) {
            //     return;
            // }

            String createdTicketKey = jiraService.createTicket(summary, description, tenantId);

            elasticsearchService.updateTicketId(uuid, tenantId, createdTicketKey);

            TenantTicket tenantTicket = new TenantTicket(tenantId, createdTicketKey, uuid);
            tenantTicketRepository.save(tenantTicket);

            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "SUCCESS");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
        } catch (Exception e) {
            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "FAIL");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
        }
    }

    public void consumeUpdateRequestEvent(String message) throws JsonProcessingException {
        UpdateRequestEvent event= objectMapper.readValue(message, UpdateRequestEvent.class);
        String originalEventId = event.getEventId();
        try {
        String uuid = event.getPayload().getUuid();
        String tooltype = event.getPayload().getTooltype();
        StateRequest request=event.getPayload().getRequest();
        String alertNumber=event.getPayload().getAlertNumber();
        int tenantId=event.getPayload().getTenantId();
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        if (optionalTenant.isEmpty()) {
            return;
        }


            githubService.updateAlert(alertNumber,tooltype, request.getState(), request.getDismissedReason(),tenantId);
            elasticsearchService.updateState(uuid,tooltype ,request.getState(), request.getDismissedReason(),tenantId);


            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "SUCCESS");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
            System.out.println("sent ack from background ");
        } catch (Exception e) {
            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "FAIL");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
            System.out.println("sent ack from background");
        }
    }


}
