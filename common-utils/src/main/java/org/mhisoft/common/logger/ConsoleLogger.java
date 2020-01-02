package org.mhisoft.common.logger;

import java.util.Date;

/**
 * Description:
 *
 * @author Tony Xue
 * @since May, 2016
 */

public class ConsoleLogger extends MHILogger {

	public ConsoleLogger(Class clazz) {
		super(clazz);
	}



	@Override
	public void debug(final String msg) {
		if (isDebugEnabled())
			System.out.println(String.format(msgFormat, new Date().toString(), msg));

	}

	@Override
	public void info(String msg) {
		if (isInfoEnabled())
			System.out.println(String.format(msgFormat, new Date().toString(), msg));
	}

	@Override
	public void error(String msg) {
		System.err.println(String.format(msgFormat, new Date().toString(), msg));
	}
}
