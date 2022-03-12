/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.ISIS_Capitalist_Server;

/**
 *
 * @author agodfrin
 */

import com.example.ISIS_Capitalist_Server.generated.PallierType;
import com.example.ISIS_Capitalist_Server.generated.ProductType;
import com.example.ISIS_Capitalist_Server.generated.World;
import com.google.gson.Gson;
import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

@Path("generic")
public class Webservice {

    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) throws JAXBException, FileNotFoundException {
        String username = request.getHeader("X-user");
        World w = services.readWorldFromXml(username);
        //services.saveWorldToXml(username, w);
        return Response.ok(w).build();
    }

    @PUT
    @Path("product")
    public Response Product(@Context HttpServletRequest request,String body) throws FileNotFoundException, JAXBException {
        String username = request.getHeader("X-user");
        ProductType product = new Gson().fromJson(body, ProductType.class);
        return Response.ok(services.updateProduct(username, product)).build();
    }
    
    @PUT
    @Path("manager")
    public Response Manager(@Context HttpServletRequest request, String body) throws FileNotFoundException {
        String username = request.getHeader("X-user");
        PallierType manager = new Gson().fromJson(body, PallierType.class);
        return Response.ok(services.updateManager(username, manager)).build();
    }
    
}
