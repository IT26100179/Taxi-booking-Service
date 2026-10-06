package com.taxibooking.repository;

import com.taxibooking.model.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * File Repository for Driver CRUD (Component 2 - Driver Management)
 */
@Repository
public class DriverFileRepository extends AbstractFileRepository<Driver, String> {

    public DriverFileRepository(@Value("${app.data.drivers-file:data/drivers.txt}") String filePath) {
        super(filePath);
    }

    @Override
    protected String getId(Driver entity) {
        return entity.getId();
    }

    @Override
    protected String serialize(Driver entity) {
        return entity.toFileString();
    }

    @Override
    protected Driver deserialize(String line) {
        return Driver.fromFileString(line);
    }
}
