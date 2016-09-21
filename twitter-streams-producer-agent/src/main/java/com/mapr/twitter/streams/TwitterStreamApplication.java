package com.mapr.twitter.streams;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogManager;

@SpringBootApplication
public class TwitterStreamApplication implements CommandLineRunner {
    private static String configFile;
    @Autowired
    private TwitterStreamService twitterStreamService;

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        //        JISystem.setAutoRegisteration(true);
        SLF4JBridgeHandler.install();
        if (args.length != 1) {
            System.out.println("Usage: mapr-twitter-streams <config file>");
            return;
        }
        configFile = args[0];
        ConfigurableApplicationContext ctx = SpringApplication.run(TwitterStreamApplication.class, args);
        final CountDownLatch closeLatch = ctx.getBean(CountDownLatch.class);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                closeLatch.countDown();
            }
        });
        closeLatch.await();
    }

    @Bean
    public TwitterConfig twitterConfig() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        return yaml.loadAs(new FileInputStream(new File(configFile)), TwitterConfig.class);
    }

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    @Override
    public void run(String... args) throws Exception {
        twitterStreamService.start();
    }
}