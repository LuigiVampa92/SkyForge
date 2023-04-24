package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class SimpleMultilineOutputShellExecutor {

    public List<String> execute(String shellCommand) {
        ArrayList<String> result = new ArrayList<>();
        try {
            String line;
            Process process = Runtime.getRuntime().exec(shellCommand);
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                    result.add(line);
                }
            }
        }
        catch (Throwable e) {}
        return result;
    }
}
