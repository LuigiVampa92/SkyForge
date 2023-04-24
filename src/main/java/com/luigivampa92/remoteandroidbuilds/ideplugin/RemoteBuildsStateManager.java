package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.SshExecutor;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;

import java.io.File;

public final class RemoteBuildsStateManager {

    private final ServiceLocator serviceLocator;
    private final RemoteBuildConfigurationService configurationService;
    private final FileManager fileManager;
    private final SshExecutor sshExecutor;
    private final NotificationManager notificationManager;
    private RemoteBuildsModeState currentState;

    public synchronized RemoteBuildsModeState getCurrentState() {
        return currentState;
    }

    private synchronized void setCurrentState(RemoteBuildsModeState state) {
        this.currentState = state;
    }

    public RemoteBuildsStateManager(ServiceLocator serviceLocator, RemoteBuildConfigurationService configurationService, FileManager fileManager, SshExecutor sshExecutor, NotificationManager notificationManager) {
        this.serviceLocator = serviceLocator;
        this.configurationService = configurationService;
        this.fileManager = fileManager;
        this.sshExecutor = sshExecutor;
        this.notificationManager = notificationManager;
        currentState = RemoteBuildsModeState.defaultValue();
    }

    // todo something with project ?? mb get rid of project usage in settings?
    // todo add notnull annotations !

    public synchronized void updateState(Project project) {
        if (RemoteBuildsModeState.PENDING.equals(currentState)) {
            return;
        }

        RemoteBuildsModeState newState;
        RemoteBuildsConfiguration configuration = configurationService.getConfiguration();

        // also check init script value ??
//        ProjectPathValues currentProjectPathValues = getCurrentProjectPathValues(project);
//        String dir = currentProjectPathValues.getDir();
//        String currentProjectInitValue = fileManager.getRemoteBuildsInitScriptProjectValue();
//        boolean initValuesEquals = currentProjectInitValue.equals(dir);

        boolean settingsFilled = configuration.isSshSettingsValid();
        if (settingsFilled) {
            boolean projectMarked = isCurrentProjectMarkedAsRemoteBuild(project);
            if (projectMarked) { // && initValuesEquals ??
                newState = RemoteBuildsModeState.ACTIVATED;
            } else {
                newState = RemoteBuildsModeState.DEACTIVATED;
            }
        } else {
            newState = RemoteBuildsModeState.DISABLED;
        }
        setCurrentState(newState);
    }

    public synchronized void setLongBackgroundTaskRunningState(boolean taskIsRunning) {
        RemoteBuildsModeState newState = taskIsRunning ? RemoteBuildsModeState.PENDING : RemoteBuildsModeState.DISABLED;
        setCurrentState(newState);
    }

    public final void enableRemote(Project project) {
        if (RemoteBuildsModeState.DEACTIVATED.equals(currentState)) {
            final Project currentProject = project;
            setCurrentState(RemoteBuildsModeState.PENDING);
            EnableRemoteBuildsTask enableRemoteBuildsTask = serviceLocator.newEnableRemoteBuildsTask(currentProject, () -> {
                setCurrentState(RemoteBuildsModeState.ACTIVATED);
                notificationManager.testNotification(currentProject, "Remote builds mode activated");
            }, e -> {
                fileManager.deleteMarkedProjectValues();
                fileManager.deleteRemoteBuildsInitScriptFile();
                setCurrentState(RemoteBuildsModeState.DEACTIVATED);
                notificationManager.testNotification(currentProject, String.format("Error: %s", String.valueOf(e.getMessage())));
            });
            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.run(enableRemoteBuildsTask);
        }
    }

    public final void disableRemote(Project project) {
        if (RemoteBuildsModeState.ACTIVATED.equals(currentState)) {
            final Project currentProject = project;
            setCurrentState(RemoteBuildsModeState.PENDING);

            DisableRemoteBuildsTask disableRemoteBuildsTask = serviceLocator.newDisableRemoteBuildsTask(currentProject, () -> {
                setCurrentState(RemoteBuildsModeState.DEACTIVATED);
                notificationManager.testNotification(currentProject, "Remote builds mode deactivated");
            }, e -> {
                fileManager.deleteRemoteBuildsInitScriptFile();
                fileManager.deleteMarkedProjectValues();
                setCurrentState(RemoteBuildsModeState.DEACTIVATED);
                notificationManager.testNotification(currentProject, String.format("Error: %s", String.valueOf(e.getMessage())));
            });

            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.run(disableRemoteBuildsTask);
        }
    }

    private boolean isCurrentProjectMarkedAsRemoteBuild(Project project) {
        if (project != null) {
            ProjectPathValues currentProjectPathValues = getCurrentProjectPathValues(project);
            ProjectPathValues markedProjectValues = fileManager.getMarkedProjectValues();
            return markedProjectValues != null && markedProjectValues.osAwareEquals(currentProjectPathValues);
        } else {
            return false;
        }
    }

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