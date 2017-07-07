package com.pubmatic.sdk.headerbidding;


import org.junit.Test;
import org.mockito.Mock;
import android.content.Context;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PMBannerPrefetchRequestTest {

    private static final int FAKE_HEIGHT       = 320;
    private static final int FAKE_WIDTH        = 480;
    private static final int FAKE_TIMEOUT      = 60000;
    private static final String FAKE_PUBID     = "31400";

    private static final String FAKE_ID      = "1234";
    private static final String FAKE_SLOT_ID = "4321";
    private static final int FAKE_INDEX      = 111;

    @Mock
    List<PMAdSize> mockAdSizes;

    @Mock
    Context context;

    @Test
    public void test_constructor() {
        PMBannerImpression impression = new PMBannerImpression(FAKE_ID, FAKE_SLOT_ID, mockAdSizes, FAKE_INDEX);
        PMBannerPrefetchRequest request = PMBannerPrefetchRequest.initHBRequestForImpression(context, FAKE_PUBID, impression);

        //assertEquals(FAKE_HEIGHT, request.getHeight());
        //assertEquals(FAKE_WIDTH, request.getWidth());

        request.setHeight(0);
        assertEquals(0, request.getHeight());

        request.setWidth(0);
        assertEquals(0, request.getWidth());

        request.setTimeoutInterval(FAKE_TIMEOUT);
        assertEquals(FAKE_TIMEOUT, request.getTimeoutInterval());
    }
}
