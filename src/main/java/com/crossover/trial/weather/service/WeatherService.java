/**
 * 
 */
package com.crossover.trial.weather.service;

import static com.crossover.trial.weather.RestWeatherCollectorEndpoint.addAirport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.crossover.trial.weather.exception.WeatherException;
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
			addAirport("BOS", 42.364347, -71.005181);
			addAirport("EWR", 40.6925, -74.168667);
			addAirport("JFK", 40.639751, -73.778925);
			addAirport("LGA", 40.777245, -73.872608);
			addAirport("MMU", 40.79935, -74.4148747);
		} catch (WeatherException e) {
			throw new WeatherException(e);
		}
    }
}
