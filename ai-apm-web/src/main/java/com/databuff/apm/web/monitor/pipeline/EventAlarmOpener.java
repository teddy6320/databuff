package com.databuff.apm.web.monitor.pipeline;

import com.databuff.apm.web.persistence.EventPersistence;
import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.AlarmStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EventAlarmOpener {

    @Autowired
    private AlarmStore alarmStore;
    @Autowired
    private EventPersistence eventPersistence;

    public Optional<Alarm> openForEvent(EventRecord event) {
        if (!event.isAbnormal()) {
            return Optional.empty();
        }
        Alarm alarm = alarmStore.openResolved(
                event.service(),
                event.detectionWay(),
                event.level(),
                event.message(),
                event.triggeredAt());
        eventPersistence.linkToAlarm(event.id(), alarm.id());
        return Optional.of(alarm);
    }
}
