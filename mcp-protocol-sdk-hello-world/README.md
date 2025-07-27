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

Add this configuration to your Claude Desktop MCP settings file.
To find the configuration file use the MacOS operating system's menu  "Claude ->> Settings" then 
navigate inside Claude via "Developer" section to **Edit Config**. 
It will open the containing folder of `claude_desktop_config.json` in Finder. There is also
a **Open Logs Folder** button in which logs are stored (useful for debugging your MCPs under development).

```json
{
  "mcpServers": {
    "weather": {
      "command": "/Users/daniel/Workspace/eighttrigrams/clojure-claude-and-mcp-knowledge/mcp-protocol-sdk-hello-world/run.sh"
    }
  }
}
```

### Integrating with Claude Code

```sh
$ claude mcp add weather -- /Users/daniel/Workspace/eighttrigrams/clojure-claude-and-mcp-knowledge/mcp-protocol-sdk-hello-world/run.sh # or
$ claude mcp add weather -- sh /Users/daniel/Workspace/eighttrigrams/clojure-claude-and-mcp-knowledge/mcp-protocol-sdk-hello-world/run.sh
```
