package com.crossover.trial.weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.service.WeatherService;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 *
 * TODO: Implement the Airport Loader
 * 
 * @author code test administrator
 */
public class AirportLoader {
	
	private static final String BASE_URI = "http://localhost:9090";
	
	WeatherService weatherService = WeatherService.INSTANCE;

    /** end point to supply updates */
    private WebTarget collect;

    public AirportLoader() {
        Client client = ClientBuilder.newClient();
        collect = client.target(BASE_URI).path("collect");
    }
    
    private void delete(String string) {
    	String path = "/airport/" + string;
    	
    	System.out.println(path);
    	collect.path(path).request().delete();
    	
	}

    public void upload(InputStream airportDataStream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream));
        String l = null;
        String[] strArr = null;
        Airport airport = null;
        String path = null;
        
        while ((l = reader.readLine()) != null) {
            //break;
        	strArr = new String[11];
        	airport = new Airport();
        	strArr = l.split(",");
        	populateAirport(strArr,airport);	
        	
        	path = "/airport/" + airport.getIata() + "/" + airport.getLatitude() + "/" + airport.getLongitude();
        	
        	System.out.println(path);
        	collect.path(path).request().post(Entity.entity("", MediaType.APPLICATION_JSON));
	        
        }
    }

    private void populateAirport(String[] strArr, Airport airport) {
		// TODO Auto-generated method stub
    	if(strArr==null || strArr.length==0)
    		return;
    	
    	airport.setAirportName(strArr[1]);
    	airport.setCity(strArr[2]);
    	airport.setCountry(strArr[3]);
    	airport.setIata(strArr[4]);
    	airport.setIcao(strArr[5]);
    	airport.setLatitude(strArr[6]);
    	airport.setLongitude(strArr[7]);
    	airport.setAltitude(Double.valueOf(strArr[8]));
    	airport.setTimezone(Float.valueOf(strArr[9]));
    	airport.setDst(strArr[10]);
    	
		return;
	}

	public static void main(String args[]) throws IOException{
                
        //args is filename = airports.dat
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(args[0]);
        
        if(inputStream==null){
        	System.err.println(args[0] + " is not a valid input");
            System.exit(1);
        }

        AirportLoader al = new AirportLoader();
        
        al.upload(inputStream);
        al.delete("BOS");
        System.exit(0);
    }

	
}
