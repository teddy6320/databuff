package com.databuff.apm.web.monitor;

import java.util.List;

public record AlarmIncident(String service, int openCount, List<Alarm> events) {
}
