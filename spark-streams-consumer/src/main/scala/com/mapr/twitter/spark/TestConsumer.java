package com.mapr.twitter.spark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by chufe on 13/09/16.
 */
public class TestConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
//        props.put("autocommit.enable", "true");
        props.put("auto.offset.reset", "latest");
        props.put("autocommit.interval.ms", "1000");
        props.put("group.id", "abc");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //   consumer.subscribe(Arrays.asList("/user/mapr/twitter:samples"));
        consumer.assign(Arrays.asList(new TopicPartition("/twitter:samples", 0)));
        while (true) {
            ConsumerRecords<String, String> msgs = consumer.poll(100);
            System.out.println("Poll " + msgs.count());
            Iterator<ConsumerRecord<String, String>> iterator = msgs.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }

        }
    }
}
