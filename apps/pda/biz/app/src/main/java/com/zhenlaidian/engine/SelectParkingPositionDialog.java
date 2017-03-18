package com.zhenlaidian.engine;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 自定义选择停车位的dialog
 * Created by zhangyunfei on 15/10/28.
 */
public class SelectParkingPositionDialog extends Dialog {

    private TextView tv_carnumber;
    private ListView lv_parking_position;
    private CheckBox cb_remind;
    private Button bt_wait;//晚点再说
    private Button bt_touser;//让车主自己填
    protected Context context;
    protected ArrayList<ParkPosition> parklists;
    protected String carnumber;
    protected String orderid;
    GetParkPosition plistener;

    public SelectParkingPositionDialog(Context context, String carnumber,String orderid, GetParkPosition plistener) {
        super(context);
        this.context = context;
        this.carnumber = carnumber;
        this.orderid = orderid;
        this.plistener = plistener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_parking_position);
        this.setCanceledOnTouchOutside(false);
        initVeiw();
        getParkLists();
        setView();
    }

    public void initVeiw() {
        tv_carnumber = (TextView) findViewById(R.id.tv_select_parking_position_carnumber);
        lv_parking_position = (ListView) findViewById(R.id.lv_select_parking_position);
        cb_remind = (CheckBox) findViewById(R.id.cb_select_parking_position_flag);
        bt_wait = (Button) findViewById(R.id.bt_select_parking_position_wait);
        bt_touser = (Button) findViewById(R.id.bt_select_parking_position_touser);
        if (SharedPreferencesUtils.getIntance(context).getSelectParkPosition()){
            cb_remind.setChecked(true);
        }else{
            cb_remind.setChecked(false);
        }
    }

    public void setView() {
        if (!TextUtils.isEmpty(carnumber)){
            tv_carnumber.setText(carnumber);
        }
        cb_remind.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        SharedPreferencesUtils.getIntance(context).setSelectParkPosition(true);
                    }else{
                        SharedPreferencesUtils.getIntance(context).setSelectParkPosition(false);
                    }
            }
        });
        bt_wait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击晚点再说,关闭对话框
                SelectParkingPositionDialog.this.dismiss();
            }
        });
        bt_touser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击让车主自己填写车牌
            }
        });
        lv_parking_position.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //车位条目点击事件,根据订单号绑定该车位;
                setParkPosition(orderid, parklists.get(position).id, parklists.get(position).name);
            }
        });
    }

    //查询车位 collectorrequest.do?action=getfreeparks&token=6f56758f82c1ccf17d4519918339dc2c
    public void getParkLists() {
        String url = Config.getUrl(context)+"collectorrequest.do?action=getfreeparks&token="+BaseActivity.token;
        MyLog.i("SelectParkingPosition","查询车位的url:"+url);
        AQuery aQuery = new AQuery(context);
        final ProgressDialog dialog = ProgressDialog.show(context, "获取车位列表", "获取中...", false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                dialog.dismiss();
                MyLog.d("SelectParkingPosition", "查询车位返回结果:" + object);
                if (!TextUtils.isEmpty(object)) {
                    try {
                        JSONObject jsonObject = new JSONObject(object);
                       if("1".equals(jsonObject.get("result"))){
                           Gson gson = new Gson();
                           parklists = gson.fromJson(jsonObject.get("info").toString(), new TypeToken<ArrayList<ParkPosition>>(){
                           }.getType());
                           if (parklists != null && parklists.size() > 0) {
                               lv_parking_position.setAdapter(new ParkingListAdapter(parklists));
                           } else {
                               Toast.makeText(context, "获取车位列表失败!", Toast.LENGTH_SHORT).show();
                           }
                       }else{
                           Toast.makeText(context, jsonObject.get("errmsg").toString(), Toast.LENGTH_LONG).show();
                       }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //绑定车位collectorrequest.do?action=bondcarpark&token=6f56758f82c1ccf17d4519918339dc2c&orderid=11111&id=133
    public void setParkPosition(String orderid,String id, final String position) {
        String url = Config.getUrl(context)+"collectorrequest.do?action=bondcarpark&token="+BaseActivity.token
                +"&orderid="+orderid+"&id="+id;
        MyLog.i("SelectParkingPositionDialog", "绑定车位的url:" + url);
        AQuery aQuery = new AQuery(context);
        final ProgressDialog dialog = ProgressDialog.show(context, "绑定车位", "绑定中...", false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                dialog.dismiss();
                MyLog.d("SelectParkingPositionDialog", "绑定车位返回结果:" + object);
                if (!TextUtils.isEmpty(object)) {
                    try {
                        JSONObject jsonObject = new JSONObject(object);
                        String result = jsonObject.getString("result");
                        if ("1".equals(result)){
                            if (plistener != null){
                                plistener.getParkPosition(position);
                            }
                            Toast.makeText(context, "绑定车位成功!", Toast.LENGTH_SHORT).show();
                            SelectParkingPositionDialog.this.dismiss();
                        }else{
                            Toast.makeText(context, "绑定车位失败!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "绑定车位解析异常!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private class ParkingListAdapter extends BaseAdapter {

        public ArrayList<ParkPosition> infos;

        public ParkingListAdapter(ArrayList<ParkPosition> infos) {
            this.infos = infos;
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv_position;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_select_park_position, null);
                tv_position = (TextView) convertView.findViewById(R.id.tv_item_select_park_position);
                convertView.setTag(tv_position);
            } else {
                tv_position = (TextView) convertView.getTag();
            }

            tv_position.setText(infos.get(position).name);

            return convertView;
        }
    }

    class ParkPosition {

        String id;
        String name;

    }
}

