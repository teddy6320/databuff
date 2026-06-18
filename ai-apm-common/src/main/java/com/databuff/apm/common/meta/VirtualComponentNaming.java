package com.databuff.apm.common.meta;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 虚拟组件服务名/实例命名，与历史 portal {@code ServiceUtil#initDefaultComponentNameInstance} 行为一致。
 */
public final class VirtualComponentNaming {

    public record NamedInstance(String service, String serviceInstance) {
    }

    private VirtualComponentNaming() {
    }

    public static NamedInstance resolve(
            String componentName,
            String dbInstance,
            String peerHost,
            String peerPort,
            String reportService) {
        String component = blankToNull(componentName);
        String instance = blankToNull(dbInstance);
        String host = blankToNull(peerHost);
        String port = blankToNull(peerPort);
        String reporter = blankToNull(reportService);

        String service;
        String serviceInstance;
        if (instance != null) {
            service = format("[%s]%s", componentOrDefault(component), instance);
            serviceInstance = host != null ? host : service;
        } else if (host != null) {
            service = "[" + componentOrDefault(component) + "]" + host + (port != null ? ":" + port : "");
            serviceInstance = host;
        } else {
            String fallback = reporter != null ? reporter : "unknown";
            service = format("[%s]%s", componentOrDefault(component), fallback);
            serviceInstance = service;
        }
        return new NamedInstance(service, serviceInstance);
    }

    public static NamedInstance mq(String mqType, String topic, String broker) {
        String type = componentOrDefault(mqType);
        String resolvedTopic = blankToNull(topic);
        if (resolvedTopic == null) {
            return null;
        }
        String resolvedBroker = blankToNull(broker);
        if (resolvedBroker == null) {
            resolvedBroker = "default";
        }
        return new NamedInstance("[" + type + "]" + resolvedTopic, resolvedBroker);
    }

    public static Map<String, String> asMap(NamedInstance named) {
        if (named == null) {
            return Map.of();
        }
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("service", named.service());
        map.put("serviceInstance", named.serviceInstance());
        return map;
    }

    private static String componentOrDefault(String componentName) {
        return componentName != null && !componentName.isBlank() ? componentName.trim() : "unknown";
    }

    private static String format(String pattern, String component, String value) {
        return String.format(pattern, componentOrDefault(component), value);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
