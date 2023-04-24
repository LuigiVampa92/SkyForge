package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.luigivampa92.remoteandroidbuilds.ideplugin.FileManager;

public final class PlatformComponentFactory {

    private final PlatformUtil.Platform platform;
    private SshExecutor sshExecutor;
    private ProcessListRetriever processListRetriever;
    private ProcessKiller processKiller;

    public PlatformComponentFactory(FileManager fileManager) {
        platform = PlatformUtil.getCurrentPlattform();
        init(fileManager);
    }

    private void init(FileManager fileManager) {
        if (PlatformUtil.Platform.MAC_OS.equals(platform) || PlatformUtil.Platform.LINUX.equals(platform)) {
            processListRetriever = new UnixProcessListRetriever();
            processKiller = new UnixProcessKiller();
        } else if (PlatformUtil.Platform.WIN.equals(platform)) {
            processListRetriever = new WindowsProcessListRetriever();
            processKiller = new WindowsProcessKiller();
        }
        if (PlatformUtil.Platform.MAC_OS.equals(platform) || PlatformUtil.Platform.LINUX.equals(platform) || PlatformUtil.Platform.WIN.equals(platform)) {
            sshExecutor = new SshExecutorImpl(fileManager, processListRetriever, processKiller);
        }
    }

    public SshExecutor getSshExecutor() {
        if (sshExecutor == null) {
            throw provideNoComponentException();
        }
        return sshExecutor;
    }

    public ProcessListRetriever getProcessListRetriever() {
        if (processListRetriever == null) {
            throw provideNoComponentException();
        }
        return processListRetriever;
    }

    public ProcessKiller getProcessKiller() {
        if (processKiller == null) {
            throw provideNoComponentException();
        }
        return processKiller;
    }

    private RuntimeException provideNoComponentException() {
        return new RuntimeException("No component can be provided for your platform: " + platform.name());
    }
}
