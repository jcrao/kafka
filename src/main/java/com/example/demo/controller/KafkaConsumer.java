package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.kafka.AbstractKafkaConsumer;

@Component
public class KafkaConsumer extends AbstractKafkaConsumer{
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
    @Value("${kafka.topic.reading}")
    private String[] topics;

    @Value("${kafka.topic.alarmReading.groupName}")
    private String consumerGroupName;

	@Override
	public String getTopic() {
		return topics[0];
	}

	@Override
	public String getGroup() {
		return consumerGroupName;
	}

	@Override
	public void process(String record) throws Exception {
         log.info("publish next info reading: "+record);
		
	}

}
