package com.databuff.apm.web.monitor;

import java.util.List;
import java.util.Optional;

public interface EventRuleStore {

    List<EventRule> list();

    Optional<EventRule> findById(long id);

    EventRule save(EventRule rule);

    boolean delete(long id);

    EventRule create(CreateRequest request);

    record CreateRequest(
            String ruleName,
            String classify,
            String service,
            String metric,
            double threshold,
            String comparator,
            boolean enabled,
            String detectionWay,
            String queryJson) {

        public CreateRequest(
                String ruleName,
                String service,
                double threshold,
                String comparator,
                boolean enabled) {
            this(ruleName, service, threshold, comparator, enabled, EventRule.WAY_THRESHOLD);
        }

        public CreateRequest(
                String ruleName,
                String service,
                double threshold,
                String comparator,
                boolean enabled,
                String detectionWay) {
            this(
                    ruleName,
                    EventRule.CLASSIFY_SINGLE,
                    service,
                    null,
                    threshold,
                    comparator,
                    enabled,
                    detectionWay,
                    null);
        }

        public String detectionWay() {
            if (detectionWay == null || detectionWay.isBlank()) {
                return EventRule.WAY_THRESHOLD;
            }
            return detectionWay;
        }
    }
}
