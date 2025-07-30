package com.nikita.Codesage.service;

import com.nikita.Codesage.util.GitHubAppTokenUtil;
import com.nikita.Codesage.util.PemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;

@Service
public class GitHubCommentService {

    @Value("${github.app.id}")
    private String githubAppId;

    @Value("${github.pem.path}")
    private String pemPath; // Absolute path from application.properties

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GITHUB_API_BASE_URL = "https://api.github.com";

    public void postComment(String owner, String repo, int prNumber, String commentBody) {
        try {
            // ✅ Load private key directly from filesystem path
            RSAPrivateKey privateKey = PemUtils.readPrivateKeyFromPemFile(pemPath);
            String jwt = GitHubAppTokenUtil.generateJWT(githubAppId, privateKey);

            // ✅ Step 2: Get installation ID
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

            // ✅ Step 3: Get installation access token
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    GITHUB_API_BASE_URL + "/app/installations/" + installationId + "/access_tokens",
                    HttpMethod.POST,
                    jwtEntity,
                    Map.class
            );
            String installationToken = (String) tokenResponse.getBody().get("token");

            // ✅ Step 4: Post the comment
            String commentUrl = String.format("%s/repos/%s/%s/issues/%d/comments",
                    GITHUB_API_BASE_URL, owner, repo, prNumber);

            HttpHeaders commentHeaders = new HttpHeaders();
            commentHeaders.setBearerAuth(installationToken);
            commentHeaders.setContentType(MediaType.APPLICATION_JSON);
            commentHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

            String json = String.format("{\"body\": \"%s\"}", commentBody.replace("\"", "\\\""));
            HttpEntity<String> commentEntity = new HttpEntity<>(json, commentHeaders);

            ResponseEntity<String> commentResponse = restTemplate.postForEntity(
                    commentUrl, commentEntity, String.class);

            System.out.println("✅ GitHub comment response: " + commentResponse.getStatusCode());
            System.out.println("✅ GitHub comment body: " + commentResponse.getBody());

        } catch (Exception e) {
            System.out.println("❌ Error while posting comment to GitHub:");
            e.printStackTrace();
        }
    }
}
