package com.mapr.twitter.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TwitterStreamService {
    private static final Logger logger = LoggerFactory.getLogger(TwitterStreamService.class);
    @Autowired
    private TwitterConfig twitterConfig;
    private AtomicLong count = new AtomicLong(0);

    public void start() {
        Properties properties = new Properties();
        Map<String, String> kafka = twitterConfig.getKafka();
        for (String kafkaKey : kafka.keySet()) {
            properties.setProperty(kafkaKey, kafka.get(kafkaKey));
        }
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        properties.setProperty("bootstrap.servers", "localhost:123");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {
                String json = TwitterObjectFactory.getRawJSON(status);
                producer.send(new ProducerRecord<>(twitterConfig.getTargetTopic(), json));
                long current = count.incrementAndGet();
                if(current % 100 == 0) {
                    System.out.println("Datasets: " + current);
                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        ConfigurationBuilder cb = new ConfigurationBuilder();
    //    cb.setDebugEnabled(true)
        cb.setJSONStoreEnabled(true);
        cb.setOAuthConsumerKey(twitterConfig.getTwitter().get("consumerKey"))
                .setOAuthConsumerSecret(twitterConfig.getTwitter().get("consumerSecret"))
                .setOAuthAccessToken(twitterConfig.getTwitter().get("accessToken"))
                .setOAuthAccessTokenSecret(twitterConfig.getTwitter().get("accessTokenSecret"));
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);
        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        String language = twitterConfig.getLanguage();
        if(language.trim().equals("all")) {
            twitterStream.sample();
        }
        else {
            twitterStream.sample(language);
        }

    }
}