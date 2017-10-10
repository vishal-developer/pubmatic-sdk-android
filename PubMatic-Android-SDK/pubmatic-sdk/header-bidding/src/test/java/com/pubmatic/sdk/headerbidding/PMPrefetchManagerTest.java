package com.pubmatic.sdk.headerbidding;


import org.junit.Test;
import org.mockito.Mock;
import android.content.Context;
import static org.junit.Assert.assertEquals;

public class PMPrefetchManagerTest {

    private static final int FAKE_HEIGHT       = 320;
    private static final int FAKE_WIDTH        = 480;
    private static final int FAKE_TIMEOUT      = 60000;
    private static final String FAKE_PUBID     = "31400";


    @Mock
    Context context;

    @Mock
    PMPrefetchManager.PMPrefetchListener listener;


    @Test
    public void test_constructor() {
        //PMPrefetchManager request = new PMPrefetchManager(context, listener);

    }
}
