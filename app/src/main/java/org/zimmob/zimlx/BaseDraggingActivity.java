package org.zimmob.zimlx;

import android.os.Bundle;
import android.view.View;

public abstract class BaseDraggingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public abstract View getRootView();
}
