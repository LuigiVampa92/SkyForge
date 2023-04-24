package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;

import java.io.*;

public final class InitScriptFileWriter {

    private static final String TEMPLATE_FILE_INIT_SCRIPT = "initScript.template";
    private static final String TEMPLATE_VALUE_PROJECT_NAME = "$ROOT_PROJECT_NAME";
    private static final String TEMPLATE_VALUE_SSH_ALIAS = "$SERVER_SSH_ALIAS";
    private String preparedTemplate = null;

    public InitScriptFileWriter() throws IOException {
        prepareTemplate();
    }

    private void prepareTemplate() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream templateFileInputStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE_INIT_SCRIPT)) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(templateFileInputStream))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
        }
        String preparedTemplate = stringBuilder.toString();
        if (!preparedTemplate.isEmpty()) {
            this.preparedTemplate = preparedTemplate;
        } else {
            throw new FileNotFoundException("Init script template is empty");
        }
    }

    private String renderTemplate(RemoteBuildsConfiguration configuration, String projectName) {
        if (preparedTemplate == null) {
            throw new IllegalStateException("Template wasn't prepared");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("projectName must not be null");
        }

        String result = new String(preparedTemplate);
        result = result.replace(TEMPLATE_VALUE_PROJECT_NAME, projectName);
        result = result.replace(TEMPLATE_VALUE_SSH_ALIAS, configuration.getSshAlias());
        return result;
    }

    public final void write(File targetFile, RemoteBuildsConfiguration configuration, String projectName) throws IOException {
        if (preparedTemplate == null) {
            throw new IllegalStateException("Template wasn't prepared");
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("targetFile must not be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("projectName must not be null");
        }
        String fileContent = renderTemplate(configuration, projectName);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetFile))) {
            bufferedWriter.write(fileContent);
        }
    }
}
