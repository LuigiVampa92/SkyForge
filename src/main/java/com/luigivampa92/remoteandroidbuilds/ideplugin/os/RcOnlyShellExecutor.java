package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class RcOnlyShellExecutor {

    private final int PROCESS_AWAITING_TIMEOUT_MS = 3000;

    public synchronized ShellExecutionResult execute(String command) {
        return execute(command, PROCESS_AWAITING_TIMEOUT_MS);
    }

    public synchronized ShellExecutionResult execute(String command, int timeoutMs) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            boolean processExited = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (processExited) {
                int exitCode = process.exitValue();
                return new ShellExecutionResult(exitCode, null);
            } else {
                throw new RuntimeException("Process hanged");
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            return new ShellExecutionResult(255, null);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }
}
