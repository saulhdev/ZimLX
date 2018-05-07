package org.zimmob.zimlx.interfaces;

import org.zimmob.zimlx.model.App;

import java.util.List;

public interface AppDeleteListener {
    /**
     * @param apps list of apps
     * @return true, if the listener should be removed
     */
    boolean onAppDeleted(List<App> apps);
}
