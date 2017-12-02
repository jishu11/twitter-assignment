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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TwitterScanner {

    final int[] sum = new int[1];
    List<TSValue> tsValuesList = new ArrayList<>();

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
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void storeValue(TSValue value) {
        tsValuesList.add(value);
        int index = tsValuesList.size();
        if(tsValuesList.size() > 1) {
            System.out.println("Old mentions >>>> " + tsValuesList.get(index - 2).val + " --------- " + tsValuesList.get(index - 2).timestamp);
            System.out.println("New mentions >>>> " + tsValuesList.get(index - 1).val + " --------- " + tsValuesList.get(index - 1).timestamp);

            if(tsValuesList.get(0).val == 0 && index == 2) {
                System.out.println("Initial increase....");
            }
            else if(tsValuesList.get(index - 1).val == tsValuesList.get(index - 2).val) {
                System.out.println("No change!!!");
            }
            else {
                performAction.accept(tsValuesList.get(index - 1).val, tsValuesList.get(index - 2).val);
            }
        }
    }

    BiFunction<Double, Double, Double> difference = (value1, value2) -> (value1 > value2) ? value1 - value2 : value2 - value1;
    Function<Double, String> convertDecimal = value -> new DecimalFormat("#.00").format(value).toString() + "%";
    BiFunction<Double, Double, Double> percentageCal = (value, original) -> value * 100 / original;
    BiFunction<Double, Double, String> status = (value1, value2) -> (value1 > value2) ? "Increased by --- " : "Decreased by --- ";
    protected BiConsumer<Double, Double> performAction = this::check;

    protected void check(Double newMention, Double originalMention) {
        System.out.println(
                status.apply(newMention, originalMention) +
                        convertDecimal.apply(
                                percentageCal.apply(
                                        difference.apply(newMention, originalMention),
                                        originalMention)
                        ));
    }

    public static void main(String ... args) {
        TwitterScanner scanner = new TwitterScanner("Facebook");
        scanner.run();
    }

}
