package com.zhenlaidian.ui.person_account;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;

/**
 * 如何获取支行;
 */
public class HowToGetSubbranch extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.how_to_get_subbranch);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        findViewById(R.id.bt_Iknow).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                HowToGetSubbranch.this.finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                HowToGetSubbranch.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
