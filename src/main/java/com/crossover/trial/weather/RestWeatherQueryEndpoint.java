package com.crossover.trial.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.service.WeatherService;
import com.google.gson.Gson;

/**
 * The Weather App REST endpoint allows clients to query, update and check health stats. Currently, all data is
 * held in memory. The end point deploys to a single container
 *
 * @author code test administrator
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

    public final static Logger LOGGER = Logger.getLogger(RestWeatherQueryEndpoint.class.getName());
    
    /** Singleton pattern for Weather service class
     */
    static WeatherService weatherService = WeatherService.INSTANCE;

    /** shared gson json to object factory */
    public static final Gson gson = new Gson();
    
    
    /** all known airports */
    private List<AirportData> airportData = weatherService.getAirportData();

    /** atmospheric information for each airport, idx corresponds with airportData */
    private List<AtmosphericInformation> atmosphericInformation = weatherService.getAtmosphericInformation();

    /**
     * Internal performance counter to better understand most requested information, this map can be improved but
     * for now provides the basis for future performance optimizations. Due to the stateless deployment architecture
     * we don't want to write this to disk, but will pull it off using a REST request and aggregate with other
     * performance metrics {@link #ping()}
     */
    private Map<AirportData, Integer> requestFrequency = weatherService.getRequestFrequency();

    private Map<Double, Integer> radiusFreq = weatherService.getRadiusFreq();

  
    static {
        try {
        	weatherService.init();
		} catch (WeatherException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    }
    /**
     * Retrieve service health including total size of valid data points and request frequency information.
     *
     * @return health stats for the service as a string
     */
    @GET
    @Path("/ping")
    @Override
    public String ping() {
        Map<String, Object> retval = new HashMap<String, Object>();
        LOGGER.log(Level.INFO, "call to query ping method");
        
        int datasize = 0;
        for (AtmosphericInformation ai : atmosphericInformation) {
            // we only count recent readings
            if (ai.getCloudCover() != null
                || ai.getHumidity() != null
                || ai.getPressure() != null
                || ai.getPrecipitation() != null
                || ai.getTemperature() != null
                || ai.getWind() != null) {
                // updated in the last day
                if (ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000) {
                    datasize++;
                }
            }
        }
        retval.put("datasize", datasize);

        Map<String, Double> freq = new HashMap<String, Double>();
        // fraction of queries
        for (AirportData data : airportData) {
            double frac = (double)requestFrequency.getOrDefault(data, 0) / requestFrequency.size();
            freq.put(data.getIata(), frac);
        }
        retval.put("iata_freq", freq);

        int m = radiusFreq.keySet().stream()
                .max(Double::compare)
                .orElse(1000.0).intValue() + 1;

        int[] hist = new int[m];
        for (Map.Entry<Double, Integer> e : radiusFreq.entrySet()) {
            int i = e.getKey().intValue() % 10;
            hist[i] += e.getValue();
        }
        retval.put("radius_freq", hist);

        return gson.toJson(retval);
    }

    /**
     * Given a query in json format {'iata': CODE, 'radius': km} extracts the requested airport information and
     * return a list of matching atmosphere information.
     *
     * @param iata the iataCode
     * @param radiusString the radius in km
     *
     * @return a list of atmospheric information
     */
    @GET
    @Path("/weather/{iata}/{radius}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response weather(String iata, String radiusString) {
    	
    	//check if airport exists
    	AirportData airport = weatherService.findAirportData(iata);
        if (airport == null) {
        	LOGGER.log(Level.INFO, "call to query weather method returned no airport data for "+iata);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    	
        double radius = radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);
        weatherService.updateRequestFrequency(iata, radius);

        List<AtmosphericInformation> retval = new ArrayList<AtmosphericInformation>();
        
        if (radius == 0) {
            int idx = weatherService.getAirportDataIdx(iata);
            retval.add(weatherService.getAtmosphericInformation().get(idx));
        } else {
            AirportData ad = weatherService.findAirportData(iata);
            for (int i=0;i< airportData.size(); i++){
                if (weatherService.calculateDistance(ad, airportData.get(i)) <= radius){
                    AtmosphericInformation ai = atmosphericInformation.get(i);
                    if (ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPrecipitation() != null
                       || ai.getPressure() != null || ai.getTemperature() != null || ai.getWind() != null){
                        retval.add(ai);
                    }
                }
            }
        }
        
        if(retval!=null && retval.size()>0){
	        LOGGER.log(Level.INFO, "call to query weather method returned details for "+iata);
	        return Response.status(Response.Status.OK).entity(retval).build();
        }
        else{
        	LOGGER.log(Level.INFO, "call to query weather method returned no results");
	        return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


}
