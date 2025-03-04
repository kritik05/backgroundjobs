package background.job.backgroundJobs.service;

import background.job.backgroundJobs.model.ActionType;
import background.job.backgroundJobs.model.TenantTicket;
import background.job.backgroundJobs.repository.TenantTicketRepository;
import background.job.backgroundJobs.service.mappers.CodeScanMapper;
import background.job.backgroundJobs.service.mappers.DependabotMapper;
import background.job.backgroundJobs.service.mappers.SecretScanMapper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class RunbookActionService {
    private final ElasticsearchService elasticsearchService;
    private final GithubService githubService;
    private final TenantTicketRepository tenantTicketRepository;
    private final JiraService jiraService;


    public RunbookActionService(ElasticsearchService elasticsearchService, GithubService githubService, TenantTicketRepository tenantTicketRepository, JiraService jiraService) {
        this.githubService=githubService;
        this.elasticsearchService = elasticsearchService;
        this.tenantTicketRepository=tenantTicketRepository;
        this.jiraService=jiraService;
    }
    public void applyAction(ActionType actionType,
                            Map<String, Object> actionParamObj,
                            List<String> findingIds,
                            int tenantId) throws IOException {
        switch (actionType) {
            case CREATE_TICKET:
                createTicketForFindings(actionParamObj, findingIds, tenantId);
                break;
            case UPDATE_STATUS:
                updateStatusForFindings(actionParamObj, findingIds, tenantId);
                break;
        }
    }

    private void createTicketForFindings(Map<String, Object> params, List<String> findingIds, int tenantId) throws IOException {
        for (String fid : findingIds) {
            Map<String, Object> doc = elasticsearchService.getFindingDoc(fid, tenantId);
            if (doc == null) {
                System.out.println("No ES doc for " + fid + ", skipping createTicket.");
                continue;
            }
            String esTitle = doc.get("title") != null ? doc.get("title").toString() : "No Title";
            String esDescription = doc.get("description") != null ? doc.get("description").toString() : "No Description";
            String createdTicketKey = jiraService.createTicket(esTitle, esDescription, tenantId);
            elasticsearchService.updateTicketId(fid, tenantId, createdTicketKey);

            TenantTicket tenantTicket = new TenantTicket(tenantId, createdTicketKey, fid);
            tenantTicketRepository.save(tenantTicket);

        }
    }

    private void updateStatusForFindings(
            Map<String, Object> params,
            List<String> findingIds,
            int tenantId
    ) throws IOException {

        String fromState    = (String) params.getOrDefault("from", "OPEN");
        String toState      = (String) params.getOrDefault("to", "SUPPRESSED");
        System.out.println("Updating status for findings: from " + fromState + " to " + toState);
        for (String fid : findingIds) {
            Map<String, Object> doc = elasticsearchService.getFindingDoc(fid, tenantId);
            if (doc == null) {
                System.out.println("No ES doc for " + fid + ", skipping.");
                continue;
            }

            String docStatus = doc.get("status") != null ? doc.get("status").toString() : "";
            if (!docStatus.equalsIgnoreCase(fromState)) {
                System.out.println("Finding " + fid + " docStatus=" + docStatus
                        + " doesn't match 'from'=" + fromState + ". Skipping.");
                continue;
            }

            String docToolType = doc.get("toolType") != null
                    ? doc.get("toolType").toString()
                    : "CODESCAN";


            String derivedDismissedReason = mapDismissedReasonFromState(toState, docToolType);

            // (D) Get alertNumber from doc.additionalData.number
            String alertNumber = null;
            if (doc.containsKey("additionalData")) {
                Object addData = doc.get("additionalData");
                if (addData instanceof Map) {
                    Map<String, Object> addDataMap = (Map<String, Object>) addData;
                    if (addDataMap.containsKey("number")) {
                        alertNumber = String.valueOf(addDataMap.get("number"));
                    }
                }
            }
            // (E) If alertNumber is found, do GitHub update
            String newstate=mapDismissedStateFromState(docToolType,toState);
            if (alertNumber != null && !alertNumber.isEmpty()) {

                githubService.updateAlert(alertNumber, docToolType, newstate, derivedDismissedReason, tenantId);
            }

            elasticsearchService.updateState(fid, docToolType, newstate, derivedDismissedReason, tenantId);
        }
    }

    private final CodeScanMapper codeScanMapper = new CodeScanMapper();
    private final DependabotMapper dependabotMapper = new DependabotMapper();
    private final SecretScanMapper secretScanMapper = new SecretScanMapper();

    private String mapDismissedReasonFromState(String toState,String tooltype) {
        switch ( tooltype ) {
            case "CODESCAN":
                return codeScanMapper.dismissedReason(toState.toUpperCase()); // or "not used" etc.
            case "DEPENDABOT":
                return dependabotMapper.dismissedReason(toState.toUpperCase());
            case "SECRETSCAN":
                return secretScanMapper.dismissedReason(toState.toUpperCase());
            default:
                return "auto-runbook";
        }
    }
    private String mapDismissedStateFromState(String tooltype,String toState) {
        switch ( tooltype ) {
            case "CODESCAN":
                return codeScanMapper.dismissedState(toState.toUpperCase()); // or "not used" etc.
            case "DEPENDABOT":
                return dependabotMapper.dismissedState(toState.toUpperCase());
            case "SECRETSCAN":
                return secretScanMapper.dismissedState(toState.toUpperCase());
            default:
                return "auto-runbook";
        }
    }

}
