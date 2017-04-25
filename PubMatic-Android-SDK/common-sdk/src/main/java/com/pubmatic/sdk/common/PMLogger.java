/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2017
 * PubMatic, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 *
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.pubmatic.sdk.common;

import android.util.Log;

public class PMLogger {

    public enum LogLevel {
        None, Error, Debug,
    }

    /**
     * Interface allowing application developers to control logging.
     */
    public interface LogListener {
        /**
         * Invoked when the SDK logs events. If applications override logging
         * they can return true to indicate the log event has been consumed and
         * the SDK processing is not needed.
         * <p>
         * Will not be invoked if the adView instance's logLevel is set lower
         * than the event.
         *
         * @param event
         *            String representing the event to be logged.
         * @param logLevel
         *            LogLevel of the event.
         * @return
         */
        public void onLogEvent(String event, LogLevel logLevel);
    }

    private static LogLevel logLevel = LogLevel.Error;

    /**
     * Sets the log level of the instance. Logging is done through console logging.
     *
     * @param logLevel LogLevel
     */
    public static void setLogLevel(LogLevel logLevel) {
        PMLogger.logLevel = logLevel;
    }

    /**
     * Returns the currently configured log level.
     *
     * @return currently configured LogLevel
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }



    public static void logEvent(String event, LogLevel eventLevel) {
        if (eventLevel.ordinal() > logLevel.ordinal()) {
            return;
        }

        switch (eventLevel){
            case Debug:
                Log.d("PubMatic SDK", event);
                break;
            case Error:
                Log.e("PubMatic SDK", event);
                break;
            default:
                // Don't Log
        }
    }
}
