package com.andro;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

public class DatabaseService {

	protected List<Entity> getBuildings(String address) {
		List<Entity> buildings = null;
		Filter addressFilter = new FilterPredicate("address", FilterOperator.EQUAL, address);
		Query query = new Query("building");

		if(address != null) {
			query = query.setFilter(addressFilter);
		}

		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		buildings = dataStore.prepare(query).asList(FetchOptions.Builder.withLimit(999));
		return buildings;
	}

	/**
	 * Method to get all Entity objects from database of kind "coordinate".
	 * 
	 * @return
	 */
	protected List<Entity> getCoordinates() {
		List<Entity> coordinates = null;
		Query query = new Query("coordinate");
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		coordinates = dataStore.prepare(query).asList(FetchOptions.Builder.withLimit(999));
		return coordinates;
	}

	/**
	 * Method to add new building to database. Will check if building exists or not also.
	 * To update coordinates in databse then this method is used also.
	 * 
	 * @param lat Latitude.
	 * @param lng Longitude.
	 * @param name Name of the building.
	 * @param address Address of the building.
	 */
	protected void saveBuilding(String lat, String lng, String name, String address) {
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("building", address);
		Entity buildingToSave = null;
		try {
			buildingToSave = dataStore.get(key);
		} catch(EntityNotFoundException e) {
			buildingToSave = new Entity("building", address);
		}

		buildingToSave.setProperty("name", name);
		buildingToSave.setProperty("address", address);
		dataStore.put(buildingToSave);
		updateCoordinates(address, lat, lng, key);
	}

	/**
	 * Method to add new coordinates to database. Checks if it's necessary to save
	 * new coordinate to database or not. If nearest coordinate is closer than
	 * 5m then it'll not save new coordinate.
	 * 
	 * @param address Address of the building in given coordinates.
	 * @param lat Latitude.
	 * @param lng Longitude.
	 * @param key Key of parent.
	 */
	private void updateCoordinates(String address, String lat, String lng, Key key) {
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> coordinates = getCoordinates(address, dataStore);
		Point position = getCurrentPosition(lat, lng);
		Coordinate latitude = null;
		Coordinate longitude = null;
		Point positionToCompare = null;
		@SuppressWarnings("unused")
		Double distance;
		boolean save = true;

		for(Entity coordinate : coordinates) {
			latitude = new DegreeCoordinate(
					Double.parseDouble((String) coordinate.getProperty("latitude")));
			longitude = new DegreeCoordinate(
					Double.parseDouble((String) coordinate.getProperty("longitude")));
			positionToCompare = new Point(latitude, longitude);
			if((distance = EarthCalc.getDistance(position, positionToCompare)) < 5) {
				save = false;
				break;
			}
		}

		if(save) {
			saveCoordinate(lat, lng, address, key, dataStore);
		}
	}

	/**
	 * Method to create and save new Entity to database. Entity is kind of "coordinate"
	 * and is child of given Entity of kind "building".
	 * 
	 * @param lat Latitude.
	 * @param lng Longitude.
	 * @param address Address of the building in given coordinates.
	 * @param key Key of parent.
	 * @param dataStore Datastore object.
	 */
	private void saveCoordinate(String lat, String lng, String address, Key key, DatastoreService dataStore) {
		Entity coordinateToSave = new Entity("coordinate", key);
		coordinateToSave.setProperty("latitude", lat);
		coordinateToSave.setProperty("longitude", lng);
		coordinateToSave.setProperty("address", address);
		dataStore.put(coordinateToSave);
	}

	/**
	 * Method to make Point object from coordinates.
	 * 
	 * @param lat Latitude.
	 * @param lng Longitude.
	 * @return Point object from coordinates.
	 */
	private Point getCurrentPosition(String lat, String lng) {
		Coordinate Latitude = new DegreeCoordinate(Double.parseDouble(lat));
		Coordinate Longitude = new DegreeCoordinate(Double.parseDouble(lng));
		Point position = new Point(Latitude, Longitude);
		return position;
	}

	/**
	 * Method to get given address coordinates from database.
	 * 
	 * @param address Address which coordinates to get.
	 * @param dataStore Datastore object.
	 * @return Given address coordinates from database.
	 */
	private List<Entity> getCoordinates(String address, DatastoreService dataStore) {
		Filter addressFilter = new FilterPredicate("address", FilterOperator.EQUAL, address);
		Query query = new Query("coordinate").setFilter(addressFilter);
		List<Entity> coordinates = dataStore.prepare(query).asList(FetchOptions.Builder.withLimit(20));
		return coordinates;
	}
}
