package com.taxibooking.repository;

import com.taxibooking.model.Passenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * File Repository for Passenger CRUD (Component 1 - Passenger Management)
 */
@Repository
public class PassengerFileRepository extends AbstractFileRepository<Passenger, String> {

    public PassengerFileRepository(@Value("${app.data.passengers-file:data/passengers.txt}") String filePath) {
        super(filePath);
    }

    @Override
    protected String getId(Passenger entity) {
        return entity.getId();
    }

    @Override
    protected String serialize(Passenger entity) {
        return entity.toFileString();
    }

    @Override
    protected Passenger deserialize(String line) {
        return Passenger.fromFileString(line);
    }
}
