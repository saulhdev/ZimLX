/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.google.android.apps.nexuslauncher.reflection.alpha;

public class PredictionUsageHelper {
    private static final PredictionUsageHelper aX = new PredictionUsageHelper("OVERVIEW_GEL");
    private static final PredictionUsageHelper aY = new PredictionUsageHelper("GEL");
    public final String aZ;
    public final String ba;
    public final String bb;
    public final String bc;

    private PredictionUsageHelper(String var1) {
        StringBuilder var2 = new StringBuilder();
        var2.append(var1);
        var2.append("_reflection_last_predictions");
        this.aZ = var2.toString();
        var2 = new StringBuilder();
        var2.append(var1);
        var2.append("_reflection_last_predictions_timestamp");
        this.ba = var2.toString();
        var2 = new StringBuilder();
        var2.append(var1);
        var2.append("_prediction_order");
        this.bb = var2.toString();
        var2 = new StringBuilder();
        var2.append(var1);
        var2.append("_prediction_order_by_rank");
        this.bc = var2.toString();
    }

    public static PredictionUsageHelper d(String var0) {
        return "OVERVIEW_GEL".equals(var0) ? aX : aY;
    }
}
