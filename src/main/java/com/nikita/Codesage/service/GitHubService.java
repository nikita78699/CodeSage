package com.nikita.Codesage.service;

import com.nikita.Codesage.util.PemUtils;
import com.nikita.Codesage.util.GitHubAppTokenUtil;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final String APP_ID = "1679318"; // GitHub App ID

    public List<Map<String, Object>> getPullRequestFiles(String owner, String repo, String prNumber) {
        try {
            // Step 1: Load private key from classpath
            InputStream keyStream = getClass().getClassLoader().getResourceAsStream("util/private-key.pem");
            RSAPrivateKey privateKey = PemUtils.readPrivateKeyFromPemFile(keyStream.toString());
            String jwt = GitHubAppTokenUtil.generateJWT(APP_ID, privateKey);

            // Step 2: Get installation ID
            HttpHeaders jwtHeaders = new HttpHeaders();
            jwtHeaders.setBearerAuth(jwt);
            jwtHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> jwtEntity = new HttpEntity<>(jwtHeaders);

            String installationsUrl = GITHUB_API_BASE_URL + "/app/installations";
            ResponseEntity<List> installResponse = restTemplate.exchange(
                    installationsUrl, HttpMethod.GET, jwtEntity, List.class
            );

            List<Map<String, Object>> installations = installResponse.getBody();
            if (installations == null || installations.isEmpty()) {
                throw new RuntimeException("No GitHub App installations found.");
            }

            int installationId = (int) installations.get(0).get("id");

            // Step 3: Exchange JWT for installation access token
            String tokenUrl = GITHUB_API_BASE_URL + "/app/installations/" + installationId + "/access_tokens";
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, jwtEntity, Map.class
            );

            String installationToken = (String) tokenResponse.getBody().get("token");

            // Step 4: Use installation token to get PR files
            String filesUrl = GITHUB_API_BASE_URL + "/repos/" + owner + "/" + repo + "/pulls/" + prNumber + "/files";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(installationToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(filesUrl, HttpMethod.GET, requestEntity, List.class);
            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch PR files from GitHub.");
        }
    }
}
