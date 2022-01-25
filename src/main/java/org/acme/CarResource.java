package org.acme;

import java.util.List;
import java.util.UUID;

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
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("/cars")
public class CarResource {

    @Channel("requests")
    Emitter<String> myEmitter;

    @GET
    public List<Car> list() {
        return Car.listAll();
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

    @GET
    @Path("/{brand}")
    public List<Car> getByBrand(@PathParam("brand") String brand) {
        return Car.listByBrand(brand);
    }

    @POST
    @Path("/request")
    @Produces(MediaType.TEXT_PLAIN)
    public String createRequest() {
        UUID uuid = UUID.randomUUID();
        myEmitter.send(uuid.toString());
        return uuid.toString() + " booking added.";
    }
}