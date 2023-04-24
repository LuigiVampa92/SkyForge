package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.RcOnlyShellExecutor;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.ShellExecutionResult;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.SshExecutor;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;

public final class PrepareServerDependenciesBackgroundTask extends Task.Backgroundable {

    private final RemoteBuildConfigurationService configurationService;
    private final RemoteBuildsStateManager stateManager;
    private final SshExecutor sshExecutor;
    private final NotificationManager notificationManager;
    private final FileManager fileManager;
    private final Collection<String> serverSdkManagerDependencies;
    private final RcOnlyShellExecutor shellExecutor;
    private volatile Process prepareServerProcess = null;

    public PrepareServerDependenciesBackgroundTask(@Nullable Project project, RemoteBuildConfigurationService configurationService, RemoteBuildsStateManager stateManager, SshExecutor sshExecutor, NotificationManager notificationManager, FileManager fileManager, Collection<String> serverSdkManagerDependencies) {
        super(project, "Prepare server for android builds", false);
        this.configurationService = configurationService;
        this.stateManager = stateManager;
        this.sshExecutor = sshExecutor;
        this.notificationManager = notificationManager;
        this.fileManager = fileManager;
        this.serverSdkManagerDependencies = serverSdkManagerDependencies;
        this.shellExecutor = new RcOnlyShellExecutor();
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        if (RemoteBuildsModeState.PENDING.equals(stateManager.getCurrentState())) {
            return;
        }

        stateManager.setLongBackgroundTaskRunningState(true);
        progressIndicator.setIndeterminate(true);
        try {
            RemoteBuildsConfiguration configuration = configurationService.getConfiguration();
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

            fileManager.saveServerPrepareScriptFile(configuration, serverSdkManagerDependencies);

            String scriptLocalPath = fileManager.getServerPrepareScriptFilePath();
            String scriptFileNameOnServer = FileManager.FILE_PREPARE_SERVER_DEPENDENCIES_SCRIPT;

            String pushScriptToServerCommandTemplate = "scp %s %s:~/%s";
            String osAwarePath = FileManager.fixFilePathForWindowsCygwin(scriptLocalPath);
            String pushScriptToServer = String.format(pushScriptToServerCommandTemplate, osAwarePath, configuration.getSshAlias(), scriptFileNameOnServer);
            ShellExecutionResult pushResult = shellExecutor.execute(pushScriptToServer, 6000);
            if (pushResult.getExitCode() != 0) {
                throw new RuntimeException("Failed to push setup script to the server");
            }

            fileManager.deleteServerPrepareScriptFile();

            String chmodCommandTemplate = "ssh %s chmod +x ~/%s";
            String chmodCommand = String.format(chmodCommandTemplate, configuration.getSshAlias(), scriptFileNameOnServer);
            ShellExecutionResult chmodResult = shellExecutor.execute(chmodCommand, 4500);
            if (chmodResult.getExitCode() != 0) {
                throw new RuntimeException("Failed to set file parameters to the script on the server");
            }

            Runtime runtime = Runtime.getRuntime();
            String prepareServerTemplate = "ssh -tt %s bash ~/%s";
            prepareServerProcess = runtime.exec(String.format(prepareServerTemplate, configuration.getSshAlias(), scriptFileNameOnServer));

            BufferedReader processStreamReader = new BufferedReader(new InputStreamReader(prepareServerProcess.getInputStream()));
            String outputLine = null;
            while ((outputLine = processStreamReader.readLine()) != null) {
                String prefixLogLine = PrepareUbuntu20ServerDependenciesScriptFileWriter.CONST_LOG_LINE;
                if (outputLine.startsWith(prefixLogLine)) {
                    String logLine = outputLine.substring(prefixLogLine.length()).trim();
                    progressIndicator.setText(logLine);
                }
            }

            int rc = prepareServerProcess.waitFor();
            try { processStreamReader.close(); } catch (Throwable e) {};
            if (rc != 0) {
                if (rc == PrepareUbuntu20ServerDependenciesScriptFileWriter.CONST_EXIT_CODE_INVALID_PASSWORD) {
                    throw new RuntimeException("Invalid sudo password");
                } else {
                    throw new RuntimeException("Failed to run setup script to the server");
                }
            }

            Thread.sleep(2000L);
            notificationManager.testNotification(myProject, String.format("Server successfully prepared for android builds"));
        } catch (Throwable e) {
            handleException(e);
        } finally {
            fileManager.deleteServerPrepareScriptFile();
            stateManager.setLongBackgroundTaskRunningState(false);
        }
    }

    private void handleException(Throwable e) {
        stateManager.setLongBackgroundTaskRunningState(false);
        notificationManager.testNotification(myProject, String.format("Error: %s", String.valueOf(e.getMessage())));
        finishTasks();
    }

    private void finishTasks() {
        if (prepareServerProcess != null && prepareServerProcess.isAlive()) {
            try { prepareServerProcess.destroy(); } catch (Throwable e) {}
        }
    }
}
