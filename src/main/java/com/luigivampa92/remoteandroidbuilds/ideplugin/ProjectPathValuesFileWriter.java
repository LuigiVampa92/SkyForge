package com.luigivampa92.remoteandroidbuilds.ideplugin;

import java.io.*;
import java.util.Properties;

public final class ProjectPathValuesFileWriter {

    private static final String KEY_PROJECT_DIR = "project.dir";
    private static final String KEY_PROJECT_PATH = "project.path";

    public void write(File targetFile, ProjectPathValues projectPathValues) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("targetFile must not be null");
        }
        if (projectPathValues == null) {
            throw new IllegalArgumentException("projectPathValues must not be null");
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetFile))) {
            Properties properties = new Properties();
            properties.put(KEY_PROJECT_DIR, projectPathValues.getDir());
            properties.put(KEY_PROJECT_PATH, projectPathValues.getPath());
            properties.store(bufferedWriter, null);
        }
    }
}
