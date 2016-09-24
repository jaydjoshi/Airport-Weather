package com.crossover.trial.weather.exception;

import com.crossover.trial.weather.model.DataPointType;

/**
 * An internal exception marker
 * implemented custom WeatherException extends Exception
 * 
 */
public class WeatherException extends Exception {

	/**
	 * generated serial id
	 */
	private static final long serialVersionUID = -1763593411470554908L;

	private final DataPointType dataPointType;

    public WeatherException() {
    	super();
    	this.dataPointType=null;
	}
    
    public WeatherException(Throwable cause){
    	super(cause);
    	this.dataPointType=null;
    }
    
    public WeatherException(String message){
    	super(message);
    	this.dataPointType=null;
    }

	public WeatherException(DataPointType dataPointType, String message) {
		super(message);
		this.dataPointType = dataPointType;
	}

	public WeatherException(DataPointType dataPointType, String message, Throwable cause) {
		super(message, cause);
		this.dataPointType = dataPointType;
	}

	public DataPointType getDataPointType() {
		return dataPointType;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " for data type " + dataPointType.name();
	}



}
