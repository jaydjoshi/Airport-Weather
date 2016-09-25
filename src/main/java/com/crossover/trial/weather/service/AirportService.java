/**
 * 
 */
package com.crossover.trial.weather.service;

import java.util.List;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.util.WeatherConstant;

/**
 * @author jdhirendrajoshi
 * Singleton design pattern implementation for Weather Service
 *
 */
public enum AirportService {
	
	INSTANCE;
	
	private static List<AirportData> airportData = WeatherService.INSTANCE.getAirportData();

    /** atmospheric information for each airport, idx corresponds with airportData */
    private static List<AtmosphericInformation> atmosphericInformation = WeatherService.INSTANCE.getAtmosphericInformation();
    
	
	 /**
     * Update the airports weather data with the collected data.
     *
     * @param iataCode the 3 letter IATA code
     * @param pointType the point type {@link DataPointType}
     * @param dp a datapoint object holding pointType data
     *
     * @throws WeatherException if the update can not be completed
     */
    public void addDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException {
    	try{
	        int airportDataIdx = WeatherService.INSTANCE.getAirportDataIdx(iataCode);
	        AtmosphericInformation ai = atmosphericInformation.get(airportDataIdx);
	        updateAtmosphericInformation(ai, pointType, dp);
	    }catch (WeatherException e) {
        	//LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WeatherException(DataPointType.valueOf(pointType.toUpperCase()), e.getMessage(), e);
        }
        catch (Exception e) {
        	//LOGGER.log(Level.SEVERE, e.getMessage(), e);
        	throw new WeatherException(DataPointType.valueOf(pointType.toUpperCase()), e.getMessage(), e);
        }
    }

    /**
     * update atmospheric information with the given data point for the given point type
     *
     * @param ai the atmospheric information object to update
     * @param pointType the data point type as a string
     * @param dp the actual data point
     */
    public void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp) throws WeatherException,Exception {
        final DataPointType dptype = DataPointType.valueOf(pointType.toUpperCase());

		switch (dptype) {
			case WIND:
				if (dp.getMean() >= WeatherConstant.WIND_LOWER_LIMIT) {
	                ai.setWind(dp);
	                ai.setLastUpdateTime(System.currentTimeMillis());
	            }
				break;
				
			case TEMPERATURE:
				if (dp.getMean() >= WeatherConstant.TEMPERATURE_LOWER_LIMIT && dp.getMean() < WeatherConstant.TEMPERATURE_UPPER_LIMIT) {
	                ai.setTemperature(dp);
	                ai.setLastUpdateTime(System.currentTimeMillis());
	            }
				break;
				
			case HUMIDTY:
				if (dp.getMean() >= WeatherConstant.HUMIDTY_LOWER_LIMIT && dp.getMean() < WeatherConstant.HUMIDTY_UPPER_LIMIT) {
	                ai.setHumidity(dp);
	                ai.setLastUpdateTime(System.currentTimeMillis());
	            }
				break;
				
			case PRESSURE:
				if (dp.getMean() >= WeatherConstant.PRESSURE_LOWER_LIMIT && dp.getMean() < WeatherConstant.PRESSURE_UPPER_LIMIT) {
	                ai.setPressure(dp);
	                ai.setLastUpdateTime(System.currentTimeMillis());
	            }
				break;
				
			case CLOUDCOVER:
				if (dp.getMean() >= WeatherConstant.CLOUDCOVER_LOWER_LIMIT && dp.getMean() < WeatherConstant.CLOUDCOVER_UPPER_LIMIT) {
	                ai.setCloudCover(dp);
	                ai.setLastUpdateTime(System.currentTimeMillis());
	            }
				break;
				
			case PRECIPITATION:
				if (dp.getMean() >= WeatherConstant.PRECIPITATION_LOWER_LIMIT && dp.getMean() < WeatherConstant.PRECIPITATION_UPPER_LIMIT) {
	                ai.setPrecipitation(dp);
	                ai.setLastUpdateTime(System.currentTimeMillis());
	            }
				break;
				
			default:
				throw new WeatherException("couldn't update atmospheric data");
		}
        
        
    }

    /**
     * Add a new known airport to our list.
     *
     * @param iataCode 3 letter code
     * @param latitude in degrees
     * @param longitude in degrees
     *
     * @return the added airport
     * @throws WeatherException 
     */
    public AirportData addAirport(String iataCode, double latitude, double longitude) throws WeatherException {
        AirportData ad = new AirportData();
        try {
			airportData.add(ad);
			AtmosphericInformation ai = new AtmosphericInformation();
			atmosphericInformation.add(ai);
			ad.setIata(iataCode);
			ad.setLatitude(latitude);
			ad.setLatitude(longitude);
		} catch (Exception e) {
			throw new WeatherException("Exception while adding the Airport data");
		}
        return ad;
    }
	
    /**
     * Remove airport from the list
     *
     * @param iataCode 3 letter code
     * @param latitude in degrees
     * @param longitude in degrees
     *
     */
    public boolean deleteAirport(String iataCode) {
        final AirportData airport = WeatherService.INSTANCE.findAirportData(iataCode);
        if (airport == null) {
            return false;
        }
        return airportData.remove(airport);
    }
	

}
