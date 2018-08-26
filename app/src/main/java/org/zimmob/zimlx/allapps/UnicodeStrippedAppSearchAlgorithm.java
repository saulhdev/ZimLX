package org.zimmob.zimlx.allapps;

import org.zimmob.zimlx.AppInfo;
import org.zimmob.zimlx.LauncherAppState;
import org.zimmob.zimlx.util.UnicodeFilter;

import java.util.List;

/**
 * A search algorithm that changes every non-ascii characters to theirs ascii equivalents and
 * then performs comparison.
 */
public class UnicodeStrippedAppSearchAlgorithm extends DefaultAppSearchAlgorithm {
    public UnicodeStrippedAppSearchAlgorithm(List<AppInfo> apps) {
        super(apps);
    }

    @Override
    protected boolean matches(AppInfo info, String query) {
        if (info.componentName.getPackageName().equals(LauncherAppState.getInstanceNoCreate().getContext().getPackageName()))
            return false;

        String title = UnicodeFilter.filter(info.title.toString().toLowerCase());
        String strippedQuery = UnicodeFilter.filter(query.trim());

        return super.matches(title, strippedQuery);
    }
}
