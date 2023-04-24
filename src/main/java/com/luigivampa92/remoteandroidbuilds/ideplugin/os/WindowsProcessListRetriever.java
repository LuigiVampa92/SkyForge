package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class WindowsProcessListRetriever extends BaseProcessListRetriever implements ProcessListRetriever {

    @Override
    public List<ProcessRecord> getProcessList() {
        HashSet<ProcessRecord> result = new HashSet<>();
        try {
            List<String> processListOsCommandResult = shellExecutor.execute(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            if (processListOsCommandResult != null && processListOsCommandResult.size() > 0) {
                for (String untrimmedLine : processListOsCommandResult) {
                    ProcessRecord processRecord = parseTaskListOutputLine(untrimmedLine);
                    if (processRecord != null) {
                        result.add(processRecord);
                    }
                }
            }
        } catch (Throwable e) {}
        return new ArrayList<ProcessRecord>(result);
    }

    // there is no reliable way to determine what process occupies port on windows if process is run in cygwin or wsl
    // we have to return all running ssh processes
    @Override
    public List<ProcessRecord> getSshProcessesOnPorts(List<Integer> ports) {
        HashSet<ProcessRecord> result = new HashSet<>();
        try {
            List<String> processListOsCommandResult = shellExecutor.execute(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            if (processListOsCommandResult != null && processListOsCommandResult.size() > 0) {
                for (String untrimmedLine : processListOsCommandResult) {
                    ProcessRecord processRecord = parseTaskListOutputLine(untrimmedLine);
                    if (processRecord != null) {
                        if (processRecord.getName().contains("ssh.exe") || processRecord.getName().contains("ssh")) {
                            result.add(processRecord);
                        }
                    }
                }
            }
        } catch (Throwable e) {}
        return new ArrayList<ProcessRecord>(result);
    }

//    @Override
//    public List<ProcessRecord> getSshProcessesOnPorts(List<Integer> ports) {
//        boolean shouldCheckForPorts = ports != null && !ports.isEmpty();
//        List<Integer> processesThatOccupyPorts = new ArrayList<>();
//        if (shouldCheckForPorts) {
//            processesThatOccupyPorts = getProcessIdsThatOccupyPorts(ports);
//        }
//        HashSet<ProcessRecord> result = new HashSet<>();
//        try {
//            List<String> processListOsCommandResult = shellExecutor.execute(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
//            if (processListOsCommandResult != null && processListOsCommandResult.size() > 0) {
//                for (String untrimmedLine : processListOsCommandResult) {
//                    ProcessRecord processRecord = parseTaskListOutputLine(untrimmedLine);
//                    if (processRecord != null) {
//                        if (processRecord.getName().contains("ssh.exe") || processRecord.getName().contains("ssh")) {
//                            if (shouldCheckForPorts) {
//                                int pid = processRecord.getPid();
//                                for (Integer pidThatOccupiesPort : processesThatOccupyPorts) {
//                                    if (pidThatOccupiesPort == pid) {
//                                        result.add(processRecord);
//                                    }
//                                }
//                            } else {
//                                result.add(processRecord);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Throwable e) {}
//        return new ArrayList<ProcessRecord>(result);
//    }
//
//    private List<Integer> getProcessIdsThatOccupyPorts(List<Integer> ports) {
//        HashSet<Integer> resultPids = new HashSet<>();
//        try {
//            List<String> netstatOsCommandResult = shellExecutor.execute("netstat -aon");
//            for(String line : netstatOsCommandResult) {
//                Integer possiblePid = parseNetstatOutputLine(line, ports);
//                if (possiblePid != null) {
//                    resultPids.add(possiblePid);
//                }
//            }
//        } catch (Throwable e) {
//            resultPids = new HashSet<>();
//        }
//        return new ArrayList<Integer>(resultPids);
//    }
//
//    private Integer parseNetstatOutputLine(String outputLine, List<Integer> ports) {
//        if (ports == null || ports.isEmpty()) {
//            return null;
//        }
//        String line = outputLine.trim();
//        int size = line.length();
//
//        boolean shouldParsePid = false;
//        for (Integer port : ports) {
//            if (port == null || port < 1 || port > 65535) {
//                continue;
//            }
//            String portLine = ":" + port;
//            if (line.contains(portLine)) {
//                shouldParsePid = true;
//                break;
//            }
//        }
//        if (!shouldParsePid) {
//            return null;
//        }
//
//        try {
//            int lastSpace = line.lastIndexOf(' ');
//            if (lastSpace != -1 && (size - lastSpace > 1)) {
//                String pidValue = line.substring(lastSpace + 1);
//                int pid = Integer.parseInt(pidValue);
//                if (pid > 0) {
//                    return pid;
//                } else {
//                    return null;
//                }
//            }
//        } catch (Throwable e) {
//            return null;
//        }
//        return null;
//    }

    private ProcessRecord parseTaskListOutputLine(String outputLine) {
        String line = outputLine.trim();
        int size = line.length();
        char charPrevious = '\0';
        int processNameFirstIndex = 0;
        int processNameLastIndex = 0;
        boolean processNameLastIndexAssigned = false;
        int processIdFirstIndex = 0;
        boolean processIdFirstIndexAssigned = false;
        int processIdLastIndex = 0;
        boolean processIdLastIndexAssigned = false;
        for (int i = 0; i < size; ++i) {
            char charCurrent = line.charAt(i);
            if (!processNameLastIndexAssigned && i > 1 && charCurrent == ' ' && charPrevious == ' ') {
                processNameLastIndex = i - 1;
                processNameLastIndexAssigned = true;
            }
            if (processNameLastIndexAssigned && !processIdFirstIndexAssigned && charCurrent >= '0' && charCurrent <= '9' && charPrevious == ' ') {
                processIdFirstIndex = i;
                processIdFirstIndexAssigned = true;
            }
            if (processNameLastIndexAssigned && processIdFirstIndexAssigned && !processIdLastIndexAssigned && (charCurrent == ' ' || (size - 1 == i))) {
                processIdLastIndex = i;
                if (size - 1 == i) {
                    processIdLastIndex = size;
                }
                processIdLastIndexAssigned = true;
            }
            charPrevious = charCurrent;
        }

        if (
                processNameLastIndex != 0 && processNameLastIndexAssigned
                        && processIdFirstIndex != 0 && processIdFirstIndexAssigned
                        && processIdLastIndex != 0 && processIdLastIndexAssigned
        ) {

            String pid = line.substring(processIdFirstIndex, processIdLastIndex);
            int pidValue = -1;
            try {
                pidValue = Integer.parseInt(pid);
            } catch (NumberFormatException e) {
            }
            if (pidValue <= 0) {
                return null;
            }

            String cmdline = line.substring(processNameFirstIndex, processNameLastIndex);
            if (cmdline == null || cmdline.isEmpty()) {
                return null;
            }

            return new ProcessRecord(pidValue, cmdline);
        } else {
            return null;
        }
    }
}
