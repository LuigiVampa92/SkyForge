package com.luigivampa92.remoteandroidbuilds.ideplugin.services;

import java.util.*;
import java.util.stream.Collectors;

public final class RemoteBuildsConfiguration {

    private final String sshAlias;
    private final String sshUserName;
    private final String sshUserPassword;
    private final boolean proxyRequired;
    private final String proxyPort;
    private final boolean extraSdkDependenciesRequired;
    private final String sdkDependencies;

    public RemoteBuildsConfiguration() {
        this.sshAlias = null;
        this.sshUserName = null;
        this.sshUserPassword = null;
        this.proxyRequired = false;
        this.proxyPort = null;
        this.extraSdkDependenciesRequired = false;
        this.sdkDependencies = null;
    }

    public RemoteBuildsConfiguration(String sshAlias, String sshUserName, String sshUserPassword, boolean proxyRequired, String proxyPort, boolean extraSdkDependenciesRequired, String sdkDependencies) {
        this.sshAlias = sshAlias;
        this.sshUserName = sshUserName;
        this.sshUserPassword = sshUserPassword;
        this.proxyRequired = proxyRequired;
        this.proxyPort = proxyPort;
        this.extraSdkDependenciesRequired = extraSdkDependenciesRequired;
        this.sdkDependencies = sdkDependencies;
    }

    public boolean isSshSettingsValid() {
        return sshAlias != null && !sshAlias.isEmpty() && !sshAlias.contains(" ") && !sshAlias.contains("@") &&
                sshUserName != null && !sshUserName.isEmpty() &&
                ("root".equals(sshUserName) || (sshUserPassword != null && !sshUserPassword.isEmpty()));
    }

    public boolean isProxySettingsValid() {
        if (!proxyRequired) {
            return true;
        } else {
            return !getProxyPortsValues().isEmpty();
        }
    }

    public List<Integer> getProxyPortsValues() {
        if (!proxyRequired) {
            return new ArrayList<>();
        } else {
            if (proxyPort != null && !proxyPort.isEmpty()) {
                HashSet<Integer> ports = new HashSet<>();
                String[] tokens = proxyPort
                        .trim()
                        .replace(",", " ")
                        .replace(":", " ")
                        .replace(";", " ")
                        .replace(".", " ")
                        .replace("-", " ")
                        .replace("|", " ")
                        .split(" ");
                for (String token : tokens) {
                    if (token != null && !token.isEmpty()) {
                        try {
                            int portNumber = Integer.parseInt(token);
                            if (portNumber > 0 && portNumber < 65536) {
                                ports.add(portNumber);
                            }
                        } catch (NumberFormatException e) {
                            return new ArrayList<>();
                        }
                    }
                }
                return new ArrayList<>(ports);
            } else {
                return new ArrayList<>();
            }
        }
    }

    public Collection<String> getValidatedExtraSdkManagerDependenciesCollection() {
        if (!extraSdkDependenciesRequired || sdkDependencies == null || sdkDependencies.isEmpty()) {
            return new ArrayList<>();
        } else {
            HashSet<String> values = new HashSet<>();
            String[] tokens = sdkDependencies.trim().replace(",", " ").split(" ");
            for (String token : tokens) {
                if (token != null && !token.isEmpty()) {
                    values.add(token);
                }
            }
            return values.stream().sorted().collect(Collectors.toList());
        }
    }

    public String getSshAlias() {
        return sshAlias;
    }

    public String getSshUserName() {
        return sshUserName;
    }

    public String getSshUserPassword() {
        return sshUserPassword;
    }

    public boolean isProxyRequired() {
        return proxyRequired;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public boolean isExtraSdkDependenciesRequired() {
        return extraSdkDependenciesRequired;
    }

    public String getSdkDependencies() {
        return sdkDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteBuildsConfiguration that = (RemoteBuildsConfiguration) o;
        return proxyRequired == that.proxyRequired && extraSdkDependenciesRequired == that.extraSdkDependenciesRequired &&
                Objects.equals(sshAlias, that.sshAlias) &&
                Objects.equals(sshUserName, that.sshUserName) &&
                Objects.equals(sshUserPassword, that.sshUserPassword) &&
                Objects.equals(proxyPort, that.proxyPort) &&
                Objects.equals(sdkDependencies, that.sdkDependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sshAlias, sshUserName, sshUserPassword, proxyRequired, proxyPort, extraSdkDependenciesRequired, sdkDependencies);
    }
}
