package org.acme;

import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

public class Car extends PanacheMongoEntity {

    public String brand;
    public String model;

    public static List<Car> listByBrand(String brand){
        return Car.find("brand = ?1", brand).list();
    }
}