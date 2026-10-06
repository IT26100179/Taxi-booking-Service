package com.taxibooking.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base Abstract Class for File-Based CRUD Operations.
 * Demonstrates:
 *  - Generic Abstraction & Code Reusability
 *  - Java File I/O (BufferedReader, BufferedWriter, File streams)
 *  - Thread safety with ReentrantReadWriteLock
 *
 * All 6 group members can extend this to implement their module's File Handling in seconds!
 */
public abstract class AbstractFileRepository<T, ID> implements FileRepository<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final File file;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public AbstractFileRepository(String filePath) {
        this.file = new File(filePath);
        initFile();
    }

    private void initFile() {
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
                logger.info("Initialized storage file: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to initialize file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Extracts the unique ID from an entity.
     */
    protected abstract ID getId(T entity);

    /**
     * Serializes an entity into a delimited string for storage in .txt.
     */
    protected abstract String serialize(T entity);

    /**
     * Deserializes a text line into an entity object.
     */
    protected abstract T deserialize(String line);

    @Override
    public List<T> findAll() {
        lock.readLock().lock();
        List<T> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                T entity = deserialize(line);
                if (entity != null) {
                    list.add(entity);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading file: " + file.getName(), e);
        } finally {
            lock.readLock().unlock();
        }
        return list;
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();
        return findAll().stream()
                .filter(item -> id.equals(getId(item)))
                .findFirst();
    }

    @Override
    public T save(T entity) {
        if (entity == null) return null;
        lock.writeLock().lock();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            String line = serialize(entity);
            writer.write(line);
            writer.newLine();
            writer.flush();
            logger.info("Saved record to {}: ID={}", file.getName(), getId(entity));
            return entity;
        } catch (IOException e) {
            logger.error("Error writing to file: " + file.getName(), e);
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean update(T entity) {
        if (entity == null || getId(entity) == null) return false;
        lock.writeLock().lock();
        try {
            List<T> all = findAll();
            boolean found = false;
            for (int i = 0; i < all.size(); i++) {
                if (getId(entity).equals(getId(all.get(i)))) {
                    all.set(i, entity);
                    found = true;
                    break;
                }
            }
            if (found) {
                rewriteFile(all);
                logger.info("Updated record in {}: ID={}", file.getName(), getId(entity));
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean deleteById(ID id) {
        if (id == null) return false;
        lock.writeLock().lock();
        try {
            List<T> all = findAll();
            boolean removed = all.removeIf(item -> id.equals(getId(item)));
            if (removed) {
                rewriteFile(all);
                logger.info("Deleted record from {}: ID={}", file.getName(), id);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long count() {
        return findAll().size();
    }

    private void rewriteFile(List<T> list) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (T item : list) {
                String line = serialize(item);
                if (line != null && !line.isBlank()) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();
        } catch (IOException e) {
            logger.error("Error rewriting file: " + file.getName(), e);
        }
    }
}
