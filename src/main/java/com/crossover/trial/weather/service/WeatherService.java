/**
 * 
 */
package com.crossover.trial.weather.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.crossover.trial.weather.AirportLoader;
import com.crossover.trial.weather.exception.WeatherException;
import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;

/**
 * @author jdhirendrajoshi
 * Singleton design pattern implementation for Weather Service
 */
public enum WeatherService {
	
    INSTANCE;
	
	/** earth radius in KM */
    public final double R = 6372.8;

    /** all known airports */
    private List<AirportData> airportData = new ArrayList<AirportData>();

    /** atmospheric information for each airport, idx corresponds with airportData */
    private List<AtmosphericInformation> atmosphericInformation = new LinkedList<AtmosphericInformation>();

    /**
     * Internal performance counter to better understand most requested information, this map can be improved but
     * for now provides the basis for future performance optimizations. Due to the stateless deployment architecture
     * we don't want to write this to disk, but will pull it off using a REST request and aggregate with other
     * performance metrics {@link #ping()}
     */
    private Map<AirportData, Integer> requestFrequency = new HashMap<AirportData, Integer>();

    private Map<Double, Integer> radiusFreq = new HashMap<Double, Integer>();
    
    
    /**
	 * @return the airportData
	 */
	public List<AirportData> getAirportData() {
		return airportData;
	}

	/**
	 * @param airportData the airportData to set
	 */
	public void setAirportData(List<AirportData> airportData) {
		this.airportData = airportData;
	}

	/**
	 * @return the atmosphericInformation
	 */
	public List<AtmosphericInformation> getAtmosphericInformation() {
		return atmosphericInformation;
	}

	/**
	 * @param atmosphericInformation the atmosphericInformation to set
	 */
	public void setAtmosphericInformation(List<AtmosphericInformation> atmosphericInformation) {
		this.atmosphericInformation = atmosphericInformation;
	}

	/**
	 * @return the requestFrequency
	 */
	public Map<AirportData, Integer> getRequestFrequency() {
		return requestFrequency;
	}

	/**
	 * @param requestFrequency the requestFrequency to set
	 */
	public void setRequestFrequency(Map<AirportData, Integer> requestFrequency) {
		this.requestFrequency = requestFrequency;
	}

	/**
	 * @return the radiusFreq
	 */
	public Map<Double, Integer> getRadiusFreq() {
		return radiusFreq;
	}

	/**
	 * @param radiusFreq the radiusFreq to set
	 */
	public void setRadiusFreq(Map<Double, Integer> radiusFreq) {
		this.radiusFreq = radiusFreq;
	}

	/**
     * Records information about how often requests are made
     *
     * @param iata an iata code
     * @param radius query radius
     */
    public void updateRequestFrequency(String iata, Double radius) {
        AirportData airportData = findAirportData(iata);
        requestFrequency.put(airportData, requestFrequency.getOrDefault(airportData, 0) + 1);
        radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public AirportData findAirportData(String iataCode) {
        return airportData.stream()
            .filter(ap -> ap.getIata().equals(iataCode))
            .findFirst().orElse(null);
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public int getAirportDataIdx(String iataCode) {
        AirportData ad = findAirportData(iataCode);
        return airportData.indexOf(ad);
    }

    /**
     * Haversine distance between two airports.
     *
     * @param ad1 airport 1
     * @param ad2 airport 2
     * @return the distance in KM
     */
    public double calculateDistance(AirportData ad1, AirportData ad2) {
        double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
        double deltaLon = Math.toRadians(ad2.getLongitude() - ad1.getLongitude());
        double a =  Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
                * Math.cos(ad1.getLatitude()) * Math.cos(ad2.getLatitude());
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    /**
     * A dummy init method that loads hard coded data
     * @throws WeatherException 
     */
    public void init() throws WeatherException {
        airportData.clear();
        atmosphericInformation.clear();
        requestFrequency.clear();

       try {
			AirportService.INSTANCE.addAirport("BOS", 42.364347, -71.005181);
			AirportService.INSTANCE.addAirport("EWR", 40.6925, -74.168667);
			AirportService.INSTANCE.addAirport("JFK", 40.639751, -73.778925);
			AirportService.INSTANCE.addAirport("LGA", 40.777245, -73.872608);
			AirportService.INSTANCE.addAirport("MMU", 40.79935, -74.4148747);
		} catch (WeatherException e) {
			throw new WeatherException(e);
		}
    }
    
    
    /*public void initFromFile() throws WeatherException {
    	airportData.clear();
    	atmosphericInformation.clear();
    	requestFrequency.clear();
        
        //args = airports.dat
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("airports.dat");
        
        if(inputStream==null){
        	System.err.println("File is not a valid input");
            //System.exit(1);
        }

        try {
			upload(inputStream);
		} catch (IOException e) {
        	throw new WeatherException(e);
        } catch (NumberFormatException e) {
        	throw new WeatherException(e);
		} catch (Exception e) {
			throw new WeatherException(e);
		}
        
        //System.exit(0);
        
        
    }
    
    public void upload(InputStream airportDataStream) throws IOException, WeatherException{
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
        	populateAirportData(strArr,airport);	
        	
        	try {
				AirportService.INSTANCE.addAirport(airport.getIata(),Double.valueOf(airport.getLatitude()),Double.valueOf(airport.getLongitude()));
			} catch (NumberFormatException | WeatherException e) {
				// TODO Auto-generated catch block
				throw new WeatherException(e);
			}
        	
	        
        }
    }

    public void populateAirportData(String[] strArr, Airport airport) {
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
	}*/
}
