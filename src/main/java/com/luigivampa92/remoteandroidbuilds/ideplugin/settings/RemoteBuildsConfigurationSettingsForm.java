package com.luigivampa92.remoteandroidbuilds.ideplugin.settings;

import com.intellij.openapi.ui.DialogWrapper;
import com.luigivampa92.remoteandroidbuilds.ideplugin.services.RemoteBuildsConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;

public final class RemoteBuildsConfigurationSettingsForm extends DialogWrapper {

    private JPanel remoteBuildsConfigPanel;
    private JPanel sshSettingsPanel;
    private JTextField sshAliasTextField;
    private JTextField sshUserNameTextField;
    private JPasswordField sshUserPasswordTextField;
    private JPanel proxySettingsPanel;
    private JCheckBox proxyRequiredCheckBox;
    private JTextField proxyPortTextField;
    private JPanel sdkSettingsPanel;
    private JTextField sdkDependenciesTextField;
    private JLabel sshAliasLabel;
    private JLabel sshUserNameLabel;
    private JLabel sshUserPasswordLabel;
    private JLabel proxyPortLabel;
    private JLabel proxySettingsDescriptionLabel;
    private JLabel sdkDependenciesLabel;
    private JLabel sdkDependenciesDescriptionLabel;
    private JCheckBox extraDependenciesRequiredCheckBox;

    public RemoteBuildsConfigurationSettingsForm() {
        super(null);
        init();
        proxyRequiredCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setProxySettingsEnabled(true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                setProxySettingsEnabled(false);
            }
        });
        extraDependenciesRequiredCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setSdkDependenciesSettingsEnabled(true);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                setSdkDependenciesSettingsEnabled(false);
            }
        });
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return remoteBuildsConfigPanel;
    }

    public void setConfigurationState(RemoteBuildsConfiguration state) {
        sshAliasTextField.setText(state.getSshAlias());
        sshUserNameTextField.setText(state.getSshUserName());
        sshUserPasswordTextField.setText(state.getSshUserPassword());
        proxyRequiredCheckBox.getModel().setSelected(state.isProxyRequired());
        setProxySettingsEnabled(state.isProxyRequired());
        proxyPortTextField.setText(state.getProxyPort());
        extraDependenciesRequiredCheckBox.getModel().setSelected(state.isExtraSdkDependenciesRequired());
        setSdkDependenciesSettingsEnabled(state.isExtraSdkDependenciesRequired());
        sdkDependenciesTextField.setText(state.getSdkDependencies());
    }

    public void resetConfigurationState() {
        sshAliasTextField.setText(null);
        sshUserNameTextField.setText(null);
        sshUserPasswordTextField.setText(null);
        proxyRequiredCheckBox.getModel().setSelected(false);
        proxyPortLabel.setEnabled(false);
        proxySettingsDescriptionLabel.setEnabled(false);
        proxyPortTextField.setEnabled(false);
        proxyPortTextField.setText(null);
        extraDependenciesRequiredCheckBox.getModel().setSelected(false);
        sdkDependenciesLabel.setEnabled(false);
        sdkDependenciesDescriptionLabel.setEnabled(false);
        sdkDependenciesTextField.setText(null);
    }

    public RemoteBuildsConfiguration getConfigurationState() {
        if (validateConfigState()) {
            return new RemoteBuildsConfiguration(
                    sshAliasTextField.getText(),
                    sshUserNameTextField.getText(),
                    sshUserPasswordTextField.getText(),
                    proxyRequiredCheckBox.getModel().isSelected(),
                    proxyPortTextField.getText(),
                    extraDependenciesRequiredCheckBox.getModel().isSelected(),
                    sdkDependenciesTextField.getText()
            );
        } else {
            return null;
        }
    }

    private boolean validateConfigState() {
        return true; // todo !
    }

    private void setProxySettingsEnabled(boolean enabled) {
        proxyPortLabel.setEnabled(enabled);
        proxySettingsDescriptionLabel.setEnabled(enabled);
        proxyPortTextField.setEnabled(enabled);
    }

    private void setSdkDependenciesSettingsEnabled(boolean enabled) {
        sdkDependenciesLabel.setEnabled(enabled);
        sdkDependenciesDescriptionLabel.setEnabled(enabled);
        sdkDependenciesTextField.setEnabled(enabled);
    }
}
