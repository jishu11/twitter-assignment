package com.fractallabs.assignment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TwitterScanner.class, TwitterStreamFactory.class, TwitterStream.class, TwitterScanner.TSValue.class})
public class TestTwitterScanner {

    private List<TwitterScanner.TSValue> tsValuesTestList;

    @org.mockito.InjectMocks
    private TwitterStreamFactory twitterStreamFactory;

    @org.mockito.InjectMocks
    private TwitterStream twitterStream;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }


    @Test
    public void testWithNoChangeInMentions() {
        tsValuesTestList = new ArrayList<>();
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

    


}
