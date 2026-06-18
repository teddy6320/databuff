# Deploy Common Assets

This directory contains deployment assets shared by Docker and Kubernetes packages.

| Directory | Purpose |
|-----------|---------|
| `sql/` | Doris initialization SQL |
| `skills/` | Built-in AgentScope skill packages (`apm.agent.builtin-skills-dir`) |

Docker/local runtime reads SQL from `deploy/common/sql/` (or packaged `sql/`); skills are copied into the web image at build time from `common/skills/`.

Kubernetes packaging should include the same common assets and mount or apply them according to the target deployment flow.
