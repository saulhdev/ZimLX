package org.zimmob.zimlx.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.zimmob.zimlx.launcher.Launcher;
import org.zimmob.zimlx.util.Tool;

import java.util.Calendar;

/**
 * Created by BennyKok on 9/9/2017.
 */

public class CalendarDropDownView extends CardView implements View.OnClickListener {

    private MaterialCalendarView calendarView;
    private boolean stateOpened = false;

    public CalendarDropDownView(Context context) {
        super(context);
        init();
    }

    public CalendarDropDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        calendarView = new MaterialCalendarView(getContext());
        addView(calendarView);
        int twoDp = Tool.dp2px(2, getContext());
        setContentPadding(0, twoDp * 5, 0, 0);
        calendarView.setTileHeightDp(40);

        Tool.invisibleViews(calendarView);
        setScaleY(0);
    }

    public void animateShow() {
        if (stateOpened) return;
        assert Launcher.Companion.getLauncher() != null;
        Tool.invisibleViews(Launcher.Companion.getLauncher().getSearchBar());
        Launcher.Companion.getLauncher().dimBackground();
        Launcher.Companion.getLauncher().clearRoomForPopUp();
        Launcher.Companion.getLauncher().getBackground().setOnClickListener(this);
        calendarView.setSelectedDate(Calendar.getInstance());
        stateOpened = true;
        animate().scaleY(1).setDuration(200).withEndAction(() -> Tool.visibleViews(200, calendarView));
    }

    public void animateHide() {
        if (!stateOpened) return;
        Tool.visibleViews(Launcher.Companion.getLauncher().getSearchBar());
        Launcher.Companion.getLauncher().unDimBackground();
        Launcher.Companion.getLauncher().unClearRoomForPopUp();
        Launcher.Companion.getLauncher().getBackground().setOnClickListener(null);
        stateOpened = false;
        Tool.invisibleViews(200, calendarView);
        animate().scaleY(0).setStartDelay(200).setDuration(200);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        ((MarginLayoutParams) getLayoutParams()).topMargin = insets.getSystemWindowInsetTop();
        return insets;
    }

    @Override
    public void onClick(View view) {
        animateHide();
    }
}
