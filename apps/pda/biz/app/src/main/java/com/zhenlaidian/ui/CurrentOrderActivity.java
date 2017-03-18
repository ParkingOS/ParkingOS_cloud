package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.CurrentOrderAdapter;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.bean.HistoryOrder;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.photo.InputCarNumberActivity;
import com.zhenlaidian.photo.MyCaptureActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 当前订单界面;
 */
@SuppressLint("DefaultLocale")
public class CurrentOrderActivity extends BaseActivity {

    private ListView lv_current_order;
    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    private TextView tv_currnet_null;
    private TextView tv_total_number;
    private ImageView iv_call_phone;
    private TextView tv_total_money;
    private CurrentOrderAdapter adapter;
    private int pagenumber = 1;
    private final int size = 10;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;
    private int count = 0;
    private int visiblecount = 0;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_order_activity);
        inintView();
        initActionBar();
    }


    public void inintView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.current_order_layout);
        lv_left_drawer = (ListView) findViewById(R.id.left_drawer);
        iv_call_phone = (ImageView) findViewById(R.id.iv_carrent_order_phone);
        tv_currnet_null = (TextView) findViewById(R.id.tv_current_order_null);
        tv_total_number = (TextView) findViewById(R.id.tv_current_order_total_number);
        tv_total_money = (TextView) findViewById(R.id.tv_current_order_total_money);
        adapter = new CurrentOrderAdapter(CurrentOrderActivity.this);
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), CurrentOrderActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout,this));
        lv_left_drawer.setScrollingCacheEnabled(false);//设置抽屉的listview不能滑动；
        lv_current_order = (ListView) findViewById(R.id.lv_current_order);
        // 订单条目的点击事件。
        lv_current_order.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                adapter.onItemClick(position, CurrentOrderActivity.this);
            }
        });
        lv_current_order.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    // 当不滚动时
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            if (count != visiblecount) {
                                adapter.onItemClick(view.getLastVisiblePosition(), CurrentOrderActivity.this);
                            } else {
                                if (count != 0) {
                                    AllOrder allOrders = adapter.getAllOrders(count);
                                    if (allOrders == null) {
                                        adapter.onItemClick(view.getLastVisiblePosition(), CurrentOrderActivity.this);
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                visiblecount = totalItemCount;
            }
        });
        // 订单条目的长按点击事件。
        lv_current_order.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO 长按删除订单条目
                adapter.onItemLongClick(position, CurrentOrderActivity.this);
                return true;
            }
        });
        iv_call_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "01056450585"));
                startActivity(phoneintent);
            }
        });
    }

    public void setView(String number, String money) {
        count = Integer.parseInt(number);
        tv_total_number.setText(number);
        //		double d = Double.parseDouble(money);
        //		String result = String .format("%.2f",d);
        //		tv_total_money.setText(result);
    }

    @SuppressWarnings("deprecation")
    public void initActionBar() {
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer_am, R.string.hello_world, R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle("当前订单");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * 抽屉的监听
     */
    @SuppressWarnings("deprecation")
    private class MyDrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {// 打开抽屉的回调
            mDrawerToggle.onDrawerOpened(drawerView);
            actionBar.setTitle("停车宝");
        }

        @Override
        public void onDrawerClosed(View drawerView) {// 关闭抽屉的回调
            mDrawerToggle.onDrawerClosed(drawerView);
            actionBar.setTitle("当前订单");
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {// 抽屉滑动的回调
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {// 抽屉状态改变的回调
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    public void setNullView() {
        tv_currnet_null.setVisibility(View.VISIBLE);
        lv_current_order.setVisibility(View.INVISIBLE);
    }

    // 从网络上获取当前订单；
    public void getOrder() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", 0).show();
            tv_currnet_null.setVisibility(View.VISIBLE);
            tv_currnet_null.setText("获取订单失败，请检查网络！");
            lv_current_order.setVisibility(View.INVISIBLE);
            return;
        }
        AQuery aQuery = new AQuery(this);
        //http://s.zhenlaidian.com/zld/collectorrequest.do?action=currorders&token=*&page=*&size=*
        String path = baseurl;
        String url = path + "collectorrequest.do?action=currorders&token=" + token
                + "&page=" + pagenumber + "&size=" + size + "&out=" + "json";
        MyLog.w("获取当前订单URL---->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取当前订单数据...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @SuppressLint("DefaultLocale")
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    dialog.dismiss();
                    MyLog.v("当前订单访问页码为", pagenumber + "");
                    System.out.println("订单数据数组的长度" + object.length());
                } else {
                    dialog.dismiss();
                    return;
                }
                if (object != null && object.length() > 110) {
                    MyLog.d("获取到当前订单为", object);
                    Gson gson = new Gson();
                    HistoryOrder orders = gson.fromJson(object, HistoryOrder.class);
                    System.out.println("获取到当前订单的集合的长度" + orders.getInfo().size());
                    MyLog.i("解析到当前订单为", "-->>" + orders.toString());
                    if (orders == null || orders.getInfo().size() == 0) {
                        return;
                    }
                    int total = 0;
                    if (orders.getCount() != null) {
                        total = Integer.parseInt(orders.getCount());
                    }
                    if (orders.getCount() != null && orders.getPrice() != null) {
                        setView(orders.getCount(), orders.getPrice());
                    }
                    adapter.addOrders(orders.getInfo(), total, CurrentOrderActivity.this);
                } else {
                    adapter.getOrdes(CurrentOrderActivity.this);
                }
            }

        });

    }

    public void setAdapter() {
        lv_current_order.setAdapter(adapter);
    }

    public void setPageNumber() {
        pagenumber++;
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawers();
                } else {

                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//	 //ActionBar添加菜单项
//	 @Override
//	 public boolean onCreateOptionsMenu(Menu menu) {
//	 super.onCreateOptionsMenu(menu);
//	 //添加菜单项；
//	 MenuItem add = menu.add(0, 0, 0, "add");
//	 //绑定到ActionBar
//	 add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//	 return true;
//	 }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    public void onResume() {
        super.onResume();
        getOrder();// 从网络上获取数据。
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        adapter.removeOrders();
        count = 0;
        pagenumber = 1;

    }

    //选择当前订单长按的操作；
    public void OrderNotNumberDialog(final String time, final String orderid) {
        final CharSequence[] items = {"扫描车牌添加", "手动输入车牌添加"};
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("添加车牌号");
        builder.setItems(items, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    MyLog.v("CurrentOrderActivity", "扫描车牌添加车牌");
                    Intent intent = new Intent(CurrentOrderActivity.this, MyCaptureActivity.class);
                    intent.putExtra("ordertime", time);
                    intent.putExtra("orderid", orderid);
                    startActivity(intent);
                } else {
                    MyLog.v("CurrentOrderActivity", "手动输入车牌添加车牌");
                    Intent intent = new Intent(CurrentOrderActivity.this, InputCarNumberActivity.class);
                    intent.putExtra("add", "add");
                    intent.putExtra("orderid", orderid);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//				Toast.makeText(CurrentOrderActivity.this, "取消误操作", 1).show();
            }
        });
        builder.create().show();
    }

    //选择当前订单长按的操作；
    public void OrderHasNumberDialog(final String carnumber, final String total, final String orderid, final int position) {
        final CharSequence[] items = {"修改车牌号", "置为逃单", "现金结算"};
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle(carnumber);
        builder.setItems(items, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (which == 0) {
                    MyLog.i("CurrentOrderActivity", "修改车牌号");
                    Intent intent = new Intent(CurrentOrderActivity.this, InputCarNumberActivity.class);
                    intent.putExtra("add", "change");
                    intent.putExtra("carnumber", carnumber);
                    intent.putExtra("orderid", orderid);
                    startActivity(intent);
                } else if (which == 1) {
                    MyLog.i("CurrentOrderActivity", "置为逃单");
                    MakeLostOrder(orderid, total, position);
                } else {
                    MyLog.i("CurrentOrderActivity", "现金结算");
                    deleteOrder(orderid, total, position);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//				Toast.makeText(CurrentOrderActivity.this, "取消操作", 1).show();
            }
        });
        builder.create().show();
    }

    //	//	http://s.zhenlaidian.com/zld/collectorrequest.do?action=ordercash&token=&orderid=1&total=*；
    //	public void deleteOrder(String orderid,final int position){
    //		if ( ! IsNetWork.IsHaveInternet(this)) {
    //			Toast.makeText(this, "获取订单失败，请检查网络！", 0).show();
    //			return;
    //		}
    //		AQuery aQuery = new AQuery(this);
    //		String path = getResources().getString(R.string.request);
    //		String url = path + "collectorrequest.do?action=ordercash&token=" + BaseActivity.token
    //				+ "&orderid=" + orderid + "&total=" + 0 +"&imei="+imei + "&out=json";
    //		Log.e("删除订单的URl-->>", url);
    //		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...","获取当前订单数据...", true, true);
    //		aQuery.ajax(url, String.class, new AjaxCallback<String>() {
    //
    //			public void callback(String url, String object, AjaxStatus status) {
    //				Log.e("CurrentOrderActivity", "删除订单返回的结果是-->>"+object);
    //				if (object != null) {
    //					dialog.dismiss();
    //					if (object.equals("1")) {
    //						Toast.makeText(CurrentOrderActivity.this, "删除提交成功", 0).show();
    //						adapter.removeOrder(position,CurrentOrderActivity.this);
    //					}else {
    //						Toast.makeText(CurrentOrderActivity.this, "删除提交失败", 0).show();
    //					}
    //				} else {
    //					dialog.dismiss();
    //					return;
    //				}
    //			}
    //		});
    //	}


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.current_order_search, menu);
//		return super.onCreateOptionsMenu(menu);
//	}

    //	http://s.zhenlaidian.com/zld/collectorrequest.do?action=ordercash&token=&orderid=1&total=*；
    //现金结算订单或者删除订单；
    public void deleteOrder(String orderid, String total, final int position) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", 0).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=ordercash&token=" + token
                + "&orderid=" + orderid + "&total=" + total + "&imei=" + imei + "&out=json";
        MyLog.w("删除订单的URl-->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "提交手工结算数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("CurrentOrderDetailsActivity", "删除订单返回的结果是-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    if (object.equals("1")) {
                        //						Toast.makeText(CurrentOrderActivity.this, "删除订单成功！", 0).show();
                        MyLog.i("CurrentOrderActivity", "删除订单成功！");
                        adapter.removeOrder(position, CurrentOrderActivity.this);
                    } else {
                        Toast.makeText(CurrentOrderActivity.this, "删除订单失败！", 0).show();
                    }
                } else {
                    dialog.dismiss();
                    return;
                }
            }
        });
    }

    //置为逃单：  cobp.do?action=escape&orderid=124614&comid=10&total=100.98；
    public void MakeLostOrder(String orderid, String total, final int position) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "置为逃单失败，请检查网络！", 0).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "cobp.do?action=escape&orderid=" + orderid + "&comid=" + comid + "&total=" + total;
        MyLog.w("置为逃单的URl-->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "提交置为逃单数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("CurrentOrderDetailsActivity", "置为逃单返回的结果是-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    if (object.equals("1")) {
                        Toast.makeText(CurrentOrderActivity.this, "置为逃单成功！", 0).show();
                        MyLog.i("CurrentOrderActivity", "置为逃单成功！");
                        adapter.removeOrder(position, CurrentOrderActivity.this);
                    } else {
                        Toast.makeText(CurrentOrderActivity.this, "置为逃单失败！", 0).show();
                    }
                } else {
                    dialog.dismiss();
                    return;
                }
            }
        });
    }

}
