package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.SshExecutor;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisableRemoteBuildsTask extends Task.Backgroundable {

    private final RemoteBuildConfigurationService configurationService;
    private final FileManager fileManager;
    private final SshExecutor sshExecutor;
    private final AsyncSuccessCallback onSuccess;
    private final AsyncErrorCallback onError;

    public DisableRemoteBuildsTask(RemoteBuildConfigurationService configurationService, FileManager fileManager, SshExecutor sshExecutor, @Nullable Project project, AsyncSuccessCallback callbackSuccess, AsyncErrorCallback callbackError) {
        super(project, "Disable Remote Builds", false);
        this.configurationService = configurationService;
        this.fileManager = fileManager;
        this.sshExecutor = sshExecutor;
        this.onSuccess = callbackSuccess;
        this.onError = callbackError;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        if (myProject == null) {
            throw new RuntimeException("Project is not prepared");
        }

        RemoteBuildsConfiguration configuration = configurationService.getConfiguration();
        boolean sshSettingsFilled = configuration.isSshSettingsValid();
        if (!sshSettingsFilled) {
            throw new RuntimeException("SSH configuration is not set");
        }

        boolean proxySettingsValid = configuration.isProxySettingsValid();
        List<Integer> proxyPortsValues = configuration.getProxyPortsValues();
        if (configuration.isProxyRequired() && proxySettingsValid && !proxyPortsValues.isEmpty()) {
            sshExecutor.stopSshTunnelsOnPorts(proxyPortsValues);
        }

        fileManager.deleteRemoteBuildsInitScriptFile();
        fileManager.deleteMarkedProjectValues();
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
}

