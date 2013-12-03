package com.andro;

import java.io.IOException;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class SaveBuildingService extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {		
		String lat = req.getParameter("latitude");
		String lng = req.getParameter("longitude");
		String name = req.getParameter("name");
		String address = req.getParameter("address");
		DatabaseService database = new DatabaseService();
		database.saveBuilding(lat, lng, name, address);
	}
}
