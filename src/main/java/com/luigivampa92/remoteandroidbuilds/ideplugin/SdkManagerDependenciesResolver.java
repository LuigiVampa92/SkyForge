package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.openapi.project.Project;

import java.util.Collection;

public interface SdkManagerDependenciesResolver {
    Collection<String> getDependencies(Project project);
}
