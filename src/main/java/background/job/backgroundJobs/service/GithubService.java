package background.job.backgroundJobs.service;


import background.job.backgroundJobs.model.Tenant;
import background.job.backgroundJobs.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.*;

@Service
public class GithubService {

    private final RestTemplate restTemplate;
    private final TenantRepository tenantRepository;
    public GithubService(RestTemplate restTemplate, TenantRepository tenantRepository) {
        this.restTemplate = restTemplate;
        this.tenantRepository = tenantRepository;
    }

    public void updateAlert(String id, String tooltype, String newState, String dismissedReason,int tenantId) throws IOException {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("No tenant found with id " + tenantId));

        String owner = tenant.getOwner();
        String repo  = tenant.getRepo();
        String githubToken = tenant.getPat();
        String tool = mapStringToToolType(tooltype);
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/"+ tool+"/alerts/" + id;

        Map<String, String> body = new HashMap<>();
        body.put("state", newState);
        if ("dismissed".equals(newState) && dismissedReason != null) {
            body.put("dismissed_reason", dismissedReason);
        }
        else if("resolved".equals(newState)&&dismissedReason!=null){
            body.put("resolution", dismissedReason);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(githubToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PATCH, entity, String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("GitHub update failed: " + response.getStatusCode());
        }
    }

    private String mapStringToToolType(String tooltype) {
        switch (tooltype) {
            case "CODESCAN":
                return "code-scanning";
            case "DEPENDABOT":
                return "dependabot";
            case "SECRETSCAN":
                return "secret-scanning";
            default:
                return "";
        }
    }
}
