package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.SshExecutor;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EnableRemoteBuildsTask extends Task.Backgroundable {

    private final RemoteBuildConfigurationService configurationService;
    private final FileManager fileManager;
    private final SshExecutor sshExecutor;
    private final AsyncSuccessCallback onSuccess;
    private final AsyncErrorCallback onError;

    public EnableRemoteBuildsTask(RemoteBuildConfigurationService configurationService, FileManager fileManager, SshExecutor sshExecutor, @Nullable Project project, AsyncSuccessCallback callbackSuccess, AsyncErrorCallback callbackError) {
        super(project, "Enable Remote Builds", false);
        this.configurationService = configurationService;
        this.fileManager = fileManager;
        this.sshExecutor = sshExecutor;
        this.onSuccess = callbackSuccess;
        this.onError = callbackError;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        RemoteBuildsConfiguration configuration = configurationService.getConfiguration();

        if (myProject == null) {
            throw new RuntimeException("Project is not prepared");
        }

        boolean rsyncExists = sshExecutor.checkRsyncExists();
        if (!rsyncExists) {
            throw new RuntimeException("Rsync executable is not available");
        }

        boolean proxySettingsValid = configuration.isProxySettingsValid();
        if (!proxySettingsValid) {
            throw new RuntimeException("Proxy configuration is invalid");
        }

        boolean sshSettingsFilled = configuration.isSshSettingsValid();
        if (!sshSettingsFilled) {
            throw new RuntimeException("SSH configuration is not set or invalid");
        }
        boolean sshExists = sshExecutor.checkSshExists();
        if (!sshExists) {
            throw new RuntimeException("SSH executable not available");
        }
        boolean sshConnectionAvailable = sshExecutor.checkSshConnection(configuration.getSshAlias());
        if (!sshConnectionAvailable) {
            throw new RuntimeException("Failed to connect to build server");
        }


        ProjectPathValues currentProjectPathValues = getCurrentProjectPathValues(myProject);

        boolean projectPropertiesOnServerPrepared = sshExecutor.prepareLocalPropertiesOnServer(configuration.getSshAlias(), configuration.getSshUserName(), currentProjectPathValues.getDir());
        if (!projectPropertiesOnServerPrepared) {
            throw new RuntimeException("Failed to setup project properties on build server");
        }

        boolean keystorePrepared = sshExecutor.uploadDebugKeystoreToServer(configuration.getSshAlias(), configuration.getSshUserName());
        if (!keystorePrepared) {
            throw new RuntimeException("Failed to setup keystore on build server");
        }

        List<Integer> proxyPortsValues = configuration.getProxyPortsValues();
        if (configuration.isProxyRequired() && proxySettingsValid && !proxyPortsValues.isEmpty()) {
            boolean closedPortsSuccessfully = sshExecutor.stopSshTunnelsOnPorts(proxyPortsValues);
            if (!closedPortsSuccessfully) {
                throw new RuntimeException("Failed to close previously started proxy SSH tunnels");
            }
            for (Integer tunnelProxyPort : proxyPortsValues) {
                boolean startedTunnel = sshExecutor.startSshTunnelOnPort(configuration.getSshAlias(), tunnelProxyPort);
                if (!startedTunnel) {
                    throw new RuntimeException("Failed to start proxy SSH tunnel on port " + String.valueOf(tunnelProxyPort));
                }
            }
        }

        fileManager.deleteMarkedProjectValues();
        try {
            fileManager.saveMarkedProjectValues(currentProjectPathValues);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist project data");
        }

        fileManager.deleteRemoteBuildsInitScriptFile();
        try {
            fileManager.saveRemoteBuildsInitScriptFile(configuration, currentProjectPathValues.getDir());
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist gradle init script");
        }
    }

    @Override
    public void onSuccess() {
        if (onSuccess != null) {
            onSuccess.onSuccess();
        }
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        if (onError != null) {
            onError.onError(error);
        }
    }

    // todo DUPLICATE
    private ProjectPathValues getCurrentProjectPathValues(Project project) {
        if (project != null) {
            String projectPath = project.getBaseDir().getPath();
            String projectDir = new File(projectPath).getName();
            return new ProjectPathValues(projectDir, projectPath);
        } else {
            return null;
        }
    }
}
