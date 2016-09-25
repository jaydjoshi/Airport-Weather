package com.crossover.trial.weather;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.service.WeatherService;
import com.google.gson.Gson;

public class AirportWeatherEndpointTest {

	
	private WeatherQueryEndpoint _query = new RestWeatherQueryEndpoint();

    private WeatherCollectorEndpoint _collect = new RestWeatherCollectorEndpoint();

    private Gson _gson = new Gson();

    private DataPoint _dp;

	@Before
	public void setUp() throws Exception {

        WeatherService.INSTANCE.initFromFile();
        _dp = new DataPoint.Builder()
                .withCount(10).withFirst(10).withMedian(20).withLast(30).withMean(22).build();
        _collect.updateWeather("BOS", "wind", _gson.toJson(_dp));
        _query.weather("BOS", "0").getEntity();
    
	}

	@Test
	public void testInitFromFileSize() throws Exception {
		
		Set<String> retval = (Set<String>) _collect.getAirports().getEntity();
		
		assertEquals(10, retval.size());
	}
	
	
	@Test
	public void testDeleteAirport() throws Exception {
		
		Response response =(Response) _collect.deleteAirport("BOS");
		
		assertEquals(200, response.getStatus());
		
		response =(Response) _collect.deleteAirport("BAS");
		
		assertEquals(404, response.getStatus());
		
		
	}
	
	@Test
	public void testAddAirport() throws Exception {
		
		Response response =(Response) _collect.addAirport("BOM", "19.0953", "72.853");
		
		assertEquals(201, response.getStatus());
		
		response =(Response) _collect.addAirport("DEL", "Lat", "Lon");
		
		assertEquals(406, response.getStatus());
		
	}
	
	@Test
	public void testDeleteAllAirport() throws Exception {
		
		 Set<String> res = (Set<String>) _collect.getAirports().getEntity();
				
		for (String iata : res) {
			_collect.deleteAirport(iata);
		}

		Response response =  _collect.getAirports();
		assertEquals(404, response.getStatus());
		
	}
	
	
}