package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

public final class ShellExecutionResult {

    private final int exitCode;
    private final String output;

    public ShellExecutionResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }
}
