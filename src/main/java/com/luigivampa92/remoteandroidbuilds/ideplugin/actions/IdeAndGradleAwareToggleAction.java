package com.luigivampa92.remoteandroidbuilds.ideplugin.actions;

import com.android.tools.idea.IdeInfo;
import com.android.tools.idea.gradle.project.GradleProjectInfo;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class IdeAndGradleAwareToggleAction extends ToggleAction {

    private IdeInfo ideInfo;

    public final void update(@NotNull AnActionEvent e) {
        if (isGradleAndroidStudioProject(e)) {
            e.getPresentation().setEnabledAndVisible(true);
            this.doUpdate(e, e.getProject());
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
        super.update(e);
    }

    protected void doUpdate(@NotNull AnActionEvent e, @NotNull Project project) {}

    @Override
    public final boolean isSelected(@NotNull AnActionEvent e) {
        if (isGradleAndroidStudioProject(e)) {
            e.getPresentation().setEnabledAndVisible(true);
            return this.doIsSelected(e, e.getProject());
        } else {
            e.getPresentation().setEnabledAndVisible(false);
            return false;
        }
    }

    protected abstract boolean doIsSelected(@NotNull AnActionEvent e, @NotNull Project project);

    @Override
    public final void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (isGradleAndroidStudioProject(e)) {
            e.getPresentation().setEnabledAndVisible(true);
            this.doSetSelected(e, e.getProject(), state);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    protected abstract void doSetSelected(@NotNull AnActionEvent e, @NotNull Project project, boolean state);

    @Override
    public final boolean isDumbAware() {
        return false;
    }

    private boolean isGradleAndroidStudioProject(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            boolean ideIsAndroidStudio = getIdeInfo().isAndroidStudio();
            boolean projectIsBuiltWithGradle = GradleProjectInfo.getInstance(project).isBuildWithGradle();
            return ideIsAndroidStudio && projectIsBuiltWithGradle;
        } else {
            return false;
        }
    }

    private IdeInfo getIdeInfo() {
        if (ideInfo == null) {
            ideInfo = IdeInfo.getInstance();
        }
        return ideInfo;
    }
}
