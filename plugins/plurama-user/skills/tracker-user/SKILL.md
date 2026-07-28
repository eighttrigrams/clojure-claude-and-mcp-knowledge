---
name: tracker-user
description: What the human's tracker holds (tasks, meets, journals, resources, YouTube/podcast/feed subscriptions, people/places/projects/goals) and how to read and write it well. Use whenever a question is about the human's tasks, schedule, saved links or subscriptions.
---

# Using tracker

Tracker is the human's personal tracker app, read and written over its HTTP
API. Every endpoint lives under `/api` — always send the full path, e.g.
`/api/today-board` or `/api/tasks?category=Acme&limit=100`. For brevity the
paths below are written without that prefix.

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
`/describe` first.

## Discover the endpoints — don't guess

Call `GET /describe` — it is the authoritative reference: every route with
its method, path, body fields, query params, views and sort modes. This skill
covers *how to use* tracker; it deliberately lists no endpoints.

If the human mentions a "view", "filter", "sort" or "tab" you don't recognise
(e.g. "saved", "archived", "today"), look it up there before answering.

## Reading well

- **Act on reads.** For a read-only question, fetch the data and answer — do
  not ask permission first ("shall I list them?"). Only ask back when the
  request is genuinely ambiguous.
- **Find the filter before saying "no".** List endpoints take query params
  (look them up in `/describe`): resources filter by
  `domain`/`excluded-domains`, tasks by scope/importance/urgency/category,
  meets by date/category. "My Google Docs" or "links from X" is a `domain`
  filter on the resources list — a domain is **not** a category and **not** a
  missing feature.
- **Query params go in the path.** `/tasks?category=Acme&limit=100`. A body is
  only for POST/PUT payloads; params put in a separate body/`query`/`params`
  field are **dropped on a GET** — the filter and limit then silently do
  nothing and you get the default capped list back.
- **Lean rows by default — so enumerate and count freely.** List reads return
  stripped rows (no `description`/`tags`) and are cheap; for machine-user
  callers the default cap is **100**, not 10. For "all / how many /
  which" questions just filter and read the full set. Only for genuinely huge
  sets pass an explicit `?limit` and state the scope you covered. Explicit
  counts ("top 5", "next 3") → pass that as `?limit`.
- **Ask for detail only when needed.** `?detail=full` on a list adds the body
  text — use it when the human wants contents, not for counting or listing
  titles. For one item, read it by id. `/today-board` is never stripped.
- **Today and the next few days.** `/today-board` is the bounded,
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

Machine-user callers are gated by tracker's **recording mode** — their writes
come back `{"dropped":true}` with nothing written until the human turns
recording on. That toggle is human-only; never flip it.

## Common request patterns

### Answering "what's on my today board?"

`GET /today-board` returns the aggregate `{tasks, meets,
journal-entries}`. Tasks use the same `:today` filter as the UI's Today
list (incomplete + due-date OR urgent/superurgent OR `today=1` OR
`lined_up_for` set OR active reminder). Meets are those with `start_date =
today` and not archived. Journal entries are today's entries.

### Adding mail to the inbox from a job

`POST /messages` bypasses the gate; requires `has_mail` on the (target)
user.

### Creating a task that should appear on Today

Two writes — create the task, then flip its `today` flag — and **both** hit
the gate, so recording mode must be ON for a machine user to land them.
Look up the exact endpoints + bodies in `/describe`.
