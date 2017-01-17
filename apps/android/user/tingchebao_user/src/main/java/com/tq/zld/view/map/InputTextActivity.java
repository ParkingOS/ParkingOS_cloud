package com.tq.zld.view.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.KeyboardUtils;


public class InputTextActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ARG_DATA = "data";

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_text);
        initToolbar();
        initView();
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
        bar.setTitle("添加说明");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        findViewById(R.id.btn_input_text).setOnClickListener(this);

        //初始化EditText
        mEditText = (EditText) findViewById(R.id.et_input_text);
        String data = getIntent().getStringExtra(ARG_DATA);
        if (!TextUtils.isEmpty(data)) {
            mEditText.setText(data);
            mEditText.setSelection(data.length());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardUtils.openKeybord(mEditText, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyboardUtils.closeKeybord(mEditText, this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_input_text) {
            onOKBtnClicked();
        }
    }

    private void onOKBtnClicked() {
        String tips = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(tips)) {
            Toast.makeText(this, "并未添加任何说明！", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        TCBApp.getAppContext().saveString(R.string.sp_remark_tips, tips);
        Intent data = new Intent();
        data.putExtra(ARG_DATA, tips);
        setResult(RESULT_OK, data);
        finish();
    }
}
