package background.job.backgroundJobs.service;


import background.job.backgroundJobs.model.Tenant;
import background.job.backgroundJobs.repository.TenantRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class JiraService {
    private final TenantRepository tenantRepository;

    public JiraService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }
    public String createTicket(String summary, String description, int tenantId) {
        // 1. Fetch tenant to retrieve project_name, project_key, username, token, etc.
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        Tenant tenant = optionalTenant
                .orElseThrow(() -> new RuntimeException("Tenant not found with ID: " + tenantId));

        // 2. Build the Jira URL
        //    E.g., https://capstoneticket.atlassian.net/rest/api/2/issue/
        String jiraUrl = "https://" + tenant.getProject_name() + "/rest/api/2/issue/";

        // 3. Construct the JSON body (you can use a HashMap or a custom DTO)
        Map<String, Object> issueType = new HashMap<>();
        // Typically, the "id" for a standard "Story" or "Task" might vary in your Jira
        // Check your Jira for the correct issue type ID
        issueType.put("name", "Bug");

        Map<String, Object> projectField = new HashMap<>();
        projectField.put("key", tenant.getProject_key());

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", projectField);
        fields.put("summary", summary);
        fields.put("description", description);
        fields.put("issuetype", issueType);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fields", fields);

        // 4. Build headers with Basic Auth
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Basic auth with username:token
        String authString = tenant.getUsername() + ":" + tenant.getToken();
        String base64Creds = Base64.getEncoder().encodeToString(authString.getBytes());
        headers.add("Authorization", "Basic " + base64Creds);

        // 5. Execute the POST request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> responseEntity = restTemplate
                .postForEntity(jiraUrl, requestEntity, Map.class);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            // Jira usually returns a JSON with "key" among other fields
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.containsKey("key")) {
                return (String) responseBody.get("key"); // e.g. "CAP-123"
            } else {
                throw new RuntimeException("Jira ticket created, but 'key' not found in response.");
            }
        } else {
            throw new RuntimeException("Failed to create Jira ticket. HTTP Status: "
                    + responseEntity.getStatusCode());
        }
    }


    public void transitionToDone(String ticketId, int tenantId) {
        System.out.println(ticketId+tenantId);
        Tenant tenant = getTenantOrThrow(tenantId);
        String baseUrl = "https://" + tenant.getProject_name() + "/rest/api/2/issue/" + ticketId;
        HttpHeaders headers = buildAuthHeaders(tenant.getUsername(), tenant.getToken());
        RestTemplate restTemplate = new RestTemplate();

        while (true) {
            // 1) GET current transitions
            String transitionsUrl = baseUrl + "/transitions?expand=transitions.fields";
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                    transitionsUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            if (getResponse.getStatusCode() != HttpStatus.OK || getResponse.getBody() == null) {
                throw new RuntimeException("Failed to retrieve transitions for: " + ticketId);
            }

            // transitions data is typically in getResponseBody.get("transitions")
            Map<String, Object> body = getResponse.getBody();
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) body.get("transitions");
            if (transitions == null || transitions.isEmpty()) {
                // No more transitions => we must be at "Done" or an unknown state
                break;
            }

            // 2) The scenario says there's only one valid next transition in each step
            //    So let's pick the first (or only) transition and do a POST to apply it
            Map<String, Object> transition = transitions.get(0);
            String transitionId = (String) transition.get("id");

            // 3) Perform the transition
            Map<String, Object> payload = Map.of("transition", Map.of("id", transitionId));
            HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> postResponse = restTemplate.postForEntity(
                    baseUrl + "/transitions", postEntity, String.class);

            if (postResponse.getStatusCode() != HttpStatus.NO_CONTENT
                    && postResponse.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to transition ticket " + ticketId
                        + " using transition ID " + transitionId
                        + ". Status: " + postResponse.getStatusCode());
            }
        }
    }


    private Tenant getTenantOrThrow(int tenantId) {
        Optional<Tenant> opt = tenantRepository.findById(tenantId);
        return opt.orElseThrow(() ->
                new RuntimeException("Tenant not found with ID: " + tenantId));
    }

    private HttpHeaders buildAuthHeaders(String username, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authString = username + ":" + token;
        String base64Creds = Base64.getEncoder().encodeToString(authString.getBytes());
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
}
