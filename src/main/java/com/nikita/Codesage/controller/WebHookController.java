package com.nikita.Codesage.controller;

import com.nikita.Codesage.service.GitHubCommentService;
import com.nikita.Codesage.service.GitHubService;
import com.nikita.Codesage.service.OpenAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebHookController {

    private final GitHubService gitHubService;
    private final OpenAIService openAIService;
    private final GitHubCommentService gitHubCommentService;

    public WebHookController(
            GitHubService gitHubService,
            OpenAIService openAIService,
            GitHubCommentService gitHubCommentService) {
        this.gitHubService = gitHubService;
        this.openAIService = openAIService;
        this.gitHubCommentService = gitHubCommentService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Received webhook: " + payload);

        String action = (String) payload.get("action");
        Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");

        if (pullRequest != null && repository != null) {
            if (!"opened".equals(action) && !"synchronize".equals(action)) {
                return ResponseEntity.ok("No action taken for action: " + action);
            }

            String prNumber = String.valueOf(pullRequest.get("number"));
            String repoName = (String) repository.get("name");
            String repoOwner = ((Map<String, Object>) repository.get("owner")).get("login").toString();

            System.out.println("Action: " + action);
            System.out.println("PR Number: " + prNumber);
            System.out.println("Repository: " + repoOwner + "/" + repoName);

            // Fetch PR files
            List<Map<String, Object>> files = gitHubService.getPullRequestFiles(repoOwner, repoName, prNumber);

            // Build code diff string
            StringBuilder codeChanges = new StringBuilder();
            for (Map<String, Object> file : files) {
                codeChanges.append("File: ").append(file.get("filename")).append("\n");
                codeChanges.append("Patch:\n").append(file.get("patch")).append("\n\n");
            }

            // Run AI analysis
            String review = openAIService.analyzeCodeChanges(codeChanges.toString());

            // Post GitHub comment
            try {
                gitHubCommentService.postComment(repoOwner, repoName, Integer.parseInt(prNumber), review);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Failed to post comment to GitHub");
            }

            return ResponseEntity.ok("Review posted successfully:\n\n" + review);
        }

        return ResponseEntity.badRequest().body("Invalid payload.");
    }

}
