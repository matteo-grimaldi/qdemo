package org.acme;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;

@Path("/cars")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarResource {
    
    @GET
    public List<PanacheMongoEntityBase> list() {
        return Car.listAll();
    }

    @GET
    @Path("/{brand}")
    public List<Car> getByBrand(@PathParam("brand") String brand) {
        return Car.listByBrand(brand);
    }

    @POST
    public Car create(Car car) {
        car.persist();
        return car;
    }

    @PUT
    public Car update(Car car) {
        car.update();
        return car;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) {
        Car car = Car.findById(new ObjectId(id));
        if(car == null) {
            throw new NotFoundException();
        }
        car.delete();
    }

}