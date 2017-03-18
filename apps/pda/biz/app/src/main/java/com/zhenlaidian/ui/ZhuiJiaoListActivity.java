package com.zhenlaidian.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.zhenlaidian.R;
import com.zhenlaidian.adapter.ImgAdapter;
import com.zhenlaidian.adapter.ZhuiJiaoListAdapter;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.fragment.FragmentShowIMG;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TCB on 2016/4/17.
 * xulu
 */
public class ZhuiJiaoListActivity extends BaseActivity implements ZhuiJiaoListAdapter.onClickImg {
    private ArrayList<ZhuiJiaoItemEntity> entity = new ArrayList<ZhuiJiaoItemEntity>();
//    public static ZhuiJiaoListActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_zhuijiao_list_activity);
        entity = (ArrayList<ZhuiJiaoItemEntity>) getIntent().getSerializableExtra("list");
//        instance = this;
        initView();
    }

    private ZhuiJiaoListAdapter adapter;
    private ListView listview;
    private RelativeLayout ln_orderdetail_vp;
    private ViewPager vp_orderdetail;
    private ImageView po1, po2, po3;
    private Button btnfra;

    private void initView() {
        listview = ((ListView) findViewById(R.id.listView));
        findViewById(R.id.zhuijiao_txt_print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PullMsgService.CanPrint) {
                    print();
                } else {
                    CommontUtils.toast(context, "打印机连接失败");
                    FinishAction();
                }
            }
        });
        findViewById(R.id.zhuijiao_txt_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下次再说
                putBooleanToPreference("next", true);
                FinishAction();
            }
        });
        findViewById(R.id.zhuijiao_txt_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //立即补缴
                Intent i = new Intent(context, ZhuiJiaoOrderActivity.class);
                i.putExtra("list", entity);
                startActivityForResult(i, 21);
            }
        });
        adapter = new ZhuiJiaoListAdapter(context, entity, this);
        listview.setAdapter(adapter);

        vp_orderdetail = ((ViewPager) findViewById(R.id.vp_orderdetail));
        ln_orderdetail_vp = ((RelativeLayout) findViewById(R.id.ln_orderdetail_vp));
        po1 = ((ImageView) findViewById(R.id.gallery_p1));
        po2 = ((ImageView) findViewById(R.id.gallery_p2));
        po3 = ((ImageView) findViewById(R.id.gallery_p3));
        btnfra = ((Button) findViewById(R.id.fragment_btn));
        btnfra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ln_orderdetail_vp.setVisibility(View.GONE);
            }
        });
         vpadapter = new ImgAdapter(getSupportFragmentManager(), listFra, context);
     vp_orderdetail.setAdapter(vpadapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 21) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void print() {
        /**
         * 序号 1
         欠费时间     2016-04-28 15:23
         停车时长   34分钟
         金额    4元
         */
        double total = 0;
        double pre = 0;
        String printstr = Constant.OWEHead;
        printstr += "车牌号：" + entity.get(0).getCar_number() + "\n";
        for (int i = 0; i < entity.size(); i++) {
            printstr += "序号：" + (i + 1) + "\n" +
                    "欠费泊位段：" + entity.get(i).getBerthsec_name() + "\n" +
                    "进场时间：" + CommontUtils.Unix2TimeS(entity.get(i).getStart()) + "\n" +
                    "出场时间：" + CommontUtils.Unix2TimeS(entity.get(i).getEnd()) + "\n" +
                    "停车时长：" + entity.get(i).getDuartion() + "\n" +
                    "欠费金额：" + CommontUtils.doubleTwoPoint(Double.parseDouble(entity.get(i).getTotal()) - Double.parseDouble(entity.get(i).getPrepay())) + "\n" +
                    "状态：" + (entity.get(i).ischeck() ? "已缴" : "未缴") +
                    "\n";
            total += Double.parseDouble(entity.get(i).getTotal());
            pre += Double.parseDouble(entity.get(i).getPrepay());
        }
        printstr += "欠费总额：" + CommontUtils.doubleTwoPoint(total - pre) + "\n" +
                Constant.FOOT + "\n" + "\n" + "\n";
        PullMsgService.sendMessage(printstr, context);
        FinishAction();
    }

    private void FinishAction() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (CommontUtils.checkString(getIntent().getStringExtra("from"))) {
            if (getIntent().getStringExtra("from").equals("jiesuan")) {
                FinishAction();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    int currentpoint = 0;
    @Override
    public void ClickImg(List<String> paths) {
        ShowImgFragment(paths);
    }
    ImgAdapter vpadapter;
    public void ShowImgFragment(List<String> paths) {
        ln_orderdetail_vp.setVisibility(View.VISIBLE);
        listFra.clear();
        if(vpadapter.listFra!=null&&vpadapter.listFra.size()>0){
            for(int i=0;i<vpadapter.listFra.size();i++){
                vpadapter.listFra.remove(0);
            }
        }

        for (int i = 0; i < paths.size(); i++) {
            Log.i("tmp",i+":------------"+paths.get(i));
            Fragment fragment = new FragmentShowIMG();
            Bundle b = new Bundle();
            b.putString("path", paths.get(i));
            fragment.setArguments(b);
            listFra.add(fragment);
        }
//        ImgAdapter adapter = new ImgAdapter(getSupportFragmentManager(), listFra, context);
//     vp_orderdetail.setAdapter(adapter);
        vpadapter.notifyDataSetChanged();
        if(currentpoint<=listFra.size()){
            setPoint(listFra.size(), currentpoint);
        }else{
            setPoint(listFra.size(), 0);
        }

        vp_orderdetail.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setPoint(listFra.size(), position);
                currentpoint = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setPoint(int count, int index) {
        switch (count) {
            case 1:
                po1.setVisibility(View.VISIBLE);
                po2.setVisibility(View.GONE);
                po3.setVisibility(View.GONE);
                po1.setBackgroundResource(R.drawable.splash_focus);
                break;
            case 2:
                po1.setVisibility(View.VISIBLE);
                po2.setVisibility(View.VISIBLE);
                po3.setVisibility(View.GONE);
                switch (index) {
                    case 0:
                        po1.setBackgroundResource(R.drawable.splash_focus);
                        po2.setBackgroundResource(R.drawable.splash_blur);
                        break;
                    case 1:
                        po1.setBackgroundResource(R.drawable.splash_blur);
                        po2.setBackgroundResource(R.drawable.splash_focus);
                        break;
                }
                break;
            case 3:
                po1.setVisibility(View.VISIBLE);
                po2.setVisibility(View.VISIBLE);
                po3.setVisibility(View.VISIBLE);
                switch (index) {
                    case 0:
                        po1.setBackgroundResource(R.drawable.splash_focus);
                        po2.setBackgroundResource(R.drawable.splash_blur);
                        po3.setBackgroundResource(R.drawable.splash_blur);
                        break;
                    case 1:
                        po1.setBackgroundResource(R.drawable.splash_blur);
                        po2.setBackgroundResource(R.drawable.splash_focus);
                        po3.setBackgroundResource(R.drawable.splash_blur);
                        break;
                    case 2:
                        po1.setBackgroundResource(R.drawable.splash_blur);
                        po2.setBackgroundResource(R.drawable.splash_blur);
                        po3.setBackgroundResource(R.drawable.splash_focus);
                        break;
                }
                break;
        }
    }

    private List<Fragment> listFra = new ArrayList<Fragment>();
}
