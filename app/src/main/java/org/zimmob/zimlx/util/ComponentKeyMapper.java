package org.zimmob.zimlx.util;

/**
 * Copyright (C) 2017 The Android Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.annotation.Nullable;

import java.util.Map;

public class ComponentKeyMapper<T> {

    protected final ComponentKey mComponentKey;

    public ComponentKeyMapper(ComponentKey key) {
        this.mComponentKey = key;
    }

    public @Nullable
    T getItem(Map<ComponentKey, T> map) {
        return map.get(mComponentKey);
    }

    public String getPackage() {
        return mComponentKey.componentName.getPackageName();
    }

    public String getComponentClass() {
        return mComponentKey.componentName.getClassName();
    }

    @Override
    public String toString() {
        return mComponentKey.toString();
    }

}
