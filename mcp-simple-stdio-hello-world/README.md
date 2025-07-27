# Weather MCP Server

A minimal MCP (Model Context Protocol) server that provides fake weather information.

## Run continuously

Either

```
$ /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
```

or

```
$ cd /Users/daniel/Workspace/eighttrigrams/tracker-mcp
$ clj -M:run
```

and then feed it lines like

```json
{"id":1,"method":"initialize","params":{}}
```

and it will respond to that on pressing **enter**. 

That is, the server reads JSON-RPC requests from stdin and writes responses to stdout.

## One-shot usage examples

```sh
$ echo '{"id":2,"method":"tools/list","params":{}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
$ echo '{"id":3,"method":"tools/call","params":{"name":"get_issues","arguments":{"q":"Paris"}}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh
$ echo '{"id":1,"method":"initialize","params":{}}' | /Users/daniel/Workspace/eighttrigrams/tracker-mcp/run.sh


{"id":3,"method":"tools/call","params":{"name":"get_related_items","arguments":{"selected-context-item-id":"10935", "q":"", "secondary-contexts-items-ids":["11041"]}}}
```

## Features

- Implements MCP protocol version 2024-11-05
- Provides a `get_issues` tool that returns up to 10 search hits at a time

## Adding to Claude Code

Run

```sh
$ claude mcp add tracker -- sh /Users/daniel/Workspace/eighttrigrams/tracker.project/tracker-mcp/run.sh
```
