package org.zimmob.zimlx.discovery.suggestions;

import android.content.Context;
import android.support.annotation.NonNull;

import org.zimmob.zimlx.Utilities;

public class SuggestionCandidate {
    @NonNull
    private String mPackageName;
    @NonNull
    private String mClassName;

    private int mDayCounter;
    private int mNightCounter;
    private int mHeadphonesCounter;

    SuggestionCandidate(@NonNull String packageName, @NonNull String className) {
        mPackageName = packageName;
        mClassName = className;
        mDayCounter = -1;
        mNightCounter = -1;
        mHeadphonesCounter = -1;
    }

    SuggestionCandidate(@NonNull String packageName, @NonNull String className,
                        int dayCounter, int nightCounter, int headphonesCounter) {
        mPackageName = packageName;
        mClassName = className;
        mDayCounter = dayCounter;
        mNightCounter = nightCounter;
        mHeadphonesCounter = headphonesCounter;
    }

    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    @NonNull
    public String getClassName() {
        return mClassName;
    }

    public int getDayCounter() {
        return mDayCounter;
    }

    public int getNightCounter() {
        return mNightCounter;
    }

    public int getHeadsetCounter() {
        return mHeadphonesCounter;
    }

    public void increaseCounter(Context context) {
        if (Utilities.hasHeadset(context)) {
            mHeadphonesCounter++;
        } else if (Utilities.isDayTime()) {
            mDayCounter++;
        } else {
            mNightCounter++;
        }
    }
}
