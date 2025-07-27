# Clojure Claude & MCP Knowledge

As developers we want to get the most out of our tools,
and for the current generation of LLM-based AI tools like **Claude** 
that means extending them via *MCP*. 

This repository should serve as an intro to Claude and MCP development in Clojure.
Things here have been tested only under MacOS (on an M chip).

## Claude Code and Claude Desktop

**Claude Code** is a terminal/command-line based application primarily focused at editing
code or code-like artifacts on your file system and helping you develop running programs.

**Claude Desktop** is a web-app like experience, which allows you do chat about whatever and 
do research, similar to ChatGPT. It runs on your desktop, however, to exploit the full benefits
of having access to your local system (if you allow it).

For Claude Desktop you need to signup for a Pro or Max subscription (can be done monthly) to which
various rate limits apply depending on the options you chose. Claude Code can share that subscription
with Desktop and both together share the assigned usage quotas. However, if you only intend to use Claude
Code you might want to simply buy API credits on console.anthropic.com.

Both Claude Code and Desktop can run on the same system, but don't share all their configuration files. So don't expect
things you configure in Code to be automatically available to Desktop for example.

On the other hand, some artifacts like your local `~/.claude.md` are shared and read by both installations.

## Clojure MCP development

MCP allows you to write tools to which your LLM has access to. In this repository, we find two examples, one
using plain Clojure, one using the Java based modelcontextprotocol SDK. These examples are intended to be minimal.
They are designed to help you to get started with writing your own MCPs.
