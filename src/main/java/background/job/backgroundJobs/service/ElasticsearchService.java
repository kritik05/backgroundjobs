package background.job.backgroundJobs.service;
import background.job.backgroundJobs.model.FilterType;
import background.job.backgroundJobs.model.Status;
import background.job.backgroundJobs.model.Tenant;
import background.job.backgroundJobs.model.TenantTicket;
import background.job.backgroundJobs.repository.TenantRepository;
import background.job.backgroundJobs.repository.TenantTicketRepository;
import background.job.backgroundJobs.service.mappers.CodeScanMapper;
import background.job.backgroundJobs.service.mappers.DependabotMapper;
import background.job.backgroundJobs.service.mappers.SecretScanMapper;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
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

    public Map<String, Object> getFindingDoc(String findingId, int tenantId) {
        // Attempt to load the tenant from DB
        Optional<Tenant> maybeTenant = tenantRepository.findById(tenantId);
        if (maybeTenant.isEmpty()) {
            // If the tenant does not exist, doc will always be null unless we fallback to some default index
            System.out.println("Tenant not found for tenantId={} => cannot lookup doc ID={}" + tenantId + findingId);
            return null;  // Or fallback to index "f1" if that's intended
        }

        Tenant tenant = maybeTenant.get();
        String index = tenant.getFindingindex();

        try {
            GetResponse<Map> response = esClient.get(g -> g
                            .index(index)
                            .id(findingId),
                    Map.class
            );
            if (response.found()) {
                return response.source();
            } else {
            }
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Checks if the doc's fields match the provided filters.
     * filterParams might be {"status":"OPEN","severity":"CRITICAL"}.
     */
    public boolean doesDocMatchFilters(
            Map<String, Object> doc,
            List<FilterType> filterTypes,
            Map<String, String> filterParams
    ) {
        // For each FilterType, compare doc's value to filterParams
        for (FilterType ft : filterTypes) {
            switch (ft) {
                case STATUS:
                    String requiredStatus = filterParams.get("status");
                    if (requiredStatus != null) {
                        String docStatus = (doc.get("status") != null) ? doc.get("status").toString() : "";
                        if (!requiredStatus.equalsIgnoreCase(docStatus)) {
                            return false;
                        }
                    }
                    break;
                case SEVERITY:
                    String requiredSeverity = filterParams.get("severity");
                    if (requiredSeverity != null) {
                        String docSeverity = (doc.get("severity") != null) ? doc.get("severity").toString() : "";
                        if (!requiredSeverity.equalsIgnoreCase(docSeverity)) {
                            return false;
                        }
                    }
                    break;
                // If more filter types are added, handle them here
            }
        }
        return true;
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
                return secretScanMapper.mapStatus(state,dismissedReason);
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

