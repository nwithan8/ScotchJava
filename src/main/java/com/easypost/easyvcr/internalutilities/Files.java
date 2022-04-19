package com.easypost.easyvcr.internalutilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Files {
    public static void createFileIfNotExists(String filePath) throws IOException {
        try {

            File file = new File(filePath);
            File parentFolder = file.toPath().getParent().toFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            file.createNewFile(); // if file already exists will do nothing
        } catch (Exception ignored) {
            throw new IOException("Could not create file");
        }
    }

    public static String readFile(File file) {
        List<String> data = new ArrayList<>();
        try {
            data = java.nio.file.Files.readAllLines(file.toPath());
        } catch (IOException ignored) {
            return null;
        }
        if (data.isEmpty()) {
            return null;
        }
        StringBuilder contents = new StringBuilder();
        for (String line : data) {
            contents.append(line);
        }
        return contents.toString();
    }

    public static String readFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;  // file doesn't exist
        }
        return readFile(file);
    }

    public static void writeFile(String filePath, String string) {
        try {
            createFileIfNotExists(filePath);
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(string);
            myWriter.close();
        } catch (IOException ignored) {
            System.out.println("An error occurred while writing to the file.");
        }
    }
}
