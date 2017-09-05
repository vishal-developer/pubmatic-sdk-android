package com.pubmatic.sdk.headerbidding;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Secure;

import com.pubmatic.sdk.common.PMUtils;

import java.util.regex.Pattern;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PMUtilsTest {

    private static final String FAKE_RESULT = "bd9d3677-7119-48af-b3ff-990c78457643";
    private static final String FAKE_RESULT_SHA1 = "306ae63ed61371e41b30bdbe9ac3b7de9296334a";

    @Mock
    Context         mMockContext;
    @Mock
    Secure          mMockSettings;
    @Mock
    ContentResolver mMockResolver;


    @Test
    public void test_getNetworkType() {

    }

    @Test
    public void test_getUdidFromContext() {

        //ContentResolver resolver = when(mMockContext.getContentResolver()).thenReturn(anyObject()<ContentResolver.class>);//mMockContext.getContentResolver().;//when(mMockContext.getContentResolver()).thenReturn(mMockResolver);
        // Given a mocked Context injected into the object under test...
        /*when(mMockSettings.getString(Matchers.any(ContentResolver.class), Settings.Secure.ANDROID_ID))
                .thenReturn(FAKE_RESULT);

        // ...when the string is returned from the object under test...
        String result = PMUtils.getUdidFromContext(mMockContext);

        // ...then the result should be the expected one.
        assertEquals(result, FAKE_RESULT_SHA1);*/
    }

    @Test
    public void test_md5() {
        assertEquals(PMUtils.md5("bd9d3677-7119-48af-b3ff-990c78457643"), "574e98f1261d65dca64ae31cb717c242");
    }


    @Test
    public void test_sha1() {
        assertEquals(PMUtils.sha1("bd9d3677-7119-48af-b3ff-990c78457643"), "306ae63ed61371e41b30bdbe9ac3b7de9296334a");
    }

}

/*
Important links:
----------------
http://www.vogella.com/tutorials/Mockito/article.html#mockitousage
https://github.com/junit-team/junit4/wiki/Rules
https://blog.codecentric.de/en/2016/03/junit-testing-using-mockito-powermock/

 */

//assertEquals("Mockito rocks", result);
//when(mock.something()).then(AdditionalAnswers.returnsFirstArg())

/*
when(mock.someMethod(anyString())).thenAnswer(new Answer() {
    Object answer(InvocationOnMock invocation) {
        return invocation.getArguments()[0];
    }
});
 */