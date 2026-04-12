package kz.kbtu.yarmaq.zombiemanager.producer;

import kz.kbtu.yarmaq.zombiemanager.dto.DistrictCriticalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictKafkaProducer {

    private final KafkaTemplate<String, DistrictCriticalEvent> kafkaTemplate;

    public void sendCriticalEvent(DistrictCriticalEvent event) {
        log.info("Sending critical event for district: {}", event.getDistrictName());
        kafkaTemplate.send("district.critical", event.getDistrictId().toString(), event);
    }
}
