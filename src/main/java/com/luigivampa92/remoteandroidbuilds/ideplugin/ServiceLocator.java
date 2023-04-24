package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.PlatformComponentFactory;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.ProcessKiller;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.ProcessListRetriever;
import com.luigivampa92.remoteandroidbuilds.ideplugin.os.SshExecutor;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;

import java.util.Collection;

public final class ServiceLocator {

    private static ServiceLocator instance;
    private final RemoteBuildsStateManager remoteBuildsStateManager;
    private final NotificationManager notificationManager;
    private final FileManager fileManager;
    private final PlatformComponentFactory platformComponentFactory;
    private final SdkManagerDependenciesResolver sdkManagerDependenciesResolver;

    private ServiceLocator() {
        fileManager = new FileManager();
        notificationManager = new NotificationManager();
        platformComponentFactory = new PlatformComponentFactory(fileManager);
        remoteBuildsStateManager = new RemoteBuildsStateManager(this, getRemoteBuildConfigurationService(), fileManager, platformComponentFactory.getSshExecutor(), notificationManager);
        sdkManagerDependenciesResolver = new IdeProjectSdkDependenciesResolver();
    }

    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    public RemoteBuildsStateManager getRemoteBuildsStateManager() {
        return remoteBuildsStateManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public RemoteBuildConfigurationService getRemoteBuildConfigurationService() {
        return ApplicationManager.getApplication().getService(RemoteBuildConfigurationService.class);
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public SshExecutor getSshExecutor() {
        return platformComponentFactory.getSshExecutor();
    }

    public SdkManagerDependenciesResolver getSdkManagerDependenciesResolver() {
        return sdkManagerDependenciesResolver;
    }

    public ProcessListRetriever getProcessListRetriever() {
        return platformComponentFactory.getProcessListRetriever();
    }

    public ProcessKiller getProcessKiller() {
        return platformComponentFactory.getProcessKiller();
    }

    public PrepareServerDependenciesBackgroundTask newPrepareServerTask(Project project, Collection<String> dependencies) {
        return new PrepareServerDependenciesBackgroundTask(project, getRemoteBuildConfigurationService(), getRemoteBuildsStateManager(), getSshExecutor(), notificationManager, fileManager, dependencies);
    }

    public EnableRemoteBuildsTask newEnableRemoteBuildsTask(Project project, AsyncSuccessCallback callbackSuccess, AsyncErrorCallback callbackError) {
        return new EnableRemoteBuildsTask(getRemoteBuildConfigurationService(), fileManager, getSshExecutor(), project, callbackSuccess, callbackError);
    }

    public DisableRemoteBuildsTask newDisableRemoteBuildsTask(Project project, AsyncSuccessCallback callbackSuccess, AsyncErrorCallback callbackError) {
        return new DisableRemoteBuildsTask(getRemoteBuildConfigurationService(), fileManager, getSshExecutor(), project, callbackSuccess, callbackError);
    }
}
