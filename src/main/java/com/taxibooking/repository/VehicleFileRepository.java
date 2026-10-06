package com.taxibooking.repository;

import com.taxibooking.model.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * File Repository for Vehicle CRUD (Component 3 - Vehicle & Fleet Management)
 */
@Repository
public class VehicleFileRepository extends AbstractFileRepository<Vehicle, String> {

    public VehicleFileRepository(@Value("${app.data.vehicles-file:data/vehicles.txt}") String filePath) {
        super(filePath);
    }

    @Override
    protected String getId(Vehicle entity) {
        return entity.getVehicleId();
    }

    @Override
    protected String serialize(Vehicle entity) {
        return entity.toFileString();
    }

    @Override
    protected Vehicle deserialize(String line) {
        return Vehicle.fromFileString(line);
    }
}
