package org.mhisoft.common.logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author Tony Xue
 * @since May, 2016
 */
public class Loggerfactory {

	static Map<Class, MHILogger> loggers = new HashMap<Class, MHILogger>();

	protected static MHILogger createLogger(Class clazz, LoggerLevel level) {
		//which logger impl to use?
		MHILogger logger =  new ConsoleLogger(clazz);
		logger.setLoggerLevel(level);
		return logger;
	}


	//todo get log leve from -DloggerLevel=debug/info/error?
//	public static MHILogger getLogger(Class clazz) {
//	   getLogger(clazz, )
//	}

	public static MHILogger getLogger(Class clazz, LoggerLevel level) {
		MHILogger logger = loggers.get(clazz);
		if (logger == null) {
			logger = createLogger(clazz, level);
			loggers.put(clazz, logger);
		}
		return  logger;
	}

}
