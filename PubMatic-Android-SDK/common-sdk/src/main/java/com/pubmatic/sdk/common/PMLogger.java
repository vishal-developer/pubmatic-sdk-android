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
