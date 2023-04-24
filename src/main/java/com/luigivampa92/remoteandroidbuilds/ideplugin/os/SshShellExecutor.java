package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public final class SshShellExecutor {

    private final long PROCESS_AWAITING_TIMEOUT_MS = 5500L;

    public ShellExecutionResult execute(String command, String lineDelimiter) {
        Process process = null;
        try {
            String line;
            StringBuilder output = new StringBuilder();

            process = Runtime.getRuntime().exec(command);
            boolean processExited = process.waitFor(PROCESS_AWAITING_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (processExited) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                    output.append(lineDelimiter);
                }
                BufferedReader readerErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = readerErr.readLine()) != null) {
                    output.append(line);
                    output.append(lineDelimiter);
                }
                int exitCode = process.exitValue();
                return new ShellExecutionResult(exitCode, output.toString());
            } else {
                throw new RuntimeException("Process hanged");
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            return new ShellExecutionResult(255, "");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }
}
