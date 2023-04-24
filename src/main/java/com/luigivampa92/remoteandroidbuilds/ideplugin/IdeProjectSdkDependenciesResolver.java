package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.android.tools.idea.gradle.model.IdeAndroidProject;
import com.android.tools.idea.gradle.project.model.GradleAndroidModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import java.util.*;

public final class IdeProjectSdkDependenciesResolver implements SdkManagerDependenciesResolver {

    private final String[] MANDATORY_DEPENDENCIES = new String[] { "platform-tools" };
    private final String PREFIX_VERSION_BUILD_TOOLS = "build-tools;";
    private final String PREFIX_VERSION_SDK = "platforms;";

    @Override
    public Collection<String> getDependencies(Project project) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (project == null) {
            return result;
        }

        HashSet<String> versionsBuildTools = new HashSet<>();
        HashSet<String> versionsSdk = new HashSet<>();

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        final Module[] modules = moduleManager.getModules();
        for (Module module : modules) {
            try {
                GradleAndroidModel gradleAndroidModel = GradleAndroidModel.get(module);
                if (gradleAndroidModel != null) {
                    IdeAndroidProject androidProject = gradleAndroidModel.getAndroidProject();
                    String buildToolsVersion = androidProject.getBuildToolsVersion();
                    versionsBuildTools.add(buildToolsVersion);
                    String compileTarget = androidProject.getCompileTarget();
                    versionsSdk.add(compileTarget);
                }
            } catch (Throwable e) {}
        }

        Collections.addAll(result, MANDATORY_DEPENDENCIES);
        result.addAll(formatBuildToolsDependencies(versionsBuildTools));
        result.addAll(formatSdkDependencies(versionsSdk));

        return result;
    }

    private Collection<String> formatBuildToolsDependencies(Collection<String> buildToolsVersions) {
        return prependWithPrefix(PREFIX_VERSION_BUILD_TOOLS, buildToolsVersions);
    }

    private Collection<String> formatSdkDependencies(Collection<String> sdkVersions) {
        return prependWithPrefix(PREFIX_VERSION_SDK, sdkVersions);
    }

    private Collection<String> prependWithPrefix(String prefix, Collection<String> buildToolsVersion) {
        ArrayList<String> result = new ArrayList<>();
        for (String version : buildToolsVersion) {
            result.add(prefix + version);
        }
        return result;
    }
}
