package org.zimmob.zimlx.settings.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import org.zimmob.zimlx.util.ThemeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class MoreInfoActivity extends ThemeActivity {

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utilities.setupPirateLocale(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_more);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Utilities.getZimPrefs(this).getPrimaryColor());
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.about_title);

        MoreInfoFragment moreInfoFragment;
        if (savedInstanceState == null) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            moreInfoFragment = MoreInfoFragment.newInstance();
            t.replace(R.id.more__fragment__placeholder_fragment, moreInfoFragment, MoreInfoFragment.TAG).commit();
        }
    }
}