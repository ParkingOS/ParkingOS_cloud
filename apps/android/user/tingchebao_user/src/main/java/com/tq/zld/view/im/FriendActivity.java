package com.tq.zld.view.im;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tq.zld.R;
import com.tq.zld.util.ToastUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.fragment.FriendFragment;

/**
 * Created by GT on 2015/9/17.
 */
public class FriendActivity extends BaseActivity {
    public static final String ARGS_PLAY_AGAIN = "args_play_again";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initToolbar();
        FriendFragment fragment = new FriendFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment,
                        fragment.getClass().getSimpleName()).commit();
    }

    private void initToolbar() {
        Toolbar mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        setSupportActionBar(mBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(FriendActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_ADD_FRIEND);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(ARGS_PLAY_AGAIN,false)) {
            ToastUtils.show(FriendActivity.this, "选择合并好友");
        }
    }
}
