package org.zimmob.zimlx.minibar;

import static org.zimmob.zimlx.minibar.DashAction.Action;

public class DashModel {
    public String label;
    public String description;
    public int id;
    public int position;
    public int icon;
    public Action action;

    public DashModel(Action action, String label, String description, int icon, int id) {
        this.action = action;
        this.label = label;
        this.description = description;
        this.icon = icon;
        this.id = id;
    }
}
