package com.pubmatic.sdk.headerbidding;


import org.junit.Test;
import org.mockito.Mock;
import android.content.Context;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PMBannerPrefetchRequestTest {

    private static final int FAKE_HEIGHT       = 320;
    private static final int FAKE_WIDTH        = 480;
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
        PMPrefetchRequest request = PMPrefetchRequest.initHBRequestForImpression(FAKE_PUBID, impression);

        //assertEquals(FAKE_HEIGHT, request.getHeight());
        //assertEquals(FAKE_WIDTH, request.getWidth());

        request.setAdSize(new com.pubmatic.sdk.common.PMAdSize(FAKE_WIDTH, FAKE_HEIGHT));
        assertEquals(FAKE_HEIGHT, request.getAdSize().getAdHeight());

        assertEquals(FAKE_WIDTH, request.getAdSize().getAdWidth());

    }
}
