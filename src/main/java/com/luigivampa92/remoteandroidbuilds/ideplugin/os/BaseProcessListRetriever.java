package com.luigivampa92.remoteandroidbuilds.ideplugin.os;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseProcessListRetriever {

    protected final SimpleMultilineOutputShellExecutor shellExecutor = new SimpleMultilineOutputShellExecutor();

    protected List<String> splitValues(String line) {
        ArrayList<String> result = new ArrayList<>();
        String[] tokens = line.split(" ");
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                result.add(token);
            }
        }
        return result;
    }

    protected String joinValues(List<String> values, String delimeter) {
        int size = values.size();
        if (size == 0) {
            return "";
        } else if (size == 1) {
            return values.get(0);
        } else {
            StringBuilder builder = new StringBuilder(values.get(0));
            for (int i = 1; i < size; ++i) {
                builder.append(delimeter);
                builder.append(values.get(i));
            }
            return builder.toString();
        }
    }
}
