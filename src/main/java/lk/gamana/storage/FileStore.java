package lk.gamana.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class FileStore {
    private final Path dir;
    private final ReentrantLock lock = new ReentrantLock();

    public FileStore(@Value("${app.data-dir}") String dataDir) throws IOException {
        this.dir = Path.of(dataDir);
        Files.createDirectories(dir);
    }

    /** Run a block so only one thread reads and writes the files at a time. */
    public <T> T withLock(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public List<String[]> read(String file) {
        Path p = dir.resolve(file);
        List<String[]> rows = new ArrayList<>();
        try {
            if (!Files.exists(p)) return rows;
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) rows.add(line.split("\\|", -1));
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(String file, List<String[]> rows) {
        Path target = dir.resolve(file);
        Path tmp = dir.resolve(file + ".tmp");
        try {
            List<String> lines = rows.stream().map(r -> String.join("|", r)).toList();
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Remove characters that would break the file format. */
    public static String clean(String s) {
        return s == null ? "" : s.replace("|", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
