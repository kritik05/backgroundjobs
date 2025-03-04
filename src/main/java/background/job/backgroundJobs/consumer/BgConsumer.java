package background.job.backgroundJobs.consumer;

import background.job.backgroundJobs.event.*;
import background.job.backgroundJobs.model.*;
import background.job.backgroundJobs.repository.RunbookRepository;
import background.job.backgroundJobs.repository.RunbookRuleRepository;
import background.job.backgroundJobs.repository.TenantRepository;
import background.job.backgroundJobs.repository.TenantTicketRepository;
import background.job.backgroundJobs.service.ElasticsearchService;
import background.job.backgroundJobs.service.GithubService;
import background.job.backgroundJobs.service.JiraService;
import background.job.backgroundJobs.service.RunbookActionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.*;

@Component
public class BgConsumer {
    private final TenantRepository tenantRepository;
    private final ElasticsearchService elasticsearchService;
    private final GithubService githubService;
    private final KafkaTemplate<String, Object> ackTemplate;
    private final ObjectMapper objectMapper;
    private final TenantTicketRepository tenantTicketRepository;
    private final JiraService jiraService;
    private final RunbookRepository runbookRepository;
    private final RunbookRuleRepository runbookRuleRepository;
    private final RunbookActionService runbookActionService;

    @Value("${app.kafka.topics.ack}")
    private String ackTopic;

    public BgConsumer(ElasticsearchService elasticsearchService, TenantRepository tenantRepository, KafkaTemplate<String, Object> ackTemplate, GithubService githubService, ObjectMapper objectMapper, TenantTicketRepository tenantTicketRepository, JiraService jiraService,RunbookRepository runbookRepository,RunbookRuleRepository runbookRuleRepository,RunbookActionService runbookActionService) {
        this.githubService=githubService;
        this.elasticsearchService = elasticsearchService;
        this.tenantRepository=tenantRepository;
        this.ackTemplate=ackTemplate;
        this.objectMapper=objectMapper;
        this.tenantTicketRepository=tenantTicketRepository;
        this.jiraService=jiraService;
        this.runbookRepository=runbookRepository;
        this.runbookRuleRepository=runbookRuleRepository;
        this.runbookActionService=runbookActionService;
    }

    @KafkaListener(
            topics = "#{'${app.kafka.topics.update}'}", // e.g. "jfc.jobs"
            groupId = "jfc-ingestion-consumer",
            containerFactory = "unifiedListenerContainerFactory"
    )
    public void onMessage(String message) throws Exception {
        JsonNode root = objectMapper.readTree(message);
        String typeString = root.get("type").asText();
        switch (typeString) {
            case "update":consumeUpdateRequestEvent(message);
                break;
            case "ticketCreate":consumeTicketCreateRequest(message);
                break;
            case "ticketTransition":consumeTransitionRequest(message);
            case "runbook":consumeRunbookRequest(message);
                break;
            default : {
                System.out.println("Unknown event type:");
            }
        }
    }

