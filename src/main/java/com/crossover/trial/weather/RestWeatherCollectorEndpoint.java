package com.crossover.trial.weather;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.service.WeatherService;
import com.crossover.trial.weather.util.WeatherConstant;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.google.gson.Gson;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.crossover.trial.weather.RestWeatherQueryEndpoint.*;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
    public final static Logger LOGGER = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());
    
    /** Singleton pattern for Weather service class */
    static WeatherService weatherService = WeatherService.INSTANCE;

    /** shared gson json to object factory */
    public final static Gson gson = new Gson();
    
    /** all known airports */
    private static List<AirportData> airportData = weatherService.getAirportData();

    /** atmospheric information for each airport, idx corresponds with airportData */
    private static List<AtmosphericInformation> atmosphericInformation = weatherService.getAtmosphericInformation();
    

    @Override
    public Response ping() {
    	LOGGER.log(Level.INFO, "call to collector ping method");
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    @Override
    public Response updateWeather(@PathParam("iata") String iataCode,
                                  @PathParam("pointType") String pointType,
                                  String datapointJson) {
        try {
            addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
            return Response.status(Response.Status.OK).build();
        } catch (WeatherException e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        catch (Exception e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        
    }


    @Override
    public Response getAirports() {
        Set<String> retval = null;
        if(airportData!=null && airportData.size()>0){
        	retval = new HashSet<String>();
        }
        for (AirportData ad : airportData) {
            retval.add(ad.getIata());
        }
        if(retval != null && retval.size()>0){
        	LOGGER.log(Level.INFO, "Airport data found");
        	return Response.status(Response.Status.OK).entity(retval).build();
        }
        else{
        	LOGGER.log(Level.INFO, "Airport data not found");
        	return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @Override
    public Response getAirport(@PathParam("iata") String iata) {
        AirportData ad = weatherService.findAirportData(iata);
        if(ad!=null ){
        	LOGGER.log(Level.INFO, "Airport data for "+iata+" found");
        	return Response.status(Response.Status.OK).entity(ad).build();
        }
        else{
        	LOGGER.log(Level.INFO, "Airport data for "+iata+" not found");
        	return Response.status(Response.Status.NOT_FOUND).build();
        }
        
    }


    @Override
    public Response addAirport(@PathParam("iata") String iata,
                               @PathParam("lat") String latString,
                               @PathParam("long") String longString) {
    	try{
    		addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
    		LOGGER.log(Level.INFO, "Airport data added successfully");
    		return Response.status(Response.Status.CREATED).build();
    	}catch (Exception e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
        	return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }


    @Override
    public Response deleteAirport(@PathParam("iata") String iata) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response exit() {
        System.exit(0);
        return Response.noContent().build();
    }
    //
    // Internal support methods
    //

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
	        int airportDataIdx = weatherService.getAirportDataIdx(iataCode);
	        AtmosphericInformation ai = atmosphericInformation.get(airportDataIdx);
	        updateAtmosphericInformation(ai, pointType, dp);
	    }catch (WeatherException e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WeatherException(DataPointType.valueOf(pointType.toUpperCase()), e.getMessage(), e);
        }
        catch (Exception e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
    public static AirportData addAirport(String iataCode, double latitude, double longitude) throws WeatherException {
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
}
