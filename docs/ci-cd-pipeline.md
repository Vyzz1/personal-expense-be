# CI/CD Pipeline

## Overview

The pipeline is defined in `.github/workflows/backend-ci.yml` and runs on **GitHub Actions**. It is triggered on every pull request to `main` and every direct push to `main`.

The pipeline is a **sequential gate**: each stage must pass before the next one starts. A PR cannot be merged unless all stages succeed.

```
┌──────────────┐     ┌──────────────┐
│     test     │     │   security   │  ← run in parallel
└──────┬───────┘     └──────┬───────┘
       │                    │
       └──────────┬─────────┘
                  ▼
          ┌──────────────┐
          │    sonar     │  ← needs test + security
          └──────┬───────┘
                 ▼
          ┌──────────────┐
          │    docker    │  ← needs test + security + sonar
          └──────┬───────┘
                 ▼
          ┌──────────────┐
          │  automerge   │  ← PR only; needs all above
          └──────────────┘
```

---

## Triggers

| Event | Branches | Jobs that run |
|---|---|---|
| `pull_request` | `main` | All 5 jobs (test → security → sonar → docker → automerge) |
| `push` | `main` | All jobs except `automerge` (Docker image pushed to GHCR) |

---

## Jobs

### 1. `test` — Lint & Unit Tests

Runs on every trigger. No dependencies.

**Steps:**
1. Checkout code
2. Set up JDK 21 (Temurin distribution, Maven cache enabled)
3. **Checkstyle** — enforces code style rules defined in `check-style.xml`
   ```
   mvn -B checkstyle:check -Dcheckstyle.consoleOutput=true
   ```
4. **Unit Tests + Coverage** — compiles, runs tests, generates JaCoCo XML report
   ```
   mvn -B clean verify
   ```
5. **Upload Artifacts** — stores `target/classes` and `target/site/jacoco` for 1 day so the `sonar` job can reuse them without rebuilding

**Fails if:** any Checkstyle violation or test failure.

---

### 2. `security` — Security Scan

Runs in **parallel** with `test`. No dependencies.

**Steps:**
1. Checkout with full git history (`fetch-depth: 0`)
2. **TruffleHog** — scans the diff between base and head SHA for leaked secrets (API keys, tokens, credentials)
   - Only reports `verified` or `unknown` findings to reduce false positives
3. **Trivy filesystem scan** — scans project files for known CVEs at `CRITICAL` and `HIGH` severity
   - `exit-code: 0` — findings do not fail the build (reported only)
   - Results exported as SARIF
4. **Upload SARIF** to GitHub Security tab via `github/codeql-action/upload-sarif`

**Fails if:** TruffleHog detects a verified secret leak.

---

### 3. `sonar` — SonarCloud Analysis

Runs after both `test` and `security` pass.

**Steps:**
1. Checkout with full git history
2. Download the compiled artifacts uploaded by the `test` job
3. **SonarCloud analysis** — sends bytecode + JaCoCo coverage report to SonarCloud
   ```
   mvn -B sonar:sonar \
     -Dsonar.host.url=https://sonarcloud.io \
     -Dsonar.projectKey=${{ secrets.SONAR_PROJECT_KEY }} \
     -Dsonar.organization=${{ secrets.SONAR_ORG }} \
     -Dsonar.java.binaries=target/classes \
     -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
   ```

**Required secrets:** `SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_ORG`

**Fails if:** SonarCloud Quality Gate is configured to block (depends on SonarCloud project settings).

---

### 4. `docker` — Docker Build & Push

Runs after `test`, `security`, and `sonar` all pass.

**Steps:**
1. Checkout code
2. Set up Docker Buildx (multi-platform builder)
3. **Log in to GHCR** — only on `push` events (not PRs)
   ```
   registry: ghcr.io
   username: ${{ github.actor }}
   password: ${{ secrets.GITHUB_TOKEN }}
   ```
4. **Generate image tags** via `docker/metadata-action`:
   - `ghcr.io/<owner>/<repo>:<git-sha>` — always
   - `ghcr.io/<owner>/<repo>:latest` — only on the default branch (`main`)
5. **Build (and push on `push` events)**:
   ```
   docker build -f ./Dockerfile --push ...
   ```
   - Uses GitHub Actions cache (`type=gha`) to speed up layer reuse
   - On PRs: image is built but **not pushed** (validates the Dockerfile without publishing)

**Fails if:** Docker build fails.

---

### 5. `automerge` — Auto Merge PR

Only runs on `pull_request` events. Needs all 4 previous jobs to pass.

**Step:**
```bash
gh pr merge ${{ github.event.pull_request.number }} \
  --auto --squash \
  --repo ${{ github.repository }}
```

- Uses `--auto`: sets GitHub's native auto-merge, which merges once all required status checks pass and any required reviews are approved
- Uses `--squash`: squash-merges all commits on the PR into a single commit on `main`

**Required secret:** `PAT_TOKEN` — a Personal Access Token with `repo` scope (needed because `GITHUB_TOKEN` cannot trigger downstream workflows when used to merge)

---

## Dockerfile — Multi-Stage Build

The build uses a 4-stage Dockerfile optimized for image size and security:

```
Stage 1: build (eclipse-temurin:21-jdk-alpine)
  ├─ Download Maven dependencies offline
  ├─ Copy source
  └─ mvn clean package -DskipTests → fat JAR

Stage 2: builder (extends build)
  └─ java -Djarmode=layertools extract
       → splits JAR into: dependencies/, spring-boot-loader/,
         snapshot-dependencies/, application/

Stage 3: jre-builder (eclipse-temurin:21-jdk-alpine)
  └─ jlink → custom minimal JRE with only required modules
       (strips debug info, man pages, headers; uses zip-2 compression)

Stage 4: runtime (alpine:3.19)
  ├─ Copies custom JRE from stage 3
  ├─ Creates non-root user "spring"
  ├─ Copies layered application from stage 2
  └─ ENTRYPOINT: JarLauncher
```

**Why layered extraction?** Docker layer caching means that if only `application/` changes (your code), the `dependencies/` layer (third-party JARs) is reused from cache — significantly faster image rebuilds.

**Why a custom JRE?** `jlink` produces a runtime with only the Java modules the application needs, reducing the final image size substantially compared to a full JDK image.

---

## Required GitHub Secrets

| Secret | Used by | Description |
|---|---|---|
| `SONAR_TOKEN` | `sonar` | SonarCloud authentication token |
| `SONAR_PROJECT_KEY` | `sonar` | SonarCloud project identifier |
| `SONAR_ORG` | `sonar` | SonarCloud organization name |
| `PAT_TOKEN` | `automerge` | Personal Access Token with `repo` scope for auto-merge |
| `GITHUB_TOKEN` | `docker`, `security` | Automatically provided by GitHub Actions |

---

## Pipeline Permissions

Each job declares only the permissions it needs (least-privilege):

| Job | `contents` | `security-events` | `packages` | `pull-requests` |
|---|---|---|---|---|
| `test` | read | — | — | — |
| `security` | read | write | — | — |
| `sonar` | read | — | — | — |
| `docker` | read | — | write | — |
| `automerge` | write | — | — | write |
