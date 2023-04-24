package com.luigivampa92.remoteandroidbuilds.ideplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.RemoteBuildsModeState;
import com.luigivampa92.remoteandroidbuilds.ideplugin.RemoteBuildsStateManager;
import com.luigivampa92.remoteandroidbuilds.ideplugin.ServiceLocator;
import com.luigivampa92.remoteandroidbuilds.ideplugin.settings.RemoteBuildsConfigurationSettings;
import org.jetbrains.annotations.NotNull;

public final class OpenPluginSettingsAction extends IdeAndGradleAwareAction {

    private final RemoteBuildsStateManager stateManager = ServiceLocator.getInstance().getRemoteBuildsStateManager();

    @Override
    protected void doUpdate(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setEnabled(!RemoteBuildsModeState.PENDING.equals(stateManager.getCurrentState()));
    }

    @Override
    protected void doPerform(@NotNull AnActionEvent e, @NotNull Project project) {
        if (!RemoteBuildsModeState.PENDING.equals(stateManager.getCurrentState())) {
            openPluginSettings(project);
        }
    }

    private void openPluginSettings(Project project) {
        RemoteBuildsConfigurationSettings settings = new RemoteBuildsConfigurationSettings();
        ShowSettingsUtil.getInstance().editConfigurable(project, settings);
    }
}
