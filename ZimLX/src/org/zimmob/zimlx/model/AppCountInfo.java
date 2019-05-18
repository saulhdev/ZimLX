package org.zimmob.zimlx.model;

public class AppCountInfo {
    private String packageName;
    private int count;

    public AppCountInfo(String name, int count) {
        this.packageName = name;
        this.count = count;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
