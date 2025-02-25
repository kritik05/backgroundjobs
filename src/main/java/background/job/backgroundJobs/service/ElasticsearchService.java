package background.job.backgroundJobs.service;
import background.job.backgroundJobs.model.Status;
import background.job.backgroundJobs.model.Tenant;
import background.job.backgroundJobs.model.TenantTicket;
import background.job.backgroundJobs.repository.TenantRepository;
import background.job.backgroundJobs.repository.TenantTicketRepository;
import background.job.backgroundJobs.service.mappers.CodeScanMapper;
import background.job.backgroundJobs.service.mappers.DependabotMapper;
import background.job.backgroundJobs.service.mappers.SecretScanMapper;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;
    private final TenantRepository tenantRepository;
    private final TenantTicketRepository tenantTicketRepository;
    private final JiraService jiraService;

    public ElasticsearchService(ElasticsearchClient esClient, TenantRepository tenantRepository,TenantTicketRepository tenantTicketRepository,JiraService jiraService) {
        this.esClient = esClient;
        this.tenantRepository=tenantRepository;
        this.tenantTicketRepository=tenantTicketRepository;
        this.jiraService=jiraService;
    }


    public void updateState(String uuid, String tooltype, String state, String dismissedReason,int tenantId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.get();
        String esindex= tenant.getFindingindex();
        Map<String, Object> partial = new HashMap<>();
        Status finalStatus = mapStringToToolType(tooltype,state,dismissedReason);
        partial.put("status", finalStatus);
        switch (finalStatus){
            case FIXED:
            case CONFIRM:
            case SUPPRESSED:
            case FALSE_POSITIVE: handleTicketTransition(uuid,tenantId);
            break;
        }
        partial.put("updatedAt", Instant.now().toString());
        UpdateRequest<Map<String, Object>, Map<String, Object>> updateReq = UpdateRequest.of(u -> u
                .index(esindex)
                .id(uuid)
                .doc(partial)
        );

        esClient.update(updateReq, Map.class);
    }

    private void handleTicketTransition(String uuid, int tenantId) {
        Optional<TenantTicket> tenantTicket = tenantTicketRepository.findByFindingIdAndTenantId(uuid, tenantId);

        tenantTicket.ifPresent(ticket -> {
            String ticketId = ticket.getTicketId();
            jiraService.transitionToDone(ticketId, tenantId);
        });
    }
    private final CodeScanMapper codeScanMapper = new CodeScanMapper();
    private final DependabotMapper dependabotMapper = new DependabotMapper();
    private final SecretScanMapper secretScanMapper = new SecretScanMapper();

    private Status mapStringToToolType(String tooltype,String state,String dismissedReason) {
        switch (tooltype) {
            case "CODESCAN":
                return  codeScanMapper.mapStatus(state,dismissedReason);
            case "DEPENDABOT":
                return dependabotMapper.mapStatus(state,dismissedReason);
            case "SECRETSCAN":
                return secretScanMapper.mapStatus(state);
            default:
                return Status.OPEN;
        }

    }

    public void updateTicketId(String uuid, int tenantId, String newTicketId) throws IOException {
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant.orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        String esIndex = tenant.getFindingindex(); // e.g., "tenant-findings"

        Map<String, Object> partial = new HashMap<>();
        partial.put("ticketId", newTicketId);
        partial.put("updatedAt", Instant.now().toString());

        UpdateRequest<Map<String, Object>, Map<String, Object>> updateReq = UpdateRequest.of(u -> u
                .index(esIndex)
                .id(uuid)
                .doc(partial)
        );
        esClient.update(updateReq, Map.class);
    }


}

