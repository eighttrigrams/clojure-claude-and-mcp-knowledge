# Weather MCP Server (Clojure)

A minimal weather MCP (Model Context Protocol) server implementation in Clojure using the Java MCP SDK.

## Features

- **Current Weather**: Get current weather conditions for any location
- **Free API**: Uses Open-Meteo API (no API key required)

## Prerequisites

- Clojure CLI tools
- Java 11 or higher

## Tools Available

### get_current_weather
Get current weather conditions for a specific location.

**Parameters:**
- `latitude` (number, required): Latitude coordinate
- `longitude` (number, required): Longitude coordinate

## Usage

### Running the Server

```bash
./run.sh
```

Or directly with Clojure:

```bash
clojure -M:run
```

### Integrating with Claude Desktop

1. Add this configuration to your Claude Desktop MCP settings file:

```json
{
  "mcpServers": {
    "weather": {
      "command": "/Users/daniel/Workspace/eighttrigrams/tracker.project/clojure-mcp/run.sh"
    }
  }
}
```

### Integrating with Claude Code

```sh
$ claude mcp add weather -- /Users/daniel/Workspace/eighttrigrams/tracker.project/clojure-mcp/run.sh # or
$ claude mcp add weather -- sh /Users/daniel/Workspace/eighttrigrams/tracker.project/clojure-mcp/run.sh
```
