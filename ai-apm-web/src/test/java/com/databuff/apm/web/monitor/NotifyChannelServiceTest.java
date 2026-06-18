package com.databuff.apm.web.monitor;

import com.databuff.apm.web.ai.TestBeanSupport;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyChannelServiceTest {

    @Test
    void updatesWebhookConfig() {
        NotifyChannelService service = TestBeanSupport.notifyChannelService();
        Map<String, Object> updated = service.updateConfig(Map.of(
                "webhookUrl", "http://127.0.0.1:9999/hook",
                "enabled", true));
        assertThat(updated.get("webhookUrl")).isEqualTo("http://127.0.0.1:9999/hook");
    }

    @Test
    void skipsWebhookWhenDisabled() {
        NotifyChannelService service = TestBeanSupport.notifyChannelService();
        service.updateConfig(Map.of("webhookUrl", "http://127.0.0.1:1/hook", "enabled", false));
        service.notifyAlert(new Alarm(
                "A1", 1L, "demo", EventRule.WAY_THRESHOLD, "warning", "msg",
                Alarm.STATUS_OPEN, Instant.now(), null));
        assertThat(service.getConfig().get("enabled")).isEqualTo(false);
    }

    @Test
    void postsWebhookWhenEnabled() throws Exception {
        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        AtomicReference<String> body = new AtomicReference<>("");
        server.createContext("/hook", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes()));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        try {
            NotifyChannelService service = TestBeanSupport.notifyChannelService();
            int port = server.getAddress().getPort();
            service.updateConfig(Map.of(
                    "webhookUrl", "http://127.0.0.1:" + port + "/hook",
                    "enabled", true));
            service.notifyAlert(new Alarm(
                    "A1", 1L, "demo", EventRule.WAY_THRESHOLD, "warning", "msg",
                    Alarm.STATUS_OPEN, Instant.now(), null));
            assertThat(body.get()).contains("\"alarmId\":\"A1\"");
        } finally {
            server.stop(0);
        }
    }
}
