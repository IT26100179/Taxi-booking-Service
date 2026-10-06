package com.taxibooking.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic File Repository Interface (OOP Abstraction)
 * Defines the contract for all CRUD operations using File Handling.
 *
 * @param <T>  Entity Type (e.g., Passenger, Driver, Vehicle, Booking, Payment, Review)
 * @param <ID> Primary Identifier Type (String)
 */
public interface FileRepository<T, ID> {

    /**
     * Retrieves all records from the text file.
     */
    List<T> findAll();

    /**
     * Finds a single record by its unique ID.
     */
    Optional<T> findById(ID id);

    /**
     * Appends a new record to the file (Create).
     */
    T save(T entity);

    /**
     * Updates an existing record in the file (Update).
     */
    boolean update(T entity);

    /**
     * Deletes a record by ID from the file (Delete).
     */
    boolean deleteById(ID id);

    /**
     * Returns total count of records.
     */
    long count();
}
