# Weather MCP Server

## Run

Run `./run.sh` or `clj -M:run`. It should do nothing but wait for a line of text and pressing ***enter***.

Entering

```
{"id":1,"method":"initialize","params":{}}
```

will yield

```
{"jsonrpc":"2.0","result":{"protocolVersion":"2024-11-05","capabilities":{"tools":{}},"serverInfo":{"name":"weather-mcp","version":"1.0.0"}},"id":1}
```

## Adding MCP to Claude Code

Run

```sh
$ claude mcp add weather -- /absolute/path/to/run.sh
```
