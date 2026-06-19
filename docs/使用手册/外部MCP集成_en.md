<p align="center">
  <a href="外部MCP集成.md">中文</a>
  &nbsp;|&nbsp;
  <a href="外部MCP集成_en.md">English</a>
</p>

# User Guide · External MCP Integration

Attach external MCP services to AI experts and call SkyWalking, Prometheus, Zabbix, and similar capabilities from chat.

**Three steps:** Register MCP → Bind to expert → Use in chat

> To create a **new custom digital expert** (not just bind to built-in query/inspection experts) and verify full **AI Brain auto-routing**, see [Custom Digital Experts](自定义数字专家_en.md).

---

## ① Register MCP Tool

**AI Platform → Tool Management → MCP Tools → New MCP**

Enter service URL and transport (SSE / Streamable HTTP), then save:

![Create MCP tool](../images/mcp-tool-create.png)

> Example: `open-api` → `http://192.168.50.69:18900/sse`, protocol SSE.

---

## ② Bind to Digital Expert

**AI Platform → Digital Experts → Edit → Tools** — check the MCP Tool ID (e.g. `open-api`):

![Bind MCP to Query Expert](../images/mcp-expert-bind.png)

---

## ③ Use in Chat

**AI Platform → AI Chat** — ask directly. The expert will call tools exposed by the MCP.

Ask "what tools do you have?" and the reply lists built-in APM tools plus external MCP tools (SkyWalking, Prometheus, Zabbix, etc.). Expand the reasoning trace to see skill loading and tool calls:

![MCP tools in chat](../images/mcp-chat-expanded.png)

---

## FAQ

| Symptom | Fix |
|---------|-----|
| Expert does not call MCP | Ensure tool is enabled and expert Tools includes the MCP |
| Connection failed | Verify URL/protocol; confirm backend can reach MCP service |
| Config change not applied | Save, then start a new chat |

Transport: **SSE** uses the `/sse` endpoint; **Streamable HTTP** uses the HTTP streaming endpoint.
