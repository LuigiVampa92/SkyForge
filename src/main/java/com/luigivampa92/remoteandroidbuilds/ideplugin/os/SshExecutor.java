package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.util.List;

public interface SshExecutor {
    boolean checkSshExists();
    boolean checkSshConnection(String sshAlias);
    boolean prepareLocalPropertiesOnServer(String sshAlias, String sshUser, String projectDirName);
    boolean uploadDebugKeystoreToServer(String sshAlias, String user);
    boolean startSshTunnelOnPort(String sshAlias, int port);
    boolean stopSshTunnelsOnPorts(List<Integer> ports);
    boolean checkRsyncExists();
}
