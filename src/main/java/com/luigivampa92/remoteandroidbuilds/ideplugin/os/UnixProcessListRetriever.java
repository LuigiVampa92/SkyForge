package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class UnixProcessListRetriever extends BaseProcessListRetriever implements ProcessListRetriever {

    private final String SHELL_COMMAND_GET_PROCESSES = "ps -ef";

    @Override
    public List<ProcessRecord> getProcessList() {
        HashSet<ProcessRecord> result = new HashSet<>();
        try {
            List<String> processListOsCommandResult = shellExecutor.execute(SHELL_COMMAND_GET_PROCESSES);
            if (processListOsCommandResult != null && processListOsCommandResult.size() > 0) {
                for (String line : processListOsCommandResult) {
                    ProcessRecord processRecord = parseProcessLine(line);
                    if (processRecord != null) {
                        result.add(processRecord);
                    }
                }
            }
        } catch (Throwable e) {}
        return new ArrayList<ProcessRecord>(result);
    }

    @Override
    public List<ProcessRecord> getSshProcessesOnPorts(List<Integer> ports) {
        ArrayList<ProcessRecord> result = new ArrayList<>();
        try {
            List<String> processListOsCommandResult = shellExecutor.execute(SHELL_COMMAND_GET_PROCESSES);
            if (processListOsCommandResult != null && processListOsCommandResult.size() > 0) {
                List<String> seekedSshProcesses = filterSshProcessesWithPort(processListOsCommandResult, ports);
                for (String line : seekedSshProcesses) {
                    ProcessRecord processRecord = parseProcessLine(line);
                    if (processRecord != null) {
                        result.add(processRecord);
                    }
                }
            }
        } catch (Throwable e) {}
        return new ArrayList<ProcessRecord>(result);
    }

    private List<String> filterSshProcessesWithPort(List<String> outputLines, List<Integer> ports) {
        ArrayList<String> result = new ArrayList<>();
        for (String line : outputLines) {
            if (line.contains("ssh")) {
                boolean portListIsNotEmpty = ports != null && !ports.isEmpty();
                if (portListIsNotEmpty) {
                    for (Integer portNumber : ports) {
                        String portNumberValue = String.valueOf(portNumber);
                        if (portNumber != null && portNumber > 0 && portNumber < 65536) {
                            if (line.contains(String.format("%s:localhost:%s", portNumberValue, portNumberValue))) {
                                result.add(line);
                            }
                        }
                    }
                } else {
                    result.add(line);
                }
            }
        }
        return result;
    }

    private ProcessRecord parseProcessLine(String line) {
        List<String> tokens = splitValues(line);
        int length = tokens.size();
        if (length >= 8) {

            String pid = tokens.get(1);
            int pidValue = -1;
            try {
                pidValue = Integer.parseInt(pid);
            } catch (NumberFormatException e) {
            }
            if (pidValue <= 0) {
                return null;
            }

            String cmdline = joinValues(tokens.subList(7, length), " ");
            if (cmdline == null || cmdline.isEmpty()) {
                return null;
            }

            return new ProcessRecord(pidValue, cmdline);
        } else {
            return null;
        }
    }
}
