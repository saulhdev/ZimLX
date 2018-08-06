package org.zimmob.zimlx.icon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.model.Item;

public class EditIconActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_ICON = 0;
    private Item mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_icon);
        mInfo = getIntent().getExtras().getParcelable("itemInfo");
        if (mInfo == null) {
            finish();
            return;
        }
        /*INICIO TEST VIEW*/
        if (mInfo.getType() == Item.Type.APP) {
            RecyclerView iconRecyclerView = findViewById(R.id.iconRecyclerView);
            iconRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            RecyclerView iconPackRecyclerView = findViewById(R.id.iconPackRecyclerView);
            iconPackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            findViewById(R.id.iconRecyclerView).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.GONE);
        }
        /*FIN TEST VIEW*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reset) {
            resetIcon();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_ICON) {
            if (resultCode == RESULT_OK) {
                String packageName = data.getStringExtra("packageName");
                String resourceName = data.getStringExtra("resource");
                setAlternateIcon("resource/" + packageName + "/" + resourceName);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setAlternateIcon(String alternateIcon) {
        Intent data = new Intent();
        data.putExtra("alternateIcon", alternateIcon);
        setResult(RESULT_OK, data);
        finish();
    }

    private void resetIcon() {
        Intent data = new Intent();
        data.putExtra("alternateIcon", "-1");
        setResult(RESULT_OK, data);
        finish();
    }
}
