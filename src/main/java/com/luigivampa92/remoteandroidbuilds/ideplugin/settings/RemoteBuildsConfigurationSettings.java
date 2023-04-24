package com.luigivampa92.remoteandroidbuilds.ideplugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.luigivampa92.remoteandroidbuilds.ideplugin.ServiceLocator;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildConfigurationService;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class RemoteBuildsConfigurationSettings implements Configurable {

    private RemoteBuildConfigurationService configurationService = ServiceLocator.getInstance().getRemoteBuildConfigurationService();
    private RemoteBuildsConfigurationSettingsForm configurationForm;
    private RemoteBuildsConfiguration initialConfiguration;

    @Override
    public @Nullable JComponent createComponent() {
        configurationForm = new RemoteBuildsConfigurationSettingsForm();
        return configurationForm.createCenterPanel();
    }

    @Override
    public void reset() {
        if (configurationService != null) {
            initialConfiguration = configurationService.getConfiguration();
            if (initialConfiguration != null) {
                configurationForm.setConfigurationState(initialConfiguration);
            }
        }
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Remote Builds";
    }

    // TODO : compare states right after the change ??

    @Override
    public boolean isModified() {
        if (configurationForm != null) {
            RemoteBuildsConfiguration configurationState = configurationForm.getConfigurationState();
            return configurationState != null && !configurationState.equals(initialConfiguration);
        } else {
            return false;
        }
    }

    @Override
    public void apply() throws ConfigurationException {
        if (configurationService != null) {
            RemoteBuildsConfiguration configuration = configurationForm.getConfigurationState();
            if (configuration != null) {
                configurationService.saveConfiguration(configuration);
                initialConfiguration = configuration;
            }
        }
    }
}
