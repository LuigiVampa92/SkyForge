package com.luigivampa92.remoteandroidbuilds.ideplugin.services;

import com.intellij.openapi.components.Service;

@Service(Service.Level.APP)
public interface RemoteBuildConfigurationService {
    RemoteBuildsConfiguration getConfiguration();
    void saveConfiguration(RemoteBuildsConfiguration configuration);
}