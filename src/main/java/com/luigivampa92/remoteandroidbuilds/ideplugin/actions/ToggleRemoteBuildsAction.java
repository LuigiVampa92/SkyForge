package com.luigivampa92.remoteandroidbuilds.ideplugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.luigivampa92.remoteandroidbuilds.ideplugin.*;
import org.jetbrains.annotations.NotNull;

public final class ToggleRemoteBuildsAction extends IdeAndGradleAwareToggleAction {

    private final RemoteBuildsStateManager stateManager = ServiceLocator.getInstance().getRemoteBuildsStateManager();

    @Override
    protected void doUpdate(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setEnabled(stateManager.getCurrentState().isEnabled());
    }

    @Override
    protected boolean doIsSelected(@NotNull AnActionEvent e, @NotNull Project project) {
        stateManager.updateState(project);
        RemoteBuildsModeState currentState = stateManager.getCurrentState();
        e.getPresentation().setEnabled(currentState.isEnabled());
        return currentState.isPressed();
    }

    @Override
    protected void doSetSelected(@NotNull AnActionEvent e, @NotNull Project project, boolean state) {
        if (state) {
            stateManager.enableRemote(project);
        } else {
            stateManager.disableRemote(project);
        }
    }
}
