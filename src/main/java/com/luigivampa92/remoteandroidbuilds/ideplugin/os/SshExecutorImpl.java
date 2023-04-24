package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.luigivampa92.remoteandroidbuilds.ideplugin.FileManager;

import java.util.List;

public final class SshExecutorImpl implements SshExecutor {

    public static final String DELIMETER = ";";
    public static final String CMD_TEMPLATE_SSH_VERSION = "%s -V";
    public static final String CMD_TEMPLATE_SSH_CHECK_CONNECTION = "%s -q -o StrictHostKeyChecking=no -o BatchMode=yes -o ConnectTimeout=5 %s exit";
    public static final String CMD_TEMPLATE_SSH_PREPARE_LOCAL_PROPERTIES_USER_NORMAL = "%s %s mkdir -p /home/%s/.mirakle/%s ; echo sdk.dir=/home/%s/Android/Sdk > /home/%s/.mirakle/%s/local.properties";
    public static final String CMD_TEMPLATE_SSH_PREPARE_LOCAL_PROPERTIES_USER_ROOT = "%s %s mkdir -p /root/.mirakle/%s ; echo sdk.dir=/root/Android/Sdk > /root/.mirakle/%s/local.properties";
    public static final String CMD_TEMPLATE_SSH_PREPARE_DEBUG_KEYSTORE_FOLDER_USER_NORMAL = "%s %s mkdir -p /home/%s/.android";
    public static final String CMD_TEMPLATE_SSH_PREPARE_DEBUG_KEYSTORE_FOLDER_USER_ROOT = "%s %s mkdir -p /root/.android";
    public static final String CMD_TEMPLATE_SCP_UPLOAD_DEBUG_KEYSTORE = "%s %s %s:~/.android/debug.keystore";

    public static final String CMD_TEMPLATE_SSH_START_TUNNEL = "%s -o ExitOnForwardFailure=yes -f -N %s -R %s:localhost:%s";
    public static final String CMD_TEMPLATE_RSYNC_VERSION = "%s --version";

    private final RcOnlyShellExecutor shellExecutor = new RcOnlyShellExecutor();
    private final FileManager fileManager;
    private final ProcessListRetriever processListRetriever;
    private final ProcessKiller processKiller;

    public SshExecutorImpl(FileManager fileManager, ProcessListRetriever processListRetriever, ProcessKiller processKiller) {
        this.fileManager = fileManager;
        this.processListRetriever = processListRetriever;
        this.processKiller = processKiller;
    }

    @Override
    public boolean checkSshExists() {
        String commandSshVersion = String.format(CMD_TEMPLATE_SSH_VERSION, getSshExecutableValueForPlatform());
        ShellExecutionResult result = shellExecutor.execute(commandSshVersion);
        return result.getExitCode() == 0;
    }

    @Override
    public boolean checkSshConnection(String sshAlias) {
        String commandSshConnectionTest = String.format(CMD_TEMPLATE_SSH_CHECK_CONNECTION, getSshExecutableValueForPlatform(), sshAlias);
        ShellExecutionResult result = shellExecutor.execute(commandSshConnectionTest);
        return result.getExitCode() == 0;
    }

    @Override
    public boolean prepareLocalPropertiesOnServer(String sshAlias, String sshUser, String projectDirName) {
        String commandSshPrepareLocalProperties;
        if (sshUser.equals("root")) {
            commandSshPrepareLocalProperties = String.format(CMD_TEMPLATE_SSH_PREPARE_LOCAL_PROPERTIES_USER_ROOT, getSshExecutableValueForPlatform(), sshAlias, projectDirName, projectDirName);
        } else {
            commandSshPrepareLocalProperties = String.format(CMD_TEMPLATE_SSH_PREPARE_LOCAL_PROPERTIES_USER_NORMAL, getSshExecutableValueForPlatform(), sshAlias, sshUser, projectDirName, sshUser, sshUser, projectDirName);
        }
        ShellExecutionResult result = shellExecutor.execute(commandSshPrepareLocalProperties);
        return result.getExitCode() == 0;
    }

