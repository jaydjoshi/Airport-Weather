/**
 * 
 */
package com.crossover.trial.weather.model;

import java.text.DecimalFormat;

/**
 * @author jdhirendrajoshi
 *
 */
public class Airport {
	
	private String airportName;
	private String city;
	private String country;
	private String iata;
	private String icao;
	private String latitude;
	private String longitude;
	private double altitude;
	private float timezone;
	private String dst;
	DecimalFormat df = new DecimalFormat("###.######");
	
	/**
	 * @return the airportName
	 */
	public String getAirportName() {
		return airportName;
	}
	/**
	 * @param airportName the airportName to set
	 */
	public void setAirportName(String airportName) {
		this.airportName = trimQuotes(airportName);
	}
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = trimQuotes(city);
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = trimQuotes(country);
	}
	/**
	 * @return the iata
	 */
	public String getIata() {
		return iata;
	}
	/**
	 * @param iata the iata to set
	 * 3-letter FAA code or IATA code (blank or "" if not assigned)
	 */
	public void setIata(String iata) {
		
		if(iata==null || iata.length()==0){
			this.iata="";
		}
		else {
			iata=trimQuotes(iata);
			if(iata.length()==3)
				this.iata = iata;
		}		
	}
	
	/**
	 * @return the icao
	 */
	public String getIcao() {
		return icao;
	}
	/**
	 * @param icao the icao to set
	 * 4-letter ICAO code (blank or "" if not assigned)
	 */
	public void setIcao(String icao) {
		if(icao==null || icao.length()==0){
			this.icao="";
		}
		else{
			icao=trimQuotes(icao);
			if(icao.length()==4)
			this.icao = icao;
		}
	}
	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}
	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	/**
	 * @return the timezone
	 */
	public float getTimezone() {
		return timezone;
	}
	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(float timezone) {
		this.timezone = timezone;
	}
	/**
	 * @return the dst
	 */
	public String getDst() {
		return dst;
	}
	/**
	 * @param dst the dst to set
	 */
	public void setDst(String dst) {
		dst = trimQuotes(dst);
		this.dst = dst;
	}
	
	String trimQuotes(String string){
		if(string!=null){
			string = string.replaceAll("^\"|\"$", "");
		}
		return string;
		
	}
	

}
