package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

public final class UnixProcessKiller implements ProcessKiller {

    private static final String TEMPLATE_KILL_PROCESS_CMD_NORMAL = "kill %s";
    private static final String TEMPLATE_KILL_PROCESS_CMD_FORCE = "kill -9 %s";

    private final RcOnlyShellExecutor shellExecutor = new RcOnlyShellExecutor();

    @Override
    public void kill(int pid, boolean force) {
        String commandTemplate = force ? TEMPLATE_KILL_PROCESS_CMD_FORCE : TEMPLATE_KILL_PROCESS_CMD_NORMAL;
        String command = String.format(commandTemplate, String.valueOf(pid));
        ShellExecutionResult result = shellExecutor.execute(command);
    }
}
