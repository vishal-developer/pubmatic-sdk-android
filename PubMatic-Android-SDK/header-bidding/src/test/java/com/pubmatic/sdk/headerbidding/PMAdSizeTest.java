package com.pubmatic.sdk.headerbidding;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;

public class PMAdSizeTest {

    private static final int FAKE_HEIGHT       = 320;
    private static final int FAKE_WIDTH        = 480;

    @Test
    public void test_constructor() {
        PMAdSize size = new PMAdSize(FAKE_WIDTH, FAKE_HEIGHT);

        assertEquals(FAKE_HEIGHT, size.getHeight());
        assertEquals(FAKE_WIDTH, size.getWidth());

        size.setHeight(0);
        assertEquals(0, size.getHeight());

        size.setWidth(0);
        assertEquals(0, size.getWidth());

    }
}
