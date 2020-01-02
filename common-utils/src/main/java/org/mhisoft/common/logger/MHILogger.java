package org.mhisoft.common.logger;

/**
 * Description:
 *
 * @author Tony Xue
 * @since May, 2016
 */
public abstract  class MHILogger {

	Class clazz;
	String msgFormat;
	LoggerLevel loggerLevel;



	public MHILogger(Class clazz) {
		this.clazz = clazz;
		msgFormat = "%s ["+ this.clazz.getSimpleName() +"]: %s";
	}


	public MHILogger(Class clazz, LoggerLevel level)    {
		this(clazz)  ;
		loggerLevel = level;

	}


	abstract public void debug(final String msg);
	abstract public void info(final String msg);
	abstract public void error(final String msg);


	public  void setLoggerLevel(LoggerLevel loggerLevel) {
		this.loggerLevel = loggerLevel;
	}

	public  boolean isDebugEnabled() {
		return loggerLevel==LoggerLevel.debug || loggerLevel==LoggerLevel.info
				||loggerLevel==LoggerLevel.error ;
	}


	public  boolean isInfoEnabled() {
		return  loggerLevel==LoggerLevel.info
				||loggerLevel==LoggerLevel.error ;
	}


}
