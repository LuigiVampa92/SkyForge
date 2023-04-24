package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;

import java.io.*;
import java.util.Collection;
import java.util.stream.Collectors;

public final class PrepareUbuntu20ServerDependenciesScriptFileWriter {

    public static final String CONST_LOG_LINE = "RBSLOG_LINE";
    public static final int CONST_EXIT_CODE_INVALID_PASSWORD = 101;

    private static final String TEMPLATE_FILE_PREPARE_SERVER_SCRIPT = "serverPrepare_Ubuntu_20.template";
    private static final String STUB_SERVER_SDK_MANAGER_DEPENDENCIES = "declare -a ANDROID_SDK_DEPENDENCIES=(\"PLACEHOLDER_FOR_ANDROID_SDK_DEPENDENCIES\")";
    private static final String TEMPLATE_SERVER_SDK_MANAGER_DEPENDENCIES = "declare -a ANDROID_SDK_DEPENDENCIES=(%s)";
    private static final String PLACEHOLDER_SERVER_DEP_JDK = "PLACEHOLDER_FOR_DEPENDENCY_JDK";
    private static final String PLACEHOLDER_SERVER_USER_DIR = "PLACEHOLDER_FOR_USER_DIR";
    private static final String PLACEHOLDER_SERVER_USER_PASSWORD = "PLACEHOLDER_FOR_USER_PASSWORD";
    private String preparedTemplate = null;

    public PrepareUbuntu20ServerDependenciesScriptFileWriter() throws IOException {
        prepareTemplate();
    }

    private void prepareTemplate() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream templateFileInputStream = getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE_PREPARE_SERVER_SCRIPT)) {
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
            throw new FileNotFoundException("Prepare server script template is empty");
        }
    }

    private String renderTemplate(RemoteBuildsConfiguration configuration, Collection<String> serverSdkManagerDependencies) {
        if (preparedTemplate == null) {
            throw new IllegalStateException("Template wasn't prepared");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        if (serverSdkManagerDependencies == null || serverSdkManagerDependencies.isEmpty()) {
            throw new IllegalArgumentException("server sdkmanager dependencies are empty");
        }

        String result = new String(preparedTemplate);
        result = result.replace(STUB_SERVER_SDK_MANAGER_DEPENDENCIES, prepareSdkManagerDependenciesLine(serverSdkManagerDependencies));
        result = result.replace(PLACEHOLDER_SERVER_DEP_JDK, getJdkDepValue());
        result = result.replace(PLACEHOLDER_SERVER_USER_DIR, getUserDirValue(configuration));
        result = result.replace(PLACEHOLDER_SERVER_USER_PASSWORD, configuration.getSshUserPassword());
        return result;
    }

    private String prepareSdkManagerDependenciesLine(Collection<String> serverSdkManagerDependencies) {
        String joinedValues = serverSdkManagerDependencies.stream()
                .map(s -> String.format("\"%s\"", s))
                .collect(Collectors.joining(" "));
        return String.format(TEMPLATE_SERVER_SDK_MANAGER_DEPENDENCIES, joinedValues);
    }

    private String getUserDirValue(RemoteBuildsConfiguration configuration) {
        String user = configuration.getSshUserName();
        if ("root".equals(user)) {
            return "/root";
        } else {
            return "/home/$USER";
        }
    }

    private String getJdkDepValue() {
        return "openjdk-11-jdk";  // todo ?
    }

    public final void write(File targetFile, RemoteBuildsConfiguration configuration, Collection<String> serverSdkManagerDependencies) throws IOException {
        if (preparedTemplate == null) {
            throw new IllegalStateException("Template wasn't prepared");
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("targetFile must not be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        if (serverSdkManagerDependencies == null || serverSdkManagerDependencies.isEmpty()) {
            throw new IllegalArgumentException("server sdkmanager dependencies are empty");
        }
        String fileContent = renderTemplate(configuration, serverSdkManagerDependencies);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetFile))) {
            bufferedWriter.write(fileContent);
        }
    }
}

