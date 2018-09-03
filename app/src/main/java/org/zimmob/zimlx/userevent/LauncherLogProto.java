package org.zimmob.zimlx.userevent;

/*
 * Copyright (C) 2016 The Android Open Source Project
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
public class LauncherLogProto {
    // Used to define what type of item a Target would represent.
    enum ItemType {
        DEFAULT_ITEMTYPE,
        APP_ICON,
        SHORTCUT,
        WIDGET,
        FOLDER_ICON,
        DEEPSHORTCUT,
        SEARCHBOX,
        EDITTEXT,
        NOTIFICATION,;
    }

    // Used to define what type of container a Target would represent.
    enum ContainerType {
        DEFAULT_CONTAINERTYPE,
        WORKSPACE,
        HOTSEAT,
        FOLDER,
        ALLAPPS,
        WIDGETS,
        OVERVIEW,
        PREDICTION,
        SEARCHRESULT,
        DEEPSHORTCUTS,
        PINITEM,    // confirmation screen
    }

    // Used to define what type of control a Target would represent.
    enum ControlType {
        DEFAULT_CONTROLTYPE,
        ALL_APPS_BUTTON,
        WIDGETS_BUTTON,
        WALLPAPER_BUTTON,
        SETTINGS_BUTTON,
        REMOVE_TARGET,
        UNINSTALL_TARGET,
        APPINFO_TARGET,
        RESIZE_HANDLE,
        VERTICAL_SCROLL,
        HOME_INTENT, // Deprecated, use enum Command instead
        BACK_BUTTON, // Deprecated, use enum Command instead
        // GO_TO_PLAYSTORE
    }
}
