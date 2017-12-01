package com.fractallabs.assignment;

import org.apache.commons.lang3.StringUtils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TwitterScanner {

    final int[] sum = new int[1];
    List<TSValue> tsValuesList = new ArrayList<>();

    public TwitterScanner() { }

    public static class TSValue {
        private final Instant timestamp;
        private final double val;

        public TSValue(Instant timestamp, double val) {
            this.timestamp = timestamp;
            this.val = val;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public double getVal() {
            return val;
        }
    }

    public TwitterScanner(String companyName) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey("b3SNw4VosXmqGpgiN6C5jBc2R")
                .setOAuthConsumerSecret("STSFUecoYK9XXSk6ucjqaSFCQz5F0KHzOjpyXJH8uQscuviA67")
                .setOAuthAccessToken("2887065282-iTnOjKlJH06zh1KwpM7xHrzhzvNRU2XUP0gYMRy")
                .setOAuthAccessTokenSecret("B1sK1MTeV3Fs0kHsn2aD6vCteqF0FYNL0aV4NwBAWoeNh");

        TwitterStreamFactory twitterStreamFactory = new TwitterStreamFactory(configurationBuilder.build());
        TwitterStream twitterStream = twitterStreamFactory.getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if (status.getText().contains(companyName)) sum[0] = sum[0] + StringUtils.countMatches(status.getText().toLowerCase(), companyName.toLowerCase());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {}

            @Override
            public void onStallWarning(StallWarning warning) {}

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        FilterQuery filterQuery = new FilterQuery();
        String[] keywordsArray = {companyName};
        filterQuery.track(keywordsArray);
        twitterStream.filter(filterQuery);
    }

    public void run() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() ->  {
            TwitterScanner.TSValue tsValue = new TwitterScanner.TSValue(java.time.Instant.now(), Double.valueOf(sum[0]));
            storeValue(tsValue);
            sum[0] = 0;
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void storeValue(TSValue value) {
        tsValuesList.add(value);
        double percentage;
        int index = tsValuesList.size();
        double originalMentions = tsValuesList.get(index - 2).getVal();
        double currentMentions = tsValuesList.get(index - 1).getVal();
        if(tsValuesList.size() > 1) {
            System.out.println("Old mentions: " + originalMentions + "-----" + tsValuesList.get(index - 2).timestamp);
            System.out.println("New mentions: " + currentMentions + "-----" + tsValuesList.get(index - 1).timestamp);

            if(currentMentions > originalMentions && originalMentions == 0) {
                System.out.println("Initial increase....");
            }
            else if(currentMentions > originalMentions) {
                double increase = (currentMentions - originalMentions);
                percentage = (increase * 100) / tsValuesList.get(index - 2 ).val;
                System.out.println("Increased by " + new DecimalFormat("#.00").format(percentage) + "%");
            }
            else if(currentMentions == originalMentions) {
                System.out.println("No change!!!");
            }
            else {
                double decrease = (originalMentions - currentMentions);
                percentage = (decrease * 100) / tsValuesList.get(index - 2 ).val;
                System.out.println("Decreased by " + new DecimalFormat("#.00").format(percentage) + "%");
            }
        }
    }

    public static void main(String ... args) {
        TwitterScanner scanner = new TwitterScanner("London");
        scanner.run();
    }

}
