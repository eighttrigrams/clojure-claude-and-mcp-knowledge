#!/usr/bin/env bash
# Drives hooks/git-identity.sh with synthetic PreToolUse payloads and asserts
# on the rewritten command (or on the absence of a rewrite).
set -uo pipefail

HOOK="$(cd "$(dirname "$0")/.." && pwd)/hooks/git-identity.sh"
PASS=0
FAIL=0

# run <tool_name> <command> -> prints the rewritten command, or "" if untouched
run() {
  jq -n --arg t "$1" --arg c "$2" \
    '{hook_event_name:"PreToolUse", tool_name:$t, tool_input:{command:$c, description:"d"}}' \
  | "$HOOK" \
  | jq -r '.hookSpecificOutput.updatedInput.command // ""'
}

expect_rewritten() {
  local desc="$1" cmd="$2"
  local out; out=$(run Bash "$cmd")
  if [[ "$out" == export*GIT_AUTHOR_NAME* && "$out" == *"$cmd" ]]; then
    PASS=$((PASS+1))
  else
    FAIL=$((FAIL+1)); printf 'FAIL: %s\n  in:  %q\n  out: %q\n' "$desc" "$cmd" "$out"
  fi
}

expect_untouched() {
  local desc="$1" cmd="$2" tool="${3:-Bash}"
  local out; out=$(run "$tool" "$cmd")
  if [[ -z "$out" ]]; then
    PASS=$((PASS+1))
  else
    FAIL=$((FAIL+1)); printf 'FAIL: %s\n  in:  %q\n  out: %q\n' "$desc" "$cmd" "$out"
  fi
}

expect_rewritten 'plain commit'          'git commit -m "hello"'
expect_rewritten 'compound command'      'git add . && git commit -m "hello"'
expect_rewritten 'leading cd'            'cd /tmp/repo && git commit -am wip'
expect_rewritten 'explicit -c identity'  'git -c user.name=Claude commit -m x'
expect_rewritten 'repo flag'             'git -C /tmp/repo commit -m x'
expect_rewritten 'global flag'           'git --no-pager commit -m x'
expect_rewritten 'no-verify'             'git commit --no-verify -m x'
expect_rewritten 'heredoc message'       'git commit -F - <<EOF
line one
line two
EOF'
expect_rewritten 'trailing semicolon'    'git commit -m x;'
expect_rewritten 'quoting hazard'        $'git commit -m "it\'s $HOME \\`ok\\` 100%"'

expect_untouched 'git log'               'git log --oneline -3'
expect_untouched 'grep for commit'       'git log --grep commit'
expect_untouched 'commit-ish subcommand' 'git commit-tree abc123'
expect_untouched 'unrelated word'        'ls /var/committed'
expect_untouched 'already prefixed'      "export GIT_AUTHOR_NAME='Claude'
git commit -m x"
expect_untouched 'inline env already'    'GIT_AUTHOR_NAME=Claude git commit -m x'
expect_untouched 'not the Bash tool'     'git commit -m x' Read

printf '\n%d passed, %d failed\n' "$PASS" "$FAIL"
[[ $FAIL -eq 0 ]]
