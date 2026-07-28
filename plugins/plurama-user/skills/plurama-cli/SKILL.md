---
name: plurama-cli
description: How to talk to the deployed plurama apps (treina, tracker, personalist, rhizome …) from the shell with the plurama-cli command — a curl-like client.
---

# plurama-cli — talking to the deployed plurama apps

`plurama-cli` is a babashka script that calls the HTTP APIs of
the live plurama apps. It is the way to read and write live app data from a shell.

```bash
plurama-cli treina /api/describe
plurama-cli treina /api/trainings/
plurama-cli treina /api/trainings/ -X POST -d '{"name":"Squat"}'
```

Argument one is the app, argument two is the request path including its query
string. Everything else mirrors curl.

Source: <https://github.com/eighttrigrams/plurama-cli> (public).

## Flags

| flag | meaning |
|------|---------|
| `-X, --method METHOD` | HTTP method. Defaults to `GET`, or `POST` when a body is given. |
| `-d, --body BODY` | Request body; `@file` reads from a file. Sets `Content-Type: application/json`. |
| `-H, --header "K: V"` | Extra header. Repeatable. |
| `-i, --include` | Print status line and response headers. |
| `--raw` | Do not pretty-print JSON responses. |

`plurama-cli apps` lists the configured apps and their base URLs. Exit codes:
`0` for 2xx, `1` for any other HTTP status, `2` for a local error (unknown app,
failed login, no credentials).

Quote paths that contain `?` or `&` so the shell keeps them intact:
`plurama-cli treina '/api/trainings/?limit=10'`.

## Discover the API before guessing

Every plurama app serves **`GET /api/describe`** — a self-description of each
route, with `:method`, `:path` and the handler's docstring. That endpoint is the
authoritative reference for paths, query params, body fields and error cases.
Read it first; never invent an endpoint from memory.

```bash
plurama-cli treina /api/describe | jq '.[] | {method, path}'
```

## Authentication, and what that implies

The CLI logs in as the app's **admin** user via `POST /api/auth/login`, caches
the JWT at `~/.cache/plurama-cli/<app>.token` (mode 600), and re-authenticates
automatically on a `401`. Nothing needs to be configured per shell.

Two consequences worth holding in mind:

- **Writes are real and immediate.** An admin token is not a machine token, so
  the recording-mode gate that drops machine writes does **not** apply. A `POST`
  or `DELETE` hits production data straight away. Confirm with the human before
  any mutating call unless they clearly asked for it.
- **The token is a full admin credential.** Don't paste it into logs or files.

## Credentials are baked in — never read them back

The installed script at `$(bbin bin)/plurama-cli` (mode 700) carries the prod
passwords **inside it**, base64-encoded, substituted at install time. That is
deliberate: the credentials come out of the private `secrets.yaml` and are never
printed.

**Do not `cat`, `grep`, or otherwise dump the installed script, and do not
decode the `baked-credentials` string.** Use the CLI; don't inspect it. The same
goes for the cached token files.

## Installing / updating (human-run)

Installation lives in the **private** `plurama.eighttrigrams` workspace, not in
the public CLI repo:

```bash
./deploy-plurama-cli
```

It installs from the local `plurama-cli/` checkout — pull that repo first if you
want the latest code. The script takes each app's password out of `secrets.yaml`
with sops, encodes the credential map, replaces the `__BAKED_CREDENTIALS__`
marker in the script, and installs it with `bbin`. Secrets stay in shell
variables, so the script's output is safe to show. Requires `bb`, `bbin`,
`sops`, `jq`, and the age key that decrypts `secrets.yaml`.

Re-run it after rotating a password, adding an app, or changing the CLI.

## Adding another app

Two edits, both one-liners:

1. `deploy-plurama-cli` in the private workspace — add a row to the `APPS`
   array: `"<app>|<base-url>|<username>|<secrets-app>|<secrets-key>"`, where the
   last two name the path in `secrets.yaml`.
2. Re-run `./deploy-plurama-cli`.

The CLI itself needs no change; it is driven entirely by the baked credential
map. Apps must expose the standard plurama auth surface (`POST /api/auth/login`
returning `{:token …}`).

## Troubleshooting

- **`plurama-cli: unknown app: X`** — X is not in the baked map. See "Adding
  another app"; the error lists what is configured.
- **`command not found`** — not installed on this machine, or `~/.local/bin` is
  not on `PATH`. Ask the human to run `./deploy-plurama-cli`; do not try to
  reconstruct credentials yourself.
- **`login … failed: HTTP 401`** — the baked password is stale. The human should
  re-run the deploy script after fixing `secrets.yaml`.
- **Falls back to `~/.config/plurama-cli/credentials.edn`** when the marker is
  still in place (e.g. running `bb plurama_cli.clj` from a checkout). That file
  is a plaintext password store; treat it like the baked script.
