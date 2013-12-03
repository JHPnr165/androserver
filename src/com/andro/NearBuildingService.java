package com.andro;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

@SuppressWarnings("serial")
public class NearBuildingService extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Coordinate latitude = new DegreeCoordinate(Double.parseDouble(req.getParameter("latitude")));
		Coordinate longitude = new DegreeCoordinate(Double.parseDouble(req.getParameter("longitude")));
		Point position = new Point(latitude, longitude);
		List<Entity> coordinates = new DatabaseService().getCoordinates();
		Entity nearbyBuilding = getNearbyBuilding(coordinates, position);

		if(nearbyBuilding == null) {
			resp.getWriter().write("404"); //TEST
			resp.setContentType("text/plain");
			resp.setHeader("status", "0");
			//TODO
		} else {
			resp.getWriter().write((String) nearbyBuilding.getProperty("address")); //TEST
			resp.setContentType("text/plain");
			resp.setHeader("status", "1");
			resp.setHeader("address", (String) nearbyBuilding.getProperty("address"));
			resp.setHeader("name", (String) nearbyBuilding.getProperty("name"));
			//TODO
		}
	}

	/**
	 * Method to get nearest building from database which is less than 20m from current position.
	 * 
	 * @param coordinates
	 * @param position
	 * @return
	 */
	private Entity getNearbyBuilding(List<Entity> coordinates, Point position) {
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		double bestDistance = 25;
		double tmpDistance = 0;
		Entity nearbyCoordinate = null;
		Entity nearbyBuilding = null;
		Coordinate latitude = null;
		Coordinate longitude = null;
		Point positionToCompare = null;

		if(coordinates != null) {
			for(Entity coordinate : coordinates) {
				latitude = new DegreeCoordinate(
						Double.parseDouble((String) coordinate.getProperty("latitude")));
				longitude = new DegreeCoordinate(
						Double.parseDouble((String) coordinate.getProperty("longitude")));
				positionToCompare = new Point(latitude, longitude);
				if((tmpDistance = EarthCalc.getDistance(position, positionToCompare)) < 20) {
					if(tmpDistance < bestDistance) {
						bestDistance = tmpDistance;
						nearbyCoordinate = coordinate;
					}
				}
			}
		}
		try {
			nearbyBuilding = dataStore.get(nearbyCoordinate.getParent());
		} catch (EntityNotFoundException e) {
			
		}
		return nearbyBuilding;
	}
}
