package com.luigivampa92.remoteandroidbuilds.ideplugin.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "RemoteBuildsConfiguration", storages = @Storage(value = "remoteBuildsConfiguration.xml") )
public final class RemoteBuildsConfigurationPersistentStateService implements PersistentStateComponent<RemoteBuildsConfigurationPersistentStateService> {

    // to create a separate config service for every project, leave it here just in case
    public static RemoteBuildsConfigurationPersistentStateService getProjectInstance(Project project) {
        return project.getService(RemoteBuildsConfigurationPersistentStateService.class);
    }

    public static RemoteBuildsConfigurationPersistentStateService getApplicationInstance() {
        return ApplicationManager.getApplication().getService(RemoteBuildsConfigurationPersistentStateService.class);
    }

    @Override
    public @Nullable RemoteBuildsConfigurationPersistentStateService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull RemoteBuildsConfigurationPersistentStateService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    String sshAlias;
    String sshUserName;
    String sshUserPassword;
    boolean proxyRequired;
    String proxyPort;
    boolean extraSdkDependenciesRequired;
    String sdkDependencies;

    // TODO : can getters and setters be removed?

    public String getSshAlias() {
        return sshAlias;
    }

    public void setSshAlias(String sshAlias) {
        this.sshAlias = sshAlias;
    }

    public String getSshUserName() {
        return sshUserName;
    }

    public void setSshUserName(String sshUserName) {
        this.sshUserName = sshUserName;
    }

    public String getSshUserPassword() {
        return sshUserPassword;
    }

    public void setSshUserPassword(String sshUserPassword) {
        this.sshUserPassword = sshUserPassword;
    }

    public boolean isProxyRequired() {
        return proxyRequired;
    }

    public void setProxyRequired(boolean proxyRequired) {
        this.proxyRequired = proxyRequired;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isExtraSdkDependenciesRequired() {
        return extraSdkDependenciesRequired;
    }

    public void setExtraSdkDependenciesRequired(boolean extraSdkDependenciesRequired) {
        this.extraSdkDependenciesRequired = extraSdkDependenciesRequired;
    }

    public String getSdkDependencies() {
        return sdkDependencies;
    }

    public void setSdkDependencies(String sdkDependencies) {
        this.sdkDependencies = sdkDependencies;
    }
}
