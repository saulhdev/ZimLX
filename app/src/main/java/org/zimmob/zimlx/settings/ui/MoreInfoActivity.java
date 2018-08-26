package org.zimmob.zimlx.settings.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.Utilities;
import org.zimmob.zimlx.minibar.ThemeActivity;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class MoreInfoActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Utilities.getPrefs(this).getPrimaryColor());
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MoreInfoFragment moreInfoFragment;
        if (savedInstanceState == null) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            moreInfoFragment = MoreInfoFragment.newInstance();
            t.replace(R.id.more__fragment__placeholder_fragment, moreInfoFragment, MoreInfoFragment.TAG).commit();
        } else {
            moreInfoFragment = (MoreInfoFragment) getSupportFragmentManager().findFragmentByTag(MoreInfoFragment.TAG);
        }
    }

}