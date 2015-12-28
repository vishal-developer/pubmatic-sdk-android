package com.pubmatic.sdk.common;

import android.graphics.Rect;
import android.view.View;

import java.util.Map;

public interface CommonDelegate {

	public enum LogLevel {
		None, Error, Debug,
	}

	/**
	 * Interface allowing application developers to control logging.
	 */
	public interface LogListener
	{
		/**
		 * Invoked when the SDK logs events.  If applications override logging they can return true to
		 * indicate the log event has been consumed and the SDK processing is not needed.
		 * <p>
		 * Will not be invoked if the adView instance's logLevel is set lower than the event.
		 * 
		 * @param adView
		 * @param event String representing the event to be logged.
		 * @param logLevel LogLevel of the event.
		 * @return
		 */
		public boolean onLogEvent(View adView, String event, LogLevel logLevel);
	}
}
