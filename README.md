# 🤖 CodeSage: An AI-Powered Code Review Bot for GitHub Pull Requests

CodeSage is an intelligent code review assistant that automatically reviews GitHub pull requests using LLMs via the Groq API and posts insightful suggestions directly on the PR.

Built with **Java + Spring Boot**, it uses a **GitHub App** to receive PR events and a **Groq LLM backend** to analyze code changes.

---

## ✨ Features

- 🧠 AI-generated code review comments on PRs
- 🔐 Secure GitHub App authentication via JWT
- 🚀 Groq-powered LLM integration
- ⚙️ PR diff analysis (`patch`) support
- 💬 Posts review comments directly on GitHub
- 📦 Clean architecture with Spring Boot

---

## 🧑‍💻 Demo Flow

1. A pull request is opened or updated (`synchronize`).
2. CodeSage receives the webhook.
3. It fetches the PR diff and sends it to Groq.
4. Groq analyzes and returns a detailed review.
5. CodeSage posts the review as a comment on the PR.

---

## 🛠️ Prerequisites

- Java 17+
- Maven
- GitHub Account
- Groq API Key
- ngrok (optional, for tunneling)

---

## 🔧 Setup Instructions

### 1. 🏗️ Clone the Repository

```bash
  git clone https://github.com/nikita78699/CodeSage.git
  cd CodeSage

```

### 2. 🔐 Set Up Your GitHub App

You’ll need a GitHub App to securely fetch PR info and post comments.

#### 🏗️ Create a GitHub App

Go to [GitHub Developer Settings → Apps](https://github.com/settings/apps/new)

#### ✅ Set the following:

**Permissions**
- **Pull requests**: _Read & write_
- **Contents**: _Read-only_
- **Metadata**: _Read-only_

**Subscribe to Events**
- `pull_request`

**Webhook URL**
- `http://localhost:8080/webhook/github` (for local development)

After creating the GitHub App:

1. **Download the private key `.pem` file**
2. Place it at the following location in your project:

  - src/main/resources/util/private-key.pem

### 3. 📦 Configure `application.properties`

Update the following in `src/main/resources/application.properties`:

```properties
# GitHub App Configuration
github.app.id=YOUR_APP_ID
github.pem.path=ABSOLUTE_PATH_TO_YOUR/private-key.pem

# Groq API Configuration
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.api.key=${GROQ_API_KEY}

# Logging
logging.level.org.springframework.web.client.RestTemplate=DEBUG
logging.level.org.apache.http=DEBUG
```
✅ Replace YOUR_APP_ID with your GitHub App ID \
✅ Replace the PEM path with the absolute path \
✅ Export your Groq key:

```export GROQ_API_KEY=your_groq_key_here```

### 4. 🧪 Run the Application
   ```bash
     ./mvnw spring-boot:run 
 ```
   Or from your IDE (e.g., IntelliJ).

### 5. 🌐 Expose Webhook (Optional, for local)
  ```` bash
     ngrok http 8080
  ````
   Use the generated HTTPS URL as your webhook URL in GitHub App settings.

#### Project Structure
``` bash
 src
├── config/              # Spring Boot configs (RestTemplate, OpenAI, GitHub)
├── controller/          # Webhook controller
├── service/             # GitHub/Groq logic
├── util/                # JWT and PEM utilities
└── resources/
├── application.properties
└── util/private-key.pem
```

## 🧑‍🎓 Author

**👩‍💻 Nikita Singhal**  
💼 Associate Software Engineer  
🔗 [GitHub](https://github.com/nikita78699)  
📫 [LinkedIn](https://www.linkedin.com/in/nikita-singhal-77b6801b5/)


