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

package com.google.android.apps.nexuslauncher.reflection;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ReflectionTimeHelper {
    public static final List<String> ALL_FILES = Collections.unmodifiableList(Arrays.asList("reflection.engine", "reflection.engine.background", "model.properties.xml", "reflection_multi_process.xml", "client_actions"));
    public static final Pattern ai = Pattern.compile("^([^/]+)/([^#/]+)(#(\\d+))?$");
    public static final long aj;
    public static final long ak;

    static {
        aj = TimeUnit.SECONDS.toMillis(1L);
        ak = TimeUnit.SECONDS.toMillis(3L);
    }

    public static String a(ComponentName var0) {
        return var0.flattenToString();
    }

    public static String a(String var0, String var1) {
        return String.format("%s%s/%s", "_", var0, var1);
    }

    public static SharedPreferences d(Context var0) {
        return var0.getSharedPreferences("reflection.private.properties", 0);
    }
}
