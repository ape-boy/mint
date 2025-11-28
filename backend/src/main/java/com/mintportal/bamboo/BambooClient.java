package com.mintportal.bamboo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BambooClient {

    private final BambooConfig config;
    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            WebClient.Builder builder = WebClient.builder()
                    .baseUrl(config.getBaseUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            // Add authentication
            if (config.getApiToken() != null && !config.getApiToken().isEmpty()) {
                builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiToken());
            } else if (config.getUsername() != null && config.getPassword() != null) {
                String auth = config.getUsername() + ":" + config.getPassword();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
            }

            webClient = builder.build();
        }
        return webClient;
    }

    /**
     * Trigger a build plan in Bamboo
     * @param planKey The Bamboo plan key (e.g., "PROJ-PLAN")
     * @param variables Optional build variables
     * @return Build result key (e.g., "PROJ-PLAN-123")
     */
    public Mono<BambooBuildResult> triggerBuild(String planKey, Map<String, String> variables) {
        log.info("Triggering Bamboo build for plan: {}", planKey);

        StringBuilder url = new StringBuilder("/rest/api/latest/queue/")
                .append(planKey);

        if (variables != null && !variables.isEmpty()) {
            url.append("?");
            variables.forEach((key, value) ->
                    url.append("bamboo.variable.").append(key).append("=").append(value).append("&"));
        }

        return getWebClient()
                .post()
                .uri(url.toString())
                .retrieve()
                .bodyToMono(BambooBuildResult.class)
                .doOnSuccess(result -> log.info("Build triggered successfully: {}", result.getBuildResultKey()))
                .doOnError(error -> log.error("Failed to trigger build: {}", error.getMessage()));
    }

    /**
     * Get build status from Bamboo
     * @param buildResultKey The build result key (e.g., "PROJ-PLAN-123")
     * @return Build status information
     */
    public Mono<BambooBuildStatus> getBuildStatus(String buildResultKey) {
        log.debug("Fetching build status for: {}", buildResultKey);

        return getWebClient()
                .get()
                .uri("/rest/api/latest/result/{buildResultKey}", buildResultKey)
                .retrieve()
                .bodyToMono(BambooBuildStatus.class)
                .doOnError(error -> log.error("Failed to get build status: {}", error.getMessage()));
    }

    /**
     * Get build logs from Bamboo
     * @param buildResultKey The build result key
     * @param jobKey The specific job key
     * @return Build logs
     */
    public Mono<String> getBuildLogs(String buildResultKey, String jobKey) {
        return getWebClient()
                .get()
                .uri("/download/{buildResultKey}/build_logs/{jobKey}.log",
                        buildResultKey, jobKey)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Get available plans from Bamboo
     * @return List of available plans
     */
    public Mono<BambooPlanList> getPlans() {
        return getWebClient()
                .get()
                .uri("/rest/api/latest/plan")
                .retrieve()
                .bodyToMono(BambooPlanList.class);
    }

    /**
     * Get specific plan details
     * @param planKey The plan key
     * @return Plan details
     */
    public Mono<BambooPlanDetails> getPlanDetails(String planKey) {
        return getWebClient()
                .get()
                .uri("/rest/api/latest/plan/{planKey}", planKey)
                .retrieve()
                .bodyToMono(BambooPlanDetails.class);
    }

    /**
     * Stop a running build
     * @param buildResultKey The build result key
     */
    public Mono<Void> stopBuild(String buildResultKey) {
        log.info("Stopping build: {}", buildResultKey);

        return getWebClient()
                .delete()
                .uri("/rest/api/latest/queue/{buildResultKey}", buildResultKey)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Build stopped successfully: {}", buildResultKey))
                .doOnError(error -> log.error("Failed to stop build: {}", error.getMessage()));
    }
}
