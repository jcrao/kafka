package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.demo.entry.Message;
import com.google.common.collect.Lists;

@Controller
public class KafkaProducer {
	
	  private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Value("${kafka.topic.reading}")
	private String[] topics;
	
	@RequestMapping(value = "demo", method = RequestMethod.GET)
	public void getMessage(){
		Message msg = new Message();
		msg.setStatus("22");
		msg.setType("1");
		List<Message> messes = Lists.newArrayList();
		messes.add(msg);
		alarmKafkaSend(messes);
	}
	
	private void alarmKafkaSend(List<Message> messes) {
		if (messes != null) {
			for(Message alarm : messes){
				if (kafkaTemplate != null) {
					kafkaTemplate.send(MessageBuilder.withPayload(alarm.toString())
							.setHeader(KafkaHeaders.TOPIC, this.topics[0]).build());
					kafkaTemplate.flush();
					logger.info("Sent successfully");
				} else {
					logger.info("Sent fail");
				}
			}
		}

	}

}
