package com.crossover.trial.weather;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.service.AirportService;
import com.crossover.trial.weather.service.WeatherService;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
    public final static Logger LOGGER = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());
    
    /** Singleton pattern for Weather and Airport service class */
    static WeatherService weatherService = WeatherService.INSTANCE;
    static AirportService airportService = AirportService.INSTANCE;

    /** shared gson json to object factory */
    public final static Gson gson = new Gson();
    
    /** all known airports */
    private  List<AirportData> airportData = weatherService.getAirportData();
    

    @GET
    @Path("/ping")
    @Override
    public Response ping() {
    	LOGGER.log(Level.INFO, "call to collector ping method");
        return Response.status(Response.Status.OK).entity("ready").build();
    }

    @POST
    @Path("/weather/{iata}/{pointType}")
    @Override
    public Response updateWeather(@PathParam("iata") String iataCode,
                                  @PathParam("pointType") String pointType,
                                  String datapointJson) {
        try {
        	airportService.addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
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

    @GET
    @Path("/airports")
    @Produces(MediaType.APPLICATION_JSON)
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


    @GET
    @Path("/airport/{iata}")
    @Produces(MediaType.APPLICATION_JSON)
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

    @POST
    @Path("/airport/{iata}/{lat}/{long}")
    @Override
    public Response addAirport(@PathParam("iata") String iata,
                               @PathParam("lat") String latString,
                               @PathParam("long") String longString) {
    	try{
    		airportService.addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
    		System.out.println("Added airport: "+iata +" size: "+airportData.size());
    		LOGGER.log(Level.INFO, "Airport data added successfully");
    		return Response.status(Response.Status.CREATED).build();
    	}catch (Exception e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
        	return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }


    @DELETE
    @Path("/airport/{iata}")
    @Override
    public Response deleteAirport(@PathParam("iata") String iata) {
    	try {
            boolean deleted = airportService.deleteAirport(iata);
            return Response.status(deleted ? Response.Status.OK : Response.Status.NOT_FOUND).build();
        } catch (NumberFormatException e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    @GET
    @Path("/exit")
    @Override
    public Response exit() {
        System.exit(0);
        return Response.noContent().build();
    }
    
}
