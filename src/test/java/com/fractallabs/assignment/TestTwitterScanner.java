package com.fractallabs.assignment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({TwitterScanner.class, TwitterStreamFactory.class, TwitterStream.class, TwitterScanner.TSValue.class})
public class TestTwitterScanner {

    private List<TwitterScanner.TSValue> tsValuesTestList;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        tsValuesTestList = new ArrayList<>();
        System.setOut(new PrintStream(outContent));
    }


    @Test
    public void testWithNoChangeInMentions() {
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now(), 1));
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now().plusSeconds(5), 1));
        TwitterScanner twitterScanner = new TwitterScanner("Facebook");
        twitterScanner.tsValuesList = tsValuesTestList;
        try {
            Whitebox.invokeMethod(twitterScanner, "storeValue", twitterScanner.tsValuesList.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(outContent.toString().contains("No change!!!"));
    }

    @Test
    public void testInitialStageInMentions() {
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now(), 0));
        TwitterScanner twitterScanner = new TwitterScanner("Facebook");
        twitterScanner.tsValuesList = tsValuesTestList;
        try {
            Whitebox.invokeMethod(twitterScanner, "storeValue", new TwitterScanner.TSValue(Instant.now().plusSeconds(5), 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(outContent.toString().contains("Initial increase...."));
    }

    @Test
    public void testDecreaseInMentions() {
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now(), 10));
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now().plusSeconds(5), 5));
        TwitterScanner twitterScanner = new TwitterScanner("Facebook");
        twitterScanner.tsValuesList = tsValuesTestList;
        try {
            Whitebox.invokeMethod(twitterScanner, "storeValue", new TwitterScanner.TSValue(Instant.now().plusSeconds(10), 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(outContent.toString().contains("Decreased by ---"));
    }

    @Test
    public void testIncreaseInMentions() {
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now(), 1));
        tsValuesTestList.add(new TwitterScanner.TSValue(Instant.now().plusSeconds(5), 5));
        TwitterScanner twitterScanner = new TwitterScanner("Facebook");
        twitterScanner.tsValuesList = tsValuesTestList;
        try {
            Whitebox.invokeMethod(twitterScanner, "storeValue", new TwitterScanner.TSValue(Instant.now().plusSeconds(10), 10));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(outContent.toString().contains("Increased by ---"));
    }


}