    @Override
    public boolean uploadDebugKeystoreToServer(String sshAlias, String user) {
        String androidDebugKeystoreFile = fileManager.getAndroidDebugKeystoreFilePath();
        if (androidDebugKeystoreFile != null && !androidDebugKeystoreFile.isEmpty()) {
            String commandPrepareAndroidDebugKeystoreFolder = null;
            if ("root".equals(user)) {
                commandPrepareAndroidDebugKeystoreFolder = String.format(CMD_TEMPLATE_SSH_PREPARE_DEBUG_KEYSTORE_FOLDER_USER_ROOT, getSshExecutableValueForPlatform(), sshAlias);
            } else {
                commandPrepareAndroidDebugKeystoreFolder = String.format(CMD_TEMPLATE_SSH_PREPARE_DEBUG_KEYSTORE_FOLDER_USER_NORMAL, getSshExecutableValueForPlatform(), sshAlias, user);
            }
            ShellExecutionResult prepareAndroidDebugKeystoreFolderResult = shellExecutor.execute(commandPrepareAndroidDebugKeystoreFolder, 4500);
            if (prepareAndroidDebugKeystoreFolderResult.getExitCode() != 0) {
                return false;
            }
            String osAwareAndroidDebugKeystoreFile = FileManager.fixFilePathForWindowsCygwin(androidDebugKeystoreFile);
            String commandTransferAndroidKeystore = String.format(CMD_TEMPLATE_SCP_UPLOAD_DEBUG_KEYSTORE, getScpExecutableValueForPlatform(), osAwareAndroidDebugKeystoreFile, sshAlias);
            ShellExecutionResult result = shellExecutor.execute(commandTransferAndroidKeystore, 6000);
            return result.getExitCode() == 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean startSshTunnelOnPort(String sshAlias, int port) {
        if (port < 1 || port > 65535) {
            return false;
        }
        String portValue = String.valueOf(port);
        String commandSshStartTunnel = String.format(CMD_TEMPLATE_SSH_START_TUNNEL, getSshExecutableValueForPlatform(), sshAlias, portValue, portValue);
        ShellExecutionResult result = shellExecutor.execute(commandSshStartTunnel);
        return result.getExitCode() == 0;
    }

    @Override
    public boolean stopSshTunnelsOnPorts(List<Integer> ports) {
        try {
            List<ProcessRecord> sshTunnelProcesses = processListRetriever.getSshProcessesOnPorts(ports);
            for (ProcessRecord process : sshTunnelProcesses) {
                processKiller.kill(process.getPid(), false);
            }
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean checkRsyncExists() {
        String commandRsyncVersion = String.format(CMD_TEMPLATE_RSYNC_VERSION, getRsyncExecutableValueForPlatform());
        ShellExecutionResult result = shellExecutor.execute(commandRsyncVersion);
        return result.getExitCode() == 0;
    }



    // on windows only cygwin or wsl binaries work as intended
    // default C:\\Windows\\System\\OpenSSH\\ssh.exe cannot create gateway ports and thus useless

    private String getSshExecutableValueForPlatform() {
        PlatformUtil.Platform platform = PlatformUtil.getCurrentPlattform();
        if (PlatformUtil.Platform.MAC_OS.equals(platform) || PlatformUtil.Platform.LINUX.equals(platform)) {
            return "ssh";
        } else if (PlatformUtil.Platform.WIN.equals(platform)) {
//            return "ssh.exe";
            return "ssh";
        } else {
            throw new RuntimeException("Current platform is not supported");
        }
    }

    private String getScpExecutableValueForPlatform() {
        PlatformUtil.Platform platform = PlatformUtil.getCurrentPlattform();
        if (PlatformUtil.Platform.MAC_OS.equals(platform) || PlatformUtil.Platform.LINUX.equals(platform)) {
            return "scp";
        } else if (PlatformUtil.Platform.WIN.equals(platform)) {
//            return "scp.exe";
            return "scp";
        } else {
            throw new RuntimeException("Current platform is not supported");
        }
    }

    private String getRsyncExecutableValueForPlatform() {
        PlatformUtil.Platform platform = PlatformUtil.getCurrentPlattform();
        if (PlatformUtil.Platform.MAC_OS.equals(platform) || PlatformUtil.Platform.LINUX.equals(platform)) {
            return "rsync";
        } else if (PlatformUtil.Platform.WIN.equals(platform)) {
//            return "rsync.exe";
            return "rsync";
        } else {
            throw new RuntimeException("Current platform is not supported");
        }
    }
}