    private void consumeRunbookRequest(String message) throws JsonProcessingException {
        RunbookRequestEvent event= objectMapper.readValue(message, RunbookRequestEvent.class);
        String originalEventId = event.getEventId();
        try{
            System.out.println("Runbook request received");
            RunbookPayload payload = event.getPayload();
            Integer tenantId = payload.getTenantId();
            String trigger = payload.getTriggerType();
            List<String> findingIds = payload.getFindingIds();
            List<Runbook> tenantRunbooks = runbookRepository.findByTenantIdAndIsEnabledTrue(Long.valueOf(tenantId));
            if (tenantRunbooks.isEmpty()) {
                System.out.println("No enabled runbooks for tenant " + tenantId);
                sendAck(originalEventId, "SUCCESS");
                return;
            }

            List<RunbookRule> allRules = runbookRuleRepository.findByRunbookInAndIsEnabledTrue(tenantRunbooks);
            if (allRules.isEmpty()) {
                System.out.println("No rules found for these runbooks");
                sendAck(originalEventId, "SUCCESS");
            }
            for (RunbookRule rule : allRules) {
                if (!rule.matchesTrigger(trigger)) {
                    continue;
                }
                System.out.println("Matching rule: " + rule);

                List<FilterType> filterTypes = parseFilterTypes(rule.getFilterType());
                Map<String, String> filterParams = parseStringMap(rule.getFilterParams());

                List<String> matchedFindings = new ArrayList<>();
                for (String fid : findingIds) {
                    Map<String, Object> doc = elasticsearchService.getFindingDoc(fid, tenantId);
                    if (doc == null) continue;
                    if (elasticsearchService.doesDocMatchFilters(doc, filterTypes, filterParams)) {
                        matchedFindings.add(fid);
                    }
                }

                if (matchedFindings.isEmpty()) {
                    System.out.println("No findings matched for ruleId=" + rule.getRuleId());
                    continue;
                }
                List<ActionType> actions = parseActionTypes(rule.getActionType());
                Map<String, Object> actionParams = parseObjectMap(rule.getActionParams());

                for (ActionType actionType : actions) {
                    // The sub-object for that action, e.g. "create_ticket" => {...}
                    // We store them in actionParams under a key matching the enum name or lowercased
                    // So if actionType=CREATE_TICKET, we look for "create_ticket" in the map
                    String key = actionType.name().toLowerCase(); // e.g. "create_ticket"
                    Object subObj = actionParams.get(key);
                    Map<String, Object> subMap = (subObj instanceof Map) ? (Map<String, Object>) subObj : new HashMap<>();
                    System.out.println("Applying action: " + actionType + " with params: " + subMap);
                    runbookActionService.applyAction(
                            actionType,
                            subMap,
                            matchedFindings,
                            tenantId
                    );
                }
            }
            System.out.println("Runbook processing complete");
            sendAck(originalEventId, "SUCCESS");
        }catch (Exception e){
            sendAck(originalEventId, "FAIL");
        }
    }


private List<FilterType> parseFilterTypes(String json) {
    if (json == null || json.isEmpty()) return Collections.emptyList();
    try {
        List<String> rawList = objectMapper.readValue(json, List.class);
        return rawList.stream()
                .map(s -> FilterType.valueOf(s.toUpperCase()))
                .collect(Collectors.toList());
    } catch (Exception e) {
        e.printStackTrace();
        return Collections.emptyList();
    }
}

private Map<String, String> parseStringMap(String json) {
    if (json == null || json.isEmpty()) return Collections.emptyMap();
    try {
        return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
        e.printStackTrace();
        return Collections.emptyMap();
    }
}

private List<ActionType> parseActionTypes(String json) {
    if (json == null || json.isEmpty()) return Collections.emptyList();
    try {
        List<String> rawList = objectMapper.readValue(json, List.class);
        return rawList.stream()
                .map(s -> ActionType.valueOf(s.toUpperCase()))
                .collect(Collectors.toList());
    } catch (Exception e) {
        e.printStackTrace();
        return Collections.emptyList();
    }
}

private Map<String, Object> parseObjectMap(String json) {
    if (json == null || json.isEmpty()) return new HashMap<>();
    try {
        return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
        e.printStackTrace();
        return new HashMap<>();
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

            sendAck(originalEventId, "SUCCESS");
        } catch (Exception e) {
            sendAck(originalEventId, "FAIL");
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

            sendAck(originalEventId, "SUCCESS");
        } catch (Exception e) {
            sendAck(originalEventId, "FAIL");
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

            sendAck(originalEventId, "SUCCESS");
            System.out.println("sent ack from background ");
        } catch (Exception e) {
            sendAck(originalEventId, "FAIL");
            System.out.println("sent ack from background");
        }
    }

    private void sendAck(String eventId, String status) {
        AcknowledgementPayload ackPayload = new AcknowledgementPayload(eventId, status);
        AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
        ackTemplate.send(ackTopic, ackEvent);
    }
}
