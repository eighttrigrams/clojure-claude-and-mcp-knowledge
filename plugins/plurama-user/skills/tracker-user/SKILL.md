---
name: tracker-user
description: What the human's tracker holds (tasks, meets, journals, resources, YouTube/podcast/feed subscriptions, people/places/projects/goals) and how to read and write it well. Use whenever a question is about the human's tasks, schedule, saved links or subscriptions.
---

# Using tracker

Tracker is the human's personal tracker app. Talk to it over its HTTP API —
from the shell that means `plurama-cli` (see the `plurama-cli` skill):

```bash
plurama-cli tracker /api/today-board
plurama-cli tracker '/api/tasks?category=Acme&limit=100'
```

## What tracker covers (scope)

Tracker is not only tasks. A single user's tracker holds **tasks, meetings
("meets") and meeting series, recurring tasks, journals and journal entries,
saved resources (links and videos), mottos**, and **"sources"** — the user's
**YouTube channel subscriptions, podcast feeds, and Atom/RSS feeds** plus
their per-source settings. People / places / projects / goals are the
categories that tie items together through relations.

So questions about the user's **YouTube subscriptions, saved YouTube videos,
podcasts, or RSS feeds are in scope** and are answered from tracker — do
**not** treat them as an external account you cannot reach. Never refuse on
the assumption that tracker does not track something; check
`/api/describe` first.

## Discover the endpoints — don't guess

`GET /api/describe` is the authoritative reference: every route with its
method, path, body fields, query params, views and sort modes. This skill
covers *how to use* tracker; it deliberately lists no endpoints.

```bash
plurama-cli tracker /api/describe | jq '.[] | {method, path}'
```

If the human mentions a "view", "filter", "sort" or "tab" you don't recognise
(e.g. "saved", "archived", "today"), look it up there before answering.

## Reading well

- **Act on reads.** For a read-only question, fetch the data and answer — do
  not ask permission first ("shall I list them?"). Only ask back when the
  request is genuinely ambiguous.
- **Find the filter before saying "no".** List endpoints take query params
  (look them up in `/api/describe`): resources filter by
  `domain`/`excluded-domains`, tasks by scope/importance/urgency/category,
  meets by date/category. "My Google Docs" or "links from X" is a `domain`
  filter on the resources list — a domain is **not** a category and **not** a
  missing feature.
- **Query params go in the path.** `'/api/tasks?category=Acme&limit=100'`,
  quoted so the shell keeps `?` and `&`. A body is only for POST/PUT payloads.
- **Lean rows by default — so enumerate and count freely.** List reads return
  stripped rows (no `description`/`tags`) and are cheap. For "all / how many /
  which" questions just filter and read the full set. Only for genuinely huge
  sets pass an explicit `?limit` and state the scope you covered. Explicit
  counts ("top 5", "next 3") → pass that as `?limit`.
- **Ask for detail only when needed.** `?detail=full` on a list adds the body
  text — use it when the human wants contents, not for counting or listing
  titles. For one item, read it by id.
- **Today and the next few days.** `/api/today-board` is the bounded,
  full-detail view of today; `?days=N` widens the meeting window to
  today..today+N. Reach for it on "what's on today / coming up" instead of
  scanning all tasks. For broad reads, prefer the specific resource list over
  the today board, which only covers today.
- **Aggregate across types** when the question spans them ("everything on
  Monday" = tasks **and** meets), and say which sources you checked. Never
  call a single filtered list "all" without confirming it covers the question.

## Writing

Writes hit the human's real data immediately. Confirm before any mutating
call unless the human clearly asked for it.

Two shape details worth knowing: task `done` and `today` are integers
(`0`/`1`), not booleans; and YouTube / Substack URLs posted as resources
auto-fetch their title server-side.

Some callers (the Telegram bot's machine user, not `plurama-cli`) are gated by
tracker's **recording mode** — their writes come back `{"dropped":true}` with
nothing written until the human turns recording on. That toggle is
human-only; never flip it.
