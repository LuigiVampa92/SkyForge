package com.luigivampa92.remoteandroidbuilds.ideplugin.services;

import com.intellij.openapi.components.Service;

@Service(Service.Level.APP)
public final class RemoteBuildConfigurationServicePersistentStateImpl implements RemoteBuildConfigurationService {

    private final RemoteBuildsConfigurationPersistentStateService persistentState = RemoteBuildsConfigurationPersistentStateService.getApplicationInstance();

    @Override
    public RemoteBuildsConfiguration getConfiguration() {
        if (persistentState != null) {
            RemoteBuildsConfigurationPersistentStateService state = persistentState.getState();
            if (state != null) {
                return new RemoteBuildsConfiguration(
                        state.sshAlias,
                        state.sshUserName,
                        state.sshUserPassword,
                        state.proxyRequired,
                        state.proxyPort,
                        state.extraSdkDependenciesRequired,
                        state.sdkDependencies
                );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void saveConfiguration(RemoteBuildsConfiguration configuration) {
        if (persistentState != null) {
            persistentState.setSshAlias(configuration.getSshAlias());
            persistentState.setSshUserName(configuration.getSshUserName());
            persistentState.setSshUserPassword(configuration.getSshUserPassword());
            persistentState.setProxyRequired(configuration.isProxyRequired());
            persistentState.setProxyPort(configuration.getProxyPort());
            persistentState.setExtraSdkDependenciesRequired(configuration.isExtraSdkDependenciesRequired());
            persistentState.setSdkDependencies(configuration.getSdkDependencies());
            persistentState.loadState(persistentState);
        }
    }
}
