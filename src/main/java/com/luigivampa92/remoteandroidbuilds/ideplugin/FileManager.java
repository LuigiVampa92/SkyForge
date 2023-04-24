package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Properties;

public final class FileManager {

    private static final String PROP_HOME_DIR = "user.home";
    private static final String DIR_GRADLE_CONFIG = ".gradle";
    private static final String DIR_GRADLE_REMOTE_BUILD_CONFIG = "mirakle";
    private static final String FILE_GRADLE_REMOTE_BUILD_CONFIG_PROJECT = "project.properties";
    private static final String KEY_PROJECT_DIR = "project.dir";
    private static final String KEY_PROJECT_PATH = "project.path";
    private static final String DIR_GRADLE_INIT_SCRIPTS = "init.d";
    private static final String FILE_GRADLE_INIT_SCRIPT_REMOTE_BUILDS = "mirakle.gradle";
    public static final String FILE_PREPARE_SERVER_DEPENDENCIES_SCRIPT = "prepare_android_builds.sh";
    private static final String DIR_ANDROID_CONFIG = ".android";
    private static final String FILE_ANDROID_DEBUG_KEYSTORE = "debug.keystore";


    public ProjectPathValues getMarkedProjectValues() {
        try {
            File gradleRemoteConfigPropertiesFile = getProjectPropertiesFile();
            if (gradleRemoteConfigPropertiesFile != null) {
                Properties properties = new Properties();
                try (InputStream input = Files.newInputStream(gradleRemoteConfigPropertiesFile.toPath())) {
                    properties.load(input);
                } catch (Throwable ignored) {}
                if (properties.containsKey(KEY_PROJECT_DIR) && properties.containsKey(KEY_PROJECT_PATH)) {
                    String dirValue = properties.getProperty(KEY_PROJECT_DIR);
                    String pathValue = properties.getProperty(KEY_PROJECT_PATH);
                    if (dirValue != null && !dirValue.isEmpty() && pathValue != null && !pathValue.isEmpty()) {
                        return new ProjectPathValues(dirValue, pathValue);
                    }
                }
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    public void saveMarkedProjectValues(ProjectPathValues projectPathValues) throws IOException {
        File homeDir = getUserHomeDirectory();
        if (homeDir.exists() && homeDir.isDirectory()) {
            File gradleConfigDir;
            gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
            if (!gradleConfigDir.exists()) {
                boolean gradleConfigDirCreated = gradleConfigDir.mkdir();
                if (!gradleConfigDirCreated) {
                    throw new RuntimeException("Failed to save project path values");
                }
            }
            gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
            if (gradleConfigDir.exists() && gradleConfigDir.isDirectory()) {
                File gradleRemoteConfigDir;
                gradleRemoteConfigDir = new File(gradleConfigDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
                if (!gradleRemoteConfigDir.exists()) {
                    boolean gradleRemoteConfigDirCreated = gradleRemoteConfigDir.mkdir();
                    if (!gradleRemoteConfigDirCreated) {
                        throw new RuntimeException("Failed to save project path values");
                    }
                }
                gradleRemoteConfigDir = new File(gradleConfigDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
                if (gradleRemoteConfigDir.exists() && gradleRemoteConfigDir.isDirectory()) {
                    File gradleRemoteConfigPropertiesFile;
                    gradleRemoteConfigPropertiesFile = new File(gradleRemoteConfigDir, FILE_GRADLE_REMOTE_BUILD_CONFIG_PROJECT);
                    if (gradleRemoteConfigPropertiesFile.exists()) {
                        boolean gradleRemoteConfigPropertiesFileDeleted = gradleRemoteConfigPropertiesFile.delete();
                        if (!gradleRemoteConfigPropertiesFileDeleted) {
                            throw new RuntimeException("Failed to save project path values");
                        }
                    }
                    gradleRemoteConfigPropertiesFile = new File(gradleRemoteConfigDir, FILE_GRADLE_REMOTE_BUILD_CONFIG_PROJECT);
                    boolean fileCreated = gradleRemoteConfigPropertiesFile.createNewFile();
                    if (!fileCreated) {
                        throw new RuntimeException("Failed to save project path values");
                    }
                    ProjectPathValuesFileWriter writer = new ProjectPathValuesFileWriter();
                    writer.write(gradleRemoteConfigPropertiesFile, projectPathValues);
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to save project path values");
    }

    public void deleteMarkedProjectValues() {
        try {
            File gradleRemoteConfigPropertiesFile = getProjectPropertiesFile();
            if (gradleRemoteConfigPropertiesFile != null) {
                gradleRemoteConfigPropertiesFile.delete();
            }
        } catch (Throwable e) {
        }
    }

    private File getProjectPropertiesFile() {
        try {
            File homeDir = getUserHomeDirectory();
            if (homeDir.exists() && homeDir.isDirectory()) {
                File gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
                if (gradleConfigDir.exists() && gradleConfigDir.isDirectory()) {
                    File gradleRemoteConfigDir = new File(gradleConfigDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
                    if (gradleRemoteConfigDir.exists() && gradleRemoteConfigDir.isDirectory()) {
                        File gradleRemoteConfigPropertiesFile = new File(gradleRemoteConfigDir, FILE_GRADLE_REMOTE_BUILD_CONFIG_PROJECT);
                        if (gradleRemoteConfigPropertiesFile.exists() && gradleRemoteConfigPropertiesFile.isFile() && gradleRemoteConfigPropertiesFile.canRead()) {
                            return gradleRemoteConfigPropertiesFile;
                        }
                    }
                }
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    private File getRemoteBuildsInitScriptFile() {
        try {
            File homeDir = getUserHomeDirectory();
            if (homeDir.exists() && homeDir.isDirectory()) {
                File gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
                if (gradleConfigDir.exists() && gradleConfigDir.isDirectory()) {
                    File gradleInitScriptsDir = new File(gradleConfigDir, DIR_GRADLE_INIT_SCRIPTS);
                    if (gradleInitScriptsDir.exists() && gradleInitScriptsDir.isDirectory()) {
                        File remoteBuildInitScriptFile = new File(gradleInitScriptsDir, FILE_GRADLE_INIT_SCRIPT_REMOTE_BUILDS);
                        if (remoteBuildInitScriptFile.exists() && remoteBuildInitScriptFile.isFile() && remoteBuildInitScriptFile.canRead()) {
                            return remoteBuildInitScriptFile;
                        }
                    }
                }
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    String PROJ_PREFIX = "def projectToBuildRemotely = \"";
    String PROJ_POSTFIX = "\"";

    public String getRemoteBuildsInitScriptProjectValue() {
        File initScriptFile = getRemoteBuildsInitScriptFile();
        if (initScriptFile != null && initScriptFile.exists() && initScriptFile.canRead()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(initScriptFile))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith(PROJ_PREFIX)) {
                        String substring = line.substring(PROJ_PREFIX.length());
                        String projectNameValue = substring.substring(0, substring.lastIndexOf(PROJ_POSTFIX));
                        if (!projectNameValue.isEmpty()) {
                            return projectNameValue;
                        }
                    }
                }
            }
            catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public void saveRemoteBuildsInitScriptFile(RemoteBuildsConfiguration configuration, String projectName) throws IOException {
        File homeDir = getUserHomeDirectory();
        if (homeDir.exists() && homeDir.isDirectory()) {
            File gradleConfigDir;
            gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
            if (!gradleConfigDir.exists()) {
                boolean gradleConfigDirCreated = gradleConfigDir.mkdir();
                if (!gradleConfigDirCreated) {
                    throw new RuntimeException("Failed to create init script file");
                }
            }
            gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
            if (gradleConfigDir.exists() && gradleConfigDir.isDirectory()) {
                File gradleInitScriptsDir;
                gradleInitScriptsDir = new File(gradleConfigDir, DIR_GRADLE_INIT_SCRIPTS);
                if (gradleInitScriptsDir.exists() && !gradleInitScriptsDir.isDirectory()) {
                    boolean fileDeleted = gradleInitScriptsDir.delete();
                    if (!fileDeleted) {
                        throw new RuntimeException("Failed to create init script file");
                    }
                }
                if (!gradleInitScriptsDir.exists()) {
                    boolean gradleInitScriptsDirCreated = gradleInitScriptsDir.mkdir();
                    if (!gradleInitScriptsDirCreated) {
                        throw new RuntimeException("Failed to create init script file");
                    }
                }
                gradleInitScriptsDir = new File(gradleConfigDir, DIR_GRADLE_INIT_SCRIPTS);
                if (gradleInitScriptsDir.exists() && gradleInitScriptsDir.isDirectory()) {
                    File remoteBuildsInitScriptFile;
                    remoteBuildsInitScriptFile = new File(gradleInitScriptsDir, FILE_GRADLE_INIT_SCRIPT_REMOTE_BUILDS);
                    if (remoteBuildsInitScriptFile.exists()) {
                        boolean remoteBuildsInitScriptFileDeleted = remoteBuildsInitScriptFile.delete();
                        if (!remoteBuildsInitScriptFileDeleted) {
                            throw new RuntimeException("Failed to create init script file");
                        }
                    }
                    remoteBuildsInitScriptFile = new File(gradleInitScriptsDir, FILE_GRADLE_INIT_SCRIPT_REMOTE_BUILDS);
                    boolean fileCreated = remoteBuildsInitScriptFile.createNewFile();
                    if (!fileCreated) {
                        throw new RuntimeException("Failed to create init script file");
                    }
                    InitScriptFileWriter initScriptFileWriter = new InitScriptFileWriter();
                    initScriptFileWriter.write(remoteBuildsInitScriptFile, configuration, projectName);
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to create init script file");
    }

    public void deleteRemoteBuildsInitScriptFile() {
        File initScriptFile = getRemoteBuildsInitScriptFile();
        if (initScriptFile != null && initScriptFile.exists()) {
            initScriptFile.delete();
        }
    }

    public void saveServerPrepareScriptFile(RemoteBuildsConfiguration configuration, Collection<String> serverSdkManagerDependencies) throws IOException {
        File homeDir = getUserHomeDirectory();
        if (homeDir.exists() && homeDir.isDirectory()) {
            File gradleConfigDir;
            gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
            if (!gradleConfigDir.exists()) {
                boolean gradleConfigDirCreated = gradleConfigDir.mkdir();
                if (!gradleConfigDirCreated) {
                    throw new RuntimeException("Failed to create prepare server script file");
                }
            }
            gradleConfigDir = new File(homeDir, DIR_GRADLE_CONFIG);
            if (gradleConfigDir.exists() && gradleConfigDir.isDirectory()) {
                File gradleRemoteBuildConfigDir;
                gradleRemoteBuildConfigDir = new File(gradleConfigDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
                if (gradleRemoteBuildConfigDir.exists() && !gradleRemoteBuildConfigDir.isDirectory()) {
                    boolean fileDeleted = gradleRemoteBuildConfigDir.delete();
                    if (!fileDeleted) {
                        throw new RuntimeException("Failed to create prepare server script file");
                    }
                }
                if (!gradleRemoteBuildConfigDir.exists()) {
                    boolean gradleRemoteBuildConfigDirCreated = gradleRemoteBuildConfigDir.mkdir();
                    if (!gradleRemoteBuildConfigDirCreated) {
                        throw new RuntimeException("Failed to create prepare server script file");
                    }
                }
                gradleRemoteBuildConfigDir = new File(gradleConfigDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
                if (gradleRemoteBuildConfigDir.exists() && gradleRemoteBuildConfigDir.isDirectory()) {
                    File prepareServerScriptFile;
                    prepareServerScriptFile = new File(gradleRemoteBuildConfigDir, FILE_PREPARE_SERVER_DEPENDENCIES_SCRIPT);
                    if (prepareServerScriptFile.exists()) {
                        boolean prepareServerScriptFileDeleted = prepareServerScriptFile.delete();
                        if (!prepareServerScriptFileDeleted) {
                            throw new RuntimeException("Failed to create prepare server script file");
                        }
                    }
                    prepareServerScriptFile = new File(gradleRemoteBuildConfigDir, FILE_PREPARE_SERVER_DEPENDENCIES_SCRIPT);
                    boolean fileCreated = prepareServerScriptFile.createNewFile();
                    if (!fileCreated) {
                        throw new RuntimeException("Failed to create prepare server script file");
                    }

                    PrepareUbuntu20ServerDependenciesScriptFileWriter fileWriter = new PrepareUbuntu20ServerDependenciesScriptFileWriter();
                    fileWriter.write(prepareServerScriptFile, configuration, serverSdkManagerDependencies);
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to create prepare server script file");
    }

    public String getServerPrepareScriptFilePath() {
        File homeDir = getUserHomeDirectory();
        File gradleDir = new File(homeDir, DIR_GRADLE_CONFIG);
        File configDir = new File(gradleDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
        File scriptFile = new File(configDir, FILE_PREPARE_SERVER_DEPENDENCIES_SCRIPT);
        return scriptFile.getAbsolutePath();
    }

    public void deleteServerPrepareScriptFile() {
        try {
            File homeDir = getUserHomeDirectory();
            File gradleDir = new File(homeDir, DIR_GRADLE_CONFIG);
            File configDir = new File(gradleDir, DIR_GRADLE_REMOTE_BUILD_CONFIG);
            File scriptFile = new File(configDir, FILE_PREPARE_SERVER_DEPENDENCIES_SCRIPT);
            if (scriptFile.exists() && scriptFile.isFile()) {
                scriptFile.delete();
            }
        } catch (Throwable e) {}
    }

    public String getAndroidDebugKeystoreFilePath() {
        File userHomeDirectory = getUserHomeDirectory();
        File dirAndroidConfig = new File(userHomeDirectory, DIR_ANDROID_CONFIG);
        File fileAndroidKeystore = new File(dirAndroidConfig, FILE_ANDROID_DEBUG_KEYSTORE);
        if (fileAndroidKeystore.exists() && fileAndroidKeystore.canRead()) {
            return fileAndroidKeystore.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String fixFilePathForWindowsCygwin(String path) {
        PlatformUtil.Platform platform = PlatformUtil.getCurrentPlattform();
        if (PlatformUtil.Platform.WIN.equals(platform)) {
            String windowsRootPathPrefixForCygwin = "/cygdrive";
            String windowsDiskLabelDelimeter = ":\\";
            int indexOfDiskLabel = path.indexOf(windowsDiskLabelDelimeter);
            if (indexOfDiskLabel == 1) {
                String windowsDiskLabel = path.substring(0, indexOfDiskLabel);
                String windowsPath = path.substring(indexOfDiskLabel + windowsDiskLabelDelimeter.length());
                String result = String.format("%s/%s/%s", windowsRootPathPrefixForCygwin, windowsDiskLabel.toLowerCase(), windowsPath.replace("\\", "/"));
                return result;
            }
        }
        return path;
    }

    // wsl is not supported yet
    private static final String CMD_TEMPLATE_WINDOWS_WSL = "bash -c \"%s\"";

    public String fixCommandForWindowsWsl(String command) {
        PlatformUtil.Platform platform = PlatformUtil.getCurrentPlattform();
        if (PlatformUtil.Platform.WIN.equals(platform)) {
            return String.format(CMD_TEMPLATE_WINDOWS_WSL, command);
        }
        return command;
    }

    private File getUserHomeDirectory() {
        return new File(System.getProperty(PROP_HOME_DIR));
    }
}
