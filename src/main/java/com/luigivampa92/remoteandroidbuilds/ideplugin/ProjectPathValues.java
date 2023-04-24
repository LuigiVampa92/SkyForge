package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.github.markusbernhardt.proxy.util.PlatformUtil;

import java.util.Objects;

public final class ProjectPathValues {

    private final String dir;
    private final String path;

    public ProjectPathValues(String dir, String path) {
        this.dir = dir;
        this.path = path;
    }

    public String getDir() {
        return dir;
    }

    public String getPath() {
        return path;
    }

    public boolean osAwareEquals(Object o) {
        PlatformUtil.Platform platform = PlatformUtil.getCurrentPlattform();
        if (PlatformUtil.Platform.WIN.equals(platform)) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProjectPathValues that = (ProjectPathValues) o;
            return Objects.equals(dir, that.dir);
        } else {
            return this.equals(o);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectPathValues that = (ProjectPathValues) o;
        return Objects.equals(dir, that.dir) &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir, path);
    }
}
