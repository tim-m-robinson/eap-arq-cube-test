package net.atos;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

@ApplicationPath("/")
@Path("/")
public class Greeter extends Application {

	@GET()
	@Path("/")
	public Response createGreeting(@QueryParam("name") String name) {
		if (name == null) {
			return Response.status(201).entity("Hello World!").build();
		} else if(name.equalsIgnoreCase("bar")) {
			return Response.status(202).entity("Your contact has been logged, " + name).build();
		} else {
			return Response.status(200).entity("Hello, " + name + "!").build();
		}
	}

}