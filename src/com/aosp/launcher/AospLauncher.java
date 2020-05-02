/*
 * Copyright (C) 2019 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aosp.launcher;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;

public class AospLauncher extends Launcher {

    public AospLauncher() {
        setLauncherCallbacks(new AospLauncherCallbacks(this));
    }

    public LauncherCallbacks getLauncherCallbacks() {
        return mLauncherCallbacks;
    }
}