package dao;

import org.apache.commons.math3.linear.RealVector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileSystemVectorXDao implements VectorXDao {

    private final Path path;

    public FileSystemVectorXDao(int variant) {
        this.path = Paths.get(System.getProperty("user.home"))
                .resolve("Documents")
                .resolve(String.format("Sa_Lab_2_var_%d", variant));
        createFile();
    }

    @Override
    public void write(int iterationStep, RealVector x) {
        try {
            String s = (Files.size(path) == 0) ? getHeader(x.getDimension()) : getRow(iterationStep, x);
            Files.write(path, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRow(int iterationStep, RealVector x) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(iterationStep).append("\t");
        for (int i = 0; i < x.getDimension(); i++) {
            sb.append(x.getEntry(i)).append("\t");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    private String getHeader(int length) {
        StringBuilder sb = new StringBuilder("k\t\t");
        for (int i = 0; i < length; i++) {
            sb.append("x_").append(i + 1).append("\t\t\t");
        }
        sb.delete(sb.length() - 3, sb.length());
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public void clear() {
        try {
            Files.writeString(path, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createFile() {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
