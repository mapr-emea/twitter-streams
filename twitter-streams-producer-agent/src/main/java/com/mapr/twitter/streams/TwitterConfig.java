package com.mapr.twitter.streams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chufe on 10/06/16.
 */
public class TwitterConfig {
    private Map<String, String> kafka;
    private Map<String, String> twitter;
    private String targetTopic;
    private String language;

    public Map<String, String> getKafka() {
        return kafka;
    }

    public void setKafka(Map<String, String> kafka) {
        this.kafka = kafka;
    }

    public Map<String, String> getTwitter() {
        return twitter;
    }

    public void setTwitter(Map<String, String> twitter) {
        this.twitter = twitter;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    public void setTargetTopic(String targetTopic) {
        this.targetTopic = targetTopic;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
