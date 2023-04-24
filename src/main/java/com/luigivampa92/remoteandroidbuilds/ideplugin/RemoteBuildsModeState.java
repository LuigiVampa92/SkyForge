package com.luigivampa92.remoteandroidbuilds.ideplugin;

public enum RemoteBuildsModeState {

    ACTIVATED,
    DEACTIVATED,
    PENDING,
    DISABLED;


    // TODO getIcon()

    // todo : remove ??

    public boolean isEnabled() {
        return RemoteBuildsModeState.ACTIVATED.equals(this) || RemoteBuildsModeState.DEACTIVATED.equals(this);
    }

    public boolean isPressed() {
        return RemoteBuildsModeState.ACTIVATED.equals(this);
    }

    public static RemoteBuildsModeState parseValue(String value) {
        for (RemoteBuildsModeState stateValue: values()) {
            if (stateValue.name().equals(value)) {
                return stateValue;
            }
        }
        return DISABLED;
    }

    public static RemoteBuildsModeState defaultValue() {
        return DISABLED;
    }
}
