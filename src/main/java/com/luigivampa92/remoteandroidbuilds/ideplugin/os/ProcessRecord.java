package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

public final class ProcessRecord {

    private final int pid;
    private final String name;

    public ProcessRecord(int pid, String name) {
        this.pid = pid;
        this.name = name;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }
}
