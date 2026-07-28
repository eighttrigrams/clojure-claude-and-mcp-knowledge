---
name: rhizome-rest-api
description: Hitting a locally running Rhizome's REST API during development, with the bundled rhizome-cli.sh curl wrapper — /rest/describe, the write gate and recording mode, embedding backfill, response shapes. For querying the human's live rhizome, use the rhizome-user skill (plurama-cli) instead.
---

# Rhizome REST API (development)

This skill is for **development against a locally running Rhizome** — you
started it yourself, you know the port, and you are exercising or changing the
API. To query the human's real rhizome, use the `rhizome-user` skill, which
goes through `plurama-cli`.

How to search well — the item/context model, intersection search, kind
filters, search modes, vector search — is in `rhizome-user` and applies
unchanged here; only the transport differs.

Rhizome exposes its REST API at `http://127.0.0.1:<port>/rest/`. Dev port is
`3006`. Request/response bodies are JSON. Rhizome must be running locally
(`./dev.sh` in `rhizome/`).

Script: ${CLAUDE_SKILL_DIR}/scripts/rhizome-cli.sh

Thin wrapper around `curl` against `127.0.0.1`. The port is required. Pass the
path after `/rest`, **including the leading `/`** (the script does not duplicate
the API surface; you must know the endpoints).

Usage
```
rhizome-cli.sh <port> <path>                        # GET
rhizome-cli.sh <port> <method> <path> [json-body]   # any method, optional JSON body
```

```bash
${CLAUDE_SKILL_DIR}/scripts/rhizome-cli.sh 3006 "/contexts?q=Books"
${CLAUDE_SKILL_DIR}/scripts/rhizome-cli.sh 3006 "/items/10935/related?secondary_ids=11041&search_mode=2"
```

## Endpoint catalogue — ask the server

`GET /rest/describe` returns a self-description of every handler (name,
arglists, docstring with method, path, params, and status codes). That endpoint
is the authoritative reference — never guess an endpoint from memory.

```bash
${CLAUDE_SKILL_DIR}/scripts/rhizome-cli.sh 3006 /describe | jq
```

## Writing and the recording gate

`POST /rest/contexts`, `POST /rest/items`, and `PUT /rest/items/:id` all first
log the intended action, then either execute (when recording is on) or drop the
request with `403 {"dropped":true,"recording":false,"intent":"..."}`.

To enable recording: inside the running Rhizome app, press **Option+Shift+W**.
A red ⚠ REC badge appears in the top-left corner while the mode is active.
Toggle it off with the same shortcut. Every attempted write is logged to
`rhizome/rhizome.log` regardless of whether it was executed or dropped.

URLs (YouTube, GitHub, Substack, …) passed as `title` on `POST /rest/items` are
auto-detected and enriched by the insertion pipeline.

## Embedding backfill

`POST /rest/backfill/embeddings` embeds every item that has a description and a
NULL embedding. Idempotent and resumable — safe to re-run (or interrupt and
re-run). Gated by recording mode. The request blocks until completion and
returns `{"embedded": N}` (or `{"embedded": 0, "dry-run": true}` when recording
is off).

```bash
${CLAUDE_SKILL_DIR}/scripts/rhizome-cli.sh 3006 POST "/backfill/embeddings"
```

Run this after bulk-ingest scripts (the rhizome-ingest scripts already call it
at the end), or periodically if items were created via the UI (the UI path
bypasses the per-item embed hook). Only items with a non-empty description get
embedded — on ingestion (`POST /rest/items`, `PUT /rest/items/:id`) and here;
title-only items are intentionally skipped.

Embeddings come from local Ollama (`nomic-embed-text`, 768-dim) and are matched
against `items.embedding` via pgvector.

## Response shape

List endpoints return JSON arrays of item objects. A single item looks like:

```json
{
  "id": 34696,
  "title": "...",
  "short-title": "...",
  "is-context": false,
  "description": "...",
  "inserted-at": "...",
  "updated-at": "...",
  "contexts": { "10935": "Second World War", "11041": "Quotes" }
}
```

`with-related` returns `{"item": {...}, "related": [{...}, ...]}`.
