package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.util.List;

public interface ProcessListRetriever {
    List<ProcessRecord> getProcessList();
    List<ProcessRecord> getSshProcessesOnPorts(List<Integer> ports);
}
