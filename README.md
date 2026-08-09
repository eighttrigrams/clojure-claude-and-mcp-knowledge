# Claude Stuff

Things I know and find important to document about Claude (Code).

Some of it perhaps interesting to a wider audience, some of it clearly
subject to my own preferences (see below).

<details>

- Clojure as a primary language I am focusing on
- VSCode as my primary targeted IDE
- GitHub, Docker on the tooling side
- MacOS as my OS, Linux inside containers and remote

See also 
- [eighttrigrams/preferences](https://github.com/eighttrigrams/preferences)

</details>

## Topics

- [Claude Code Configuration Files](./topics/claude-code-configuration.md)
- [Claude and VSCode](./topics/claude-and-vscode.md)
- [Claude Plugins](./topics/claude-plugins.md)
- [Claude Marketplaces](./topics/claude-marketplaces.md)
- [clojure-claude-and-mcp-knowledge](./clojure-claude-and-mcp-knowledge/README.md).

## Recipes

- [How to Install Claude Code](./recipes/how-to-install-claude-code.md)
- [Working with MCP](./recipes/working-with-mcp.md)
- [Docker Claude Container](./recipes/docker-claude-container.md)
- [Writing a Hook Plugin That Rewrites a Tool Call](./recipes/writing-a-hook-plugin.md)

## Issues

- [Claude Shows Claude.ai Connectors in Claude Code](./issues/claude-shows-claude-ai-connectors-in-claude-code.md)

## Eighttrigrams marketplace

See [claude-marketplaces](./topics/claude-marketplaces.md) for background.

### Plugins

#### clj-nrepl-eval

Evaluating Clojure code via nREPL.

- [clj-nrepl-eval](plugins/clj-nrepl-eval/skills/clj-nrepl-eval/SKILL.md) — nREPL [SKILL](https://github.com/bhauman/clojure-mcp-light/blob/main/skills/clojure-eval/SKILL.md) copied wholesale from [bhauman/clojure-mcp-light](https://github.com/bhauman/clojure-mcp-light); install `clj-nrepl-eval` via `bbin`

#### architecture

Guidelines for structuring code.

- [Architecture review](plugins/architecture/skills/architecture-review/SKILL.md)
- [Writing tests](plugins/architecture/skills/writing-tests/SKILL.md)

#### log-tool-calls

A `PreToolUse` hook that logs every tool invocation to `logs/hooks.log`.

#### git-identity

A `PreToolUse` hook that stamps `Claude <claude@eighttrigrams.net>` onto any Bash
call that commits, so agent commits are never signed as mine.

- Rewrites the command to `export` the four `GIT_AUTHOR_*`/`GIT_COMMITTER_*` variables on a line of their own, ahead of the original — a `VAR=x cmd` prefix would reach only the first command of a `git add . && git commit`
- Returns no `permissionDecision`, so it rewrites the command without granting it — `"allow"` would wave the whole line through, and `"defer"` hands the call back to the caller and discards the rewrite
