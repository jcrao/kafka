/*
 * Copyright (c) 2016, INESA, China.
 * All rights reserved.
 * Modification, redistribution and use in source and binary
 * forms, with or without modification, are not permitted
 * without prior written approval by the copyright holder.
 */

package com.example.demo.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.transaction.annotation.Transactional;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

public abstract class AbstractKafkaConsumer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DiscoveryClient discoveryClient;

    private ConsumerConnector consumerConnector;

    @Value("${kafka.zookeeper.connect}")
    private String zookeeperConnect;

    public abstract String getTopic();

    public abstract String getGroup();

    public String offset() {
        return "smallest";
    }

    @Transactional
    public abstract void process(String record) throws Exception;

    @PostConstruct
    public void consumer() {
        discoveryClient.getServices();
        new Thread(new Runnable() {

            @Override
            public void run() {
                String fileName = getTopic() + "_" + getGroup() + ".txt";
                ConsumerIterator<String, String> consumerIterator = getConsumerIterator();
                while (consumerIterator.hasNext()) {
                    String message = null;
                    try {
                        message = consumerIterator.next().message();
                        process(message);
                    } catch (Exception e) {
                        logger.error("处理失败：" + message, e);
                        File file = new File(fileName);
                        BufferedWriter bw = null;
                        try {
                            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                            bw.write(message);
                            bw.newLine();
                            bw.flush();
                        } catch (Exception e1) {
                            logger.error("写入文件失败" + message, e1);
                        } finally {
                            if (bw != null) {
                                try {
                                    bw.close();
                                } catch (IOException e1) {
                                    logger.error("关闭文件失败", e1);
                                }
                            }
                        }
                    } finally {
                        consumerConnector.commitOffsets();
                    }
                }
            }
        }).start();

    }

    public ConsumerIterator<String, String> getConsumerIterator() {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", zookeeperConnect);
        properties.put("group.id", getGroup());
        properties.put("enable.auto.commit", "false");
        properties.put("auto.offset.reset", offset());
        properties.put("fetch.message.max.bytes", "20971520");
        properties.put("max.partition.fetch.bytes", "20971520");
        // properties.put("replica.fetch.max.bytes", "20971520");
        properties.put("message.max.bytes", "20971520");
        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(getTopic(), new Integer(1));
        Map<String, List<KafkaStream<String, String>>> messageStreamsMap =
            consumerConnector.createMessageStreams(topicCountMap, new StringDecoder(new VerifiableProperties()),
                new StringDecoder(new VerifiableProperties()));
        KafkaStream<String, String> kafkaStream = messageStreamsMap.get(getTopic()).get(0);
        return kafkaStream.iterator();
    }

}
