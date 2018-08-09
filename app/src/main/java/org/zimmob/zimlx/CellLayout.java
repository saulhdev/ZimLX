package org.zimmob.zimlx;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CellLayout {
    public static final int WORKSPACE = 0;
    public static final int HOTSEAT = 1;
    public static final int FOLDER = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WORKSPACE, HOTSEAT, FOLDER})
    public @interface ContainerType {
    }
}
