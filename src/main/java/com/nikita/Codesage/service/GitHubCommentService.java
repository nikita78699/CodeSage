package com.nikita.Codesage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikita.Codesage.util.GitHubAppTokenUtil;
import com.nikita.Codesage.util.PemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;

@Service
public class GitHubCommentService {

    @Value("${github.app.id}")
    private String githubAppId;

    @Value("${github.pem.path}")
    private String pemPath;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GITHUB_API_BASE_URL = "https://api.github.com";

    public void postComment(String owner, String repo, int prNumber, String commentBody) {
        try {
            // Step 1: Load private key and generate JWT
            RSAPrivateKey privateKey = PemUtils.readPrivateKeyFromPemFile(pemPath);
            String jwt = GitHubAppTokenUtil.generateJWT(githubAppId, privateKey);

            // Step 2: Get installation ID
            HttpHeaders jwtHeaders = new HttpHeaders();
            jwtHeaders.setBearerAuth(jwt);
            jwtHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> jwtEntity = new HttpEntity<>(jwtHeaders);

            ResponseEntity<List> installResponse = restTemplate.exchange(
                    GITHUB_API_BASE_URL + "/app/installations",
                    HttpMethod.GET,
                    jwtEntity,
                    List.class
            );
            List<Map<String, Object>> installations = installResponse.getBody();
            if (installations == null || installations.isEmpty()) {
                throw new RuntimeException("No GitHub App installations found.");
            }
            int installationId = (int) installations.get(0).get("id");

            // Step 3: Get installation access token
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    GITHUB_API_BASE_URL + "/app/installations/" + installationId + "/access_tokens",
                    HttpMethod.POST,
                    jwtEntity,
                    Map.class
            );
            String installationToken = (String) tokenResponse.getBody().get("token");

            // Step 4: Post comment to the PR
            String commentUrl = String.format("%s/repos/%s/%s/issues/%d/comments",
                    GITHUB_API_BASE_URL, owner, repo, prNumber);

            HttpHeaders commentHeaders = new HttpHeaders();
            commentHeaders.setBearerAuth(installationToken);
            commentHeaders.setContentType(MediaType.APPLICATION_JSON);
            commentHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

            // ✅ Proper JSON encoding using ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Map.of("body", commentBody));

            HttpEntity<String> commentEntity = new HttpEntity<>(json, commentHeaders);

            ResponseEntity<String> commentResponse = restTemplate.postForEntity(
                    commentUrl, commentEntity, String.class);

            System.out.println("✅ GitHub comment response: " + commentResponse.getStatusCode());
            System.out.println("✅ GitHub comment response body: " + commentResponse.getBody());

        } catch (Exception e) {
            System.out.println("❌ Error while posting comment to GitHub:");
            e.printStackTrace();
        }
    }
}
