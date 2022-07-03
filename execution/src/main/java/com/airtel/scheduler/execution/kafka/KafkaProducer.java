package com.airtel.scheduler.execution.kafka;

import com.airtel.scheduler.execution.utils.CommonUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    @Qualifier("schedulerKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(Object message, String topic, Map<String, String> kafkaHeaders) {
        logger.info("Publishing Message to Topic : {} with Content :{}, identifier: {}", topic, message, kafkaHeaders);
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, message);
        if (!CommonUtils.isNullOrEmptyMap(kafkaHeaders)) {
            for (Map.Entry<String, String> entry : kafkaHeaders.entrySet()) {
                record.headers().add(new RecordHeader(entry.getKey(), entry.getValue().getBytes()));
            }
        }
        kafkaTemplate.send(record);
    }
}