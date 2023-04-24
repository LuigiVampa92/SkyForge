package com.luigivampa92.remoteandroidbuilds.ideplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.PrepareServerDependenciesBackgroundTask;
import com.luigivampa92.remoteandroidbuilds.ideplugin.RemoteBuildsStateManager;
import com.luigivampa92.remoteandroidbuilds.ideplugin.SdkManagerDependenciesResolver;
import com.luigivampa92.remoteandroidbuilds.ideplugin.ServiceLocator;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public final class PrepareServerAction extends IdeAndGradleAwareAction {

    private final RemoteBuildsStateManager stateManager = ServiceLocator.getInstance().getRemoteBuildsStateManager();

    @Override
    protected void doUpdate(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setEnabled(stateManager.getCurrentState().isEnabled());
    }

    @Override
    protected void doPerform(@NotNull AnActionEvent e, @NotNull Project project) {
        SdkManagerDependenciesResolver dependenciesResolver = ServiceLocator.getInstance().getSdkManagerDependenciesResolver();
        Collection<String> projectBuildDependencies = dependenciesResolver.getDependencies(project);

        RemoteBuildConfigurationService configurationService = ServiceLocator.getInstance().getRemoteBuildConfigurationService();
        Collection<String> extraSdkManagerDependencies = configurationService.getConfiguration().getValidatedExtraSdkManagerDependenciesCollection();

        ArrayList<String> dependencies = new ArrayList<>();
        dependencies.addAll(projectBuildDependencies);
        dependencies.addAll(extraSdkManagerDependencies);

        PrepareServerDependenciesBackgroundTask task = ServiceLocator.getInstance().newPrepareServerTask(project, dependencies);
        ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.run(task);
    }
}
