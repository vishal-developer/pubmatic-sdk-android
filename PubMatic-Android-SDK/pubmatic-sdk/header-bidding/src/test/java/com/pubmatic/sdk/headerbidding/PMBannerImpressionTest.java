package com.pubmatic.sdk.headerbidding;


import org.junit.Test;
import org.mockito.Mock;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PMBannerImpressionTest {

    private static final String FAKE_ID      = "1234";
    private static final String FAKE_SLOT_ID = "4321";
    private static final int FAKE_INDEX      = 111;

    @Mock
    List<PMAdSize> mockAdSizes;

    @Test
    public void test_constructor() {
        PMBannerImpression impression = new PMBannerImpression(FAKE_ID, FAKE_SLOT_ID, mockAdSizes, FAKE_INDEX);

        assertEquals(FAKE_ID, impression.getId());
        assertEquals(FAKE_SLOT_ID, impression.getAdSlotId());

        assertEquals(mockAdSizes, impression.getAdSizes());

        impression.setInterstitial(true);
        assertEquals(true, impression.isInterstitial());


        impression.setInterstitial(false);
        assertEquals(false, impression.isInterstitial());


        impression.setAdSizes(mockAdSizes);
        assertEquals(mockAdSizes, impression.getAdSizes());

        assertEquals(false, impression.validate());
    }

    @Test
    public void test_setInterstitial() {

    }
}
