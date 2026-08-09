#!/usr/bin/env bash
# PreToolUse: stamp the agent's git identity onto any Bash call that commits.
#
# Rewrites the command so it is preceded by an `export` of the four git identity
# variables. An `export` on its own line (rather than a `VAR=x cmd` prefix) is
# what makes this work for compound commands: `git add . && git commit -m x`
# would otherwise apply the variables to `git add` only.
set -uo pipefail

NAME='Claude'
EMAIL='claude@eighttrigrams.net'

# `git [global flags] commit` — the flag alternatives cover `-c k=v`, `-C dir`
# and `--no-pager`, so an explicit identity or repo path still gets stamped.
# The trailing class rejects `commit-tree` and friends.
COMMITS='(^|[^[:alnum:]_./-])git([[:space:]]+(-[cC][[:space:]]*[^[:space:]]+|--[^[:space:]]+))*[[:space:]]+commit([^[:alnum:]_-]|$)'

jq -c \
  --arg name "$NAME" \
  --arg email "$EMAIL" \
  --arg commits "$COMMITS" \
  '
  (.tool_input.command // "") as $cmd
  | if .tool_name != "Bash"
       or ($cmd | test("GIT_AUTHOR_NAME"))   # already stamped, or hand-set
       or ($cmd | test($commits) | not)
    then
      {}
    else
      { hookSpecificOutput: {
          hookEventName: "PreToolUse",
          # No permissionDecision at all, so the permission gate stays exactly
          # where it was. "allow" would wave the whole command through, and
          # "defer" hands the call back to the caller and drops the rewrite.
          # @sh does the shell quoting of the values for us — and keeps quote
          # characters out of this filter, which bash single-quotes. Mind the
          # same trap in these comments: no apostrophes.
          updatedInput: (.tool_input + { command: (
              "export GIT_AUTHOR_NAME=" + ($name | @sh)
            + " GIT_AUTHOR_EMAIL=" + ($email | @sh)
            + " GIT_COMMITTER_NAME=" + ($name | @sh)
            + " GIT_COMMITTER_EMAIL=" + ($email | @sh)
            + "\n" + $cmd
          )})
      }}
    end
  ' 2>/dev/null || echo '{}'

# Never let a hook failure interfere with the tool call.
exit 0
