package org.zimmob.zimlx.apps;

import org.zimmob.zimlx.model.App;

import java.util.List;

public interface IAppUpdateListener {

    /**
     * @param apps list of apps
     * @return true, if the listener should be removed
     */
    boolean onAppUpdated(List<App> apps);
}
