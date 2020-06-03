/*
 * Copyright (c) 2020 Zim Launcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.zimmob.zimlx.allapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.aosp.launcher.qsb.AllAppsQsbContainer;

public class AllAppsSearchContainerView extends AllAppsContainerView {

    private boolean mClearQsb;

    public AllAppsSearchContainerView(Context context) {
        this(context, null);
    }

    public AllAppsSearchContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsSearchContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void dispatchDraw(Canvas canvas) {
        View searchView = getSearchView();
        if (mClearQsb && searchView instanceof AllAppsQsbContainer) {
            AllAppsQsbContainer qsb = (AllAppsQsbContainer) searchView;
            int left = (int) (qsb.getLeft() + qsb.getTranslationX());
            int top = (int) (qsb.getTop() + qsb.getTranslationY());
            int right = left + qsb.getWidth() + 1;
            int bottom = top + qsb.getHeight() + 1;
            //if (Utilities.ATLEAST_P && Utilities.HIDDEN_APIS_ALLOWED) {
            //    canvas.saveUnclippedLayer(left, 0, right, bottom);
            //} else {
            int flags = Utilities.ATLEAST_P ? Canvas.ALL_SAVE_FLAG : 0x04 /* HAS_ALPHA_LAYER_SAVE_FLAG */;
            canvas.saveLayer(left, 0, right, bottom, null, flags);
            //}
        }

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}