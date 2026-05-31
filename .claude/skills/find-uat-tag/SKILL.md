---
name: find-uat-tag
description: Find the docker image tag from the last successful UAT build in GitHub Actions. Use this skill when the user wants to know the latest UAT docker tag, or before triggering a manual production deployment.
---

Find the docker image tag produced by the last successful "Build and deploy to UAT" GitHub Actions workflow run.

## Steps

1. Find the last successful run of `build-test-uat.yml`:
```bash
gh run list --workflow=build-test-uat.yml --status=success --limit=1 --json databaseId,displayTitle,createdAt,headSha
```

2. Get the `docker-build` job ID from that run (replace RUN_ID):
```bash
gh api repos/:owner/:repo/actions/runs/RUN_ID/jobs --jq '.jobs[] | select(.name == "docker-build") | {id: .id, name: .name}'
```

3. Extract the docker tag from that job's logs (replace JOB_ID):
```bash
gh api repos/:owner/:repo/actions/jobs/JOB_ID/logs 2>&1 | grep "DOCKER_IMAGE_TAG:" | head -1
```

## Output

Report to the user:
- The docker tag (e.g. `2.1.1-197`)
- The commit it was built from (title + short SHA)
- The build timestamp

The tag can be passed directly as `docker_tag_version` to the `manual-deploy.yml` workflow when deploying to prod.
