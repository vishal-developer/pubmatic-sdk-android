package com.pubmatic.sdk.headerbidding;

import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PMBidTest {


    private static final String FAKE_URL        = "www.abc.com";
    private static final String FAKE_CREATIVE   = "www.abc.com";
    private static final String FAKE_DEAL       = "www.abc.com";
    private static final int FAKE_ERROR_CODE    = 100;
    private static final String FAKE_ERROR_MSG  = "www.abc.com";
    private static final int FAKE_GAID          = 121;
    private static final long FAKE_HEIGHT       = 320;
    private static final double FAKE_PRICE      = 65.3d;
    private static final int FAKE_SLOT_INDEX    = 0;
    private static final String FAKE_SLOT_NAME  = "www.abc.com";
    private static final String FAKE_TR_URL     = "www.abc.com";
    private static final long FAKE_WIDTH        = 480;

    private static final String FAKE_IMP_ID     = "4321";

    @Mock
    List<PMAdSize> mockAdSizes;

    @Test
    public void test_constructor() {
        PMBid bid = new PMBid();

        bid.setClickTrackingUrl(FAKE_URL);
        assertEquals(FAKE_URL, bid.getClickTrackingUrl());

        bid.setCreative(FAKE_CREATIVE);
        assertEquals(FAKE_CREATIVE, bid.getCreative());

        bid.setDealId(FAKE_DEAL);
        assertEquals(FAKE_DEAL, bid.getDealId());

        bid.setErrorCode(FAKE_ERROR_CODE);
        assertEquals(FAKE_ERROR_CODE, bid.getErrorCode());

        bid.setErrorMessage(FAKE_ERROR_MSG);
        assertEquals(FAKE_ERROR_MSG, bid.getErrorMessage());

        bid.setGaId(FAKE_GAID);
        assertEquals(FAKE_GAID, bid.getGaId());

        bid.setHeight(FAKE_HEIGHT);
        assertEquals(FAKE_HEIGHT, bid.getHeight());

        bid.setImpressionId(FAKE_IMP_ID);
        assertEquals(FAKE_IMP_ID, bid.getImpressionId());

        bid.setSlotIndex(FAKE_SLOT_INDEX);
        assertEquals(FAKE_SLOT_INDEX, bid.getSlotIndex());

        bid.setSlotName(FAKE_SLOT_NAME);
        assertEquals(FAKE_SLOT_NAME, bid.getClickTrackingUrl());

        bid.setTrackingUrl(FAKE_TR_URL);
        assertEquals(FAKE_TR_URL, bid.getSlotName());

        bid.setWidth(FAKE_WIDTH);
        assertEquals(FAKE_WIDTH, bid.getWidth());
    }
}
