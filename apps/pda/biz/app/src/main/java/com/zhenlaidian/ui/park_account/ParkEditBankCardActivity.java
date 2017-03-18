package com.zhenlaidian.ui.park_account;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.MyBankCard;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.person_account.HowToGetSubbranch;
import com.zhenlaidian.ui.person_account.TakePhotoForBankCardActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 添加/修改-银行卡；
 *
 * @author zhangyunfei 2015年8月24日
 */
public class ParkEditBankCardActivity extends BaseActivity {

    private LinearLayout ll_chose_backcard;
    private TextView tv_bankname;
    private ActionBar actionBar;
    private Button bt_bind_done;
    private EditText et_id_number;
    private EditText et_bankcard_number;
    private EditText et_card_address;
    private EditText et_card_branch;
    private TextView tv_name;
    private TextView tv_bind_done_photo_park;//不会绑定点这里
    private EditText et_mobile;
    private TextView tv_how_to_get;// 提示用户怎么获取支行；

    private String bankName; // 银行卡名字
    private String userId; // 身份证号
    private String cardNumber;// 银行卡号
    private String cardAddress; // 开户地
    private String cardBranch; // 开户支行
    private String name; // 姓名
    private String mobile; // 电话
    private MyBankCard bankinfo;
    private boolean isCardBinded = false;
    private int selected = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLog.v("EditBankCardActivity", "onCreate" + "我的银行卡");
        setContentView(R.layout.park_edit_bankcard_activity);
        initView();
        initActionBar();
    }

    public void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        MyLog.v("EditBankCardActivity", "如果为true显示编辑银行卡" + isCardBinded);
        if (isCardBinded) {
            actionBar.setTitle("编辑银行卡");
        } else {
            actionBar.setTitle("添加银行卡");
        }
        actionBar.show();
    }

    private void initView() {
        bankinfo = (MyBankCard) getIntent().getSerializableExtra("mybankcard");
        isCardBinded = getIntent().getBooleanExtra("isCardBinded", false);
        ll_chose_backcard = (LinearLayout) findViewById(R.id.ll_chose_bankcard);
        MyLog.w("TAG", "判断是否有银行卡" + isCardBinded);
        tv_bankname = (TextView) findViewById(R.id.tv_bankname);
        bt_bind_done = (Button) findViewById(R.id.bt_bind_done);
        et_id_number = (EditText) findViewById(R.id.et_id_card);
        et_card_address = (EditText) findViewById(R.id.et_card_address);
        et_card_branch = (EditText) findViewById(R.id.et_card_branch);
        et_bankcard_number = (EditText) findViewById(R.id.et_bankcard_number);
        tv_name = (TextView) findViewById(R.id.tv_name);
        et_mobile = (EditText) findViewById(R.id.et_card_mobile);
        tv_how_to_get = (TextView) findViewById(R.id.tv_how_to_get);
        tv_bind_done_photo_park = (TextView) findViewById(R.id.tv_bind_done_photo_park);

        final String[] items = new String[]{"中国工商银行", "中国建设银行", "中国农业银行", "中国银行", "交通银行", "招商银行", "中国邮政储蓄银行", "中信实业银行",
                "上海浦东发展银行", "民生银行", "光大银行", "广东发展银行", "兴业银行", "华夏银行", "上海银行", "北京银行", "北京农村商业银行"};

        bankName = items[selected];
        tv_bankname.setText(items[selected]);
        if (bankinfo != null) {
            if (bankinfo.getName() != null) {
                tv_name.setText(bankinfo.getName());
            }
            // if (bankinfo.getUser_id() != null) {
            // et_id_number.setText(bankinfo.getUser_id());
            // }
            if (bankinfo.getArea() != null) {
                et_card_address.setText(bankinfo.getArea());
            }
            if (bankinfo.getBank_pint() != null) {
                et_card_branch.setText(bankinfo.getBank_pint());
            }
            // if ( bankinfo.getCard_number()!= null) {
            // et_bankcard_number.setText(bankinfo.getCard_number());
            // }
            if (bankinfo.getMobile() != null) {
                et_mobile.setText(bankinfo.getMobile());
            }
            if (bankinfo.getBank_name() != null) {
                for (int i = 0; i < items.length; i++) {
                    if (bankinfo.getBank_name().equals(items[i])) {
                        selected = i;
                        bankName = items[i];
                        tv_bankname.setText(items[i]);
                    }
                }
            }
        }
        tv_bind_done_photo_park.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 去拍照绑定银行卡；
                Intent intent = new Intent(ParkEditBankCardActivity.this, TakePhotoForBankCardActivity.class);
                startActivity(intent);
            }
        });
        tv_how_to_get.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击去怎么获取支行：
                Intent intent = new Intent(ParkEditBankCardActivity.this, HowToGetSubbranch.class);
                startActivity(intent);
            }
        });
        ll_chose_backcard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AlertDialog.Builder builder = new Builder(ParkEditBankCardActivity.this);
                builder.setTitle("选择银行");

                builder.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        /* 记录选中后的条目所在位置 */
                        selected = which;
                        if (null != tv_bankname) {
                            tv_bankname.setText(items[which]);
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                builder.create().show();
            }
        });

        bt_bind_done.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean flag = false;
                bankName = tv_bankname.getText().toString().trim();
                cardNumber = et_bankcard_number.getText().toString().trim();
                userId = et_id_number.getText().toString().trim();
                cardAddress = et_card_address.getText().toString().trim();
                cardBranch = et_card_branch.getText().toString().trim();
                name = tv_name.getText().toString().trim();
                mobile = et_mobile.getText().toString().trim();
                /* 除去卡号中的空格 */
                cardNumber = cardNumber.replaceAll(" ", "");

                if (TextUtils.isEmpty(userId)) {
                    showTost("请输入身份证号！");
                } else if (TextUtils.isEmpty(cardAddress)) {
                    showTost("请输入开户地！");
                } else if (TextUtils.isEmpty(cardBranch)) {
                    showTost("请输入开户支行！");
                } else if (TextUtils.isEmpty(cardNumber)) {
                    showTost("请输入银行卡号！");
                } else if (TextUtils.isEmpty(name)) {
                    showTost("请输入姓名！");
                } else if (TextUtils.isEmpty(mobile)) {
                    showTost("请输入手机号！");
                } else {
                    Pattern idNumPattern = Pattern.compile("(^((\\d{15})|(\\d{18})|(\\d{17}[xX]))$)");
                    Pattern cardNumPattern = Pattern.compile("(^((\\d{14,20}))$)");
                    Matcher idNumMatcher = idNumPattern.matcher(userId);
                    Matcher cardNumMatcher = cardNumPattern.matcher(cardNumber);
                    Pattern mustChinaPattern = Pattern.compile("^[\u4e00-\u9fa5]*$");
                    flag = mustChinaPattern.matcher(cardAddress).matches();
                    flag &= mustChinaPattern.matcher(cardBranch).matches();
                    if (!idNumMatcher.matches()) {
                        showTost("输入身份证号有误！");
                    } else if (!flag) {
                        showTost("开户地和开户支行内容必须为中文");
                    } else if (!cardNumMatcher.matches()) {
                        showTost("银行卡输入有误！");
                    } else {
                        try {
                            if (isCardBinded) {
                                submitInfo("edit");
                            } else {
                                submitInfo("add");
                            }
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        et_bankcard_number.addTextChangedListener(new TextWatcher() {
            int beforeTextLength = 0;
            int onTextLength = 0;
            boolean isChanged = false;

            int location = 0;// 记录光标的位置
            private char[] tempChar;
            private StringBuffer buffer = new StringBuffer();
            int konggeNumberB = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                MyLog.v("watcher", "before" + s.length() + "start: " + start + "count " + count);
                beforeTextLength = s.length();
                if (buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
                konggeNumberB = 0;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == ' ') {
                        konggeNumberB++;
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MyLog.v("watcher", "onTextChanged" + s.length() + "start: " + start + "count " + count);

                onTextLength = s.length();
                buffer.append(s.toString());
                if (onTextLength == beforeTextLength || onTextLength <= 3 || isChanged) {
                    isChanged = false;
                    return;
                }
                isChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                MyLog.w("watcher", "afterTextChanged");

                if (isChanged) {
                    location = et_bankcard_number.getSelectionEnd();
                    int index = 0;
                    while (index < buffer.length()) {
                        if (buffer.charAt(index) == ' ') {
                            buffer.deleteCharAt(index);
                        } else {
                            index++;
                        }
                    }

                    index = 0;
                    int konggeNumberC = 0;
                    while (index < buffer.length()) {
                        if ((index == 4 || index == 9 || index == 14 || index == 19)) {
                            buffer.insert(index, ' ');
                            konggeNumberC++;
                        }
                        index++;
                    }

                    if (konggeNumberC > konggeNumberB) {
                        location += (konggeNumberC - konggeNumberB);
                    }

                    tempChar = new char[buffer.length()];
                    buffer.getChars(0, buffer.length(), tempChar, 0);
                    String str = buffer.toString();
                    if (location > str.length()) {
                        location = str.length();
                    } else if (location < 0) {
                        location = 0;
                    }

                    et_bankcard_number.setText(str);
                    Editable etable = et_bankcard_number.getText();
                    Selection.setSelection(etable, location);
                    isChanged = false;
                }
            }
        });
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                ParkEditBankCardActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 向服务器发送添加银行卡或修改银行卡信息 type "add" 添加 "edit" 修改
     * <p/>
     * 31、添加车场账户collectorrequest.do?action=addparkbank&token=
     * aa9a48d2f41bb2722f29c8714cbc754c
     * &name=&card_number=&mobile=&bank_name=&area=&bank_pint=user_id=
     * card_number user_id name bank_name area bank_pint mobile 银行卡号 身份证号 姓名
     * 开户银行 开户地 开户网点 手机 返回1，成功，其它失败
     * <p/>
     * 32、修改车场账户 collectorrequest.do?action=editpbank&token=
     * aa9a48d2f41bb2722f29c8714cbc754c
     * &name=&card_number=&mobile=&bank_name=&area=&bank_pint=user_id=&id=
     * 参数：与上一添加接口一样 返回1，成功，其它失败
     */
    public void submitInfo(String type) throws UnsupportedEncodingException {
        // private String bankName; //银行卡名字
        // private String userId; // 身份证号
        // private String cardNumber;// 银行卡号
        // private String cardAddress; // 开户地
        // private String cardBranch; //开户支行
        // private String name; //姓名
        // private String mobile; //电话
        String url = null;
        String id = null;

		/* 避免乱码的出现 */
        bankName = URLEncoder.encode(URLEncoder.encode(bankName, "utf-8"), "utf-8");
        userId = URLEncoder.encode(URLEncoder.encode(userId, "utf-8"), "utf-8");
        cardNumber = URLEncoder.encode(URLEncoder.encode(cardNumber, "utf-8"), "utf-8");
        cardAddress = URLEncoder.encode(URLEncoder.encode(cardAddress, "utf-8"), "utf-8");
        cardBranch = URLEncoder.encode(URLEncoder.encode(cardBranch, "utf-8"), "utf-8");
        name = URLEncoder.encode(URLEncoder.encode(name, "utf-8"), "utf-8");
        mobile = URLEncoder.encode(URLEncoder.encode(mobile, "utf-8"), "utf-8");
        if (bankinfo != null && bankinfo.getId() != null) {
            id = URLEncoder.encode(URLEncoder.encode(bankinfo.getId(), "utf-8"), "utf-8");
        }

        String path = baseurl;
        String addUrl = path + "collectorrequest.do?action=addparkbank&token=" + token + "&name=" + name
                + "&card_number=" + cardNumber + "&mobile=" + mobile + "&bank_name=" + bankName + "&area=" + cardAddress
                + "&bank_pint=" + cardBranch + "&user_id=" + userId;

        String editUrl = path + "collectorrequest.do?action=editpbank&token=" + token + "&name=" + name
                + "&card_number=" + cardNumber + "&mobile=" + mobile + "&bank_name=" + bankName + "&area=" + cardAddress
                + "&bank_pint=" + cardBranch + "&user_id=" + userId + "&id=" + id;
        if (type.equals("add")) {
            url = addUrl;
        } else if (type.equals("edit")) {
            url = editUrl;
        }
        if (IsNetWork.IsHaveInternet(ParkEditBankCardActivity.this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "提交中...", "提交绑定银行卡...", true, true);
            AQuery aQuery = new AQuery(this);
            MyLog.w("EidtBankCardActivity", "绑定银行卡提交的参数：" + url);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    super.callback(url, object, status);
                    if (object != null && object != "") {
                        dialog.dismiss();
                        MyLog.i("EidtBankCardActivity", "绑定银行卡返回的结果：" + object);
                        if (object.equals("1")) {
                            showTost("绑定银行卡成功！");
                            ParkEditBankCardActivity.this.finish();
                            Intent bankcardIntent = new Intent(ParkEditBankCardActivity.this, ParkBankCardActivity.class);
                            startActivity(bankcardIntent);
                        } else {
                            showTost("绑定银行卡失败！");
                        }
                    } else {
                        dialog.dismiss();
                    }
                }

            });
        } else {
            showTost("请检查网络!");
        }
    }

    public void showTost(String info) {
        Toast.makeText(ParkEditBankCardActivity.this, info, 0).show();
    }

}
