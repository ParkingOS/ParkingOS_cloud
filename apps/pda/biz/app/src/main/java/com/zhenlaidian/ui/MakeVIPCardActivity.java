package com.zhenlaidian.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.photo.InputCarNumberActivity;
import com.zhenlaidian.util.Coverter;
import com.zhenlaidian.util.MyLog;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册速通卡nfc刷卡注册;
 */
public class MakeVIPCardActivity extends BaseActivity {

    private static final String TAG = "MakeVIPCardActivity";

//	private ActionBar actionBar;
//	private ArrayList<DrawerItemInfo> lists;
//	private DrawerLayout drawerLayout = null;
//	private ListView lv_left_drawer;
//	private ActionBarDrawerToggle mDrawerToggle;

    // --------------------读取NFC TAG------------------------------------
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    public static IntentFilter[] FILTERS;

    // 映射Uri前缀和对应的值
    public static final Map<Byte, String> URI_PREFIX_MAP = new HashMap<Byte, String>();

    private String uid;

    static {
        // 设置NDEF Uri规范支持的Uri前缀，在解析payload时，需要根据payload的第1个字节定位相应的uri前缀
        URI_PREFIX_MAP.put((byte) 0x00, "");
        URI_PREFIX_MAP.put((byte) 0x01, "http://www.");
        URI_PREFIX_MAP.put((byte) 0x02, "https://www.");
        URI_PREFIX_MAP.put((byte) 0x03, "http://");
        URI_PREFIX_MAP.put((byte) 0x04, "https://");
        URI_PREFIX_MAP.put((byte) 0x05, "tel:");
        URI_PREFIX_MAP.put((byte) 0x06, "mailto:");
        URI_PREFIX_MAP.put((byte) 0x07, "ftp://anonymous:anonymous@");
        URI_PREFIX_MAP.put((byte) 0x08, "ftp://ftp.");
        URI_PREFIX_MAP.put((byte) 0x09, "ftps://");
        URI_PREFIX_MAP.put((byte) 0x0A, "sftp://");
        URI_PREFIX_MAP.put((byte) 0x0B, "smb://");
        URI_PREFIX_MAP.put((byte) 0x0C, "nfs://");
        URI_PREFIX_MAP.put((byte) 0x0D, "ftp://");
        URI_PREFIX_MAP.put((byte) 0x0E, "dav://");
        URI_PREFIX_MAP.put((byte) 0x0F, "news:");
        URI_PREFIX_MAP.put((byte) 0x10, "telnet://");
        URI_PREFIX_MAP.put((byte) 0x11, "imap:");
        URI_PREFIX_MAP.put((byte) 0x12, "rtsp://");
        URI_PREFIX_MAP.put((byte) 0x13, "urn:");
        URI_PREFIX_MAP.put((byte) 0x14, "pop:");
        URI_PREFIX_MAP.put((byte) 0x15, "sip:");
        URI_PREFIX_MAP.put((byte) 0x16, "sips:");
        URI_PREFIX_MAP.put((byte) 0x17, "tftp:");
        URI_PREFIX_MAP.put((byte) 0x18, "btspp://");
        URI_PREFIX_MAP.put((byte) 0x19, "btl2cap://");
        URI_PREFIX_MAP.put((byte) 0x1A, "btgoep://");
        URI_PREFIX_MAP.put((byte) 0x1B, "tcpobex://");
        URI_PREFIX_MAP.put((byte) 0x1C, "irdaobex://");
        URI_PREFIX_MAP.put((byte) 0x1D, "file://");
        URI_PREFIX_MAP.put((byte) 0x1E, "urn:epc:id:");
        URI_PREFIX_MAP.put((byte) 0x1F, "urn:epc:tag:");
        URI_PREFIX_MAP.put((byte) 0x20, "urn:epc:pat:");
        URI_PREFIX_MAP.put((byte) 0x21, "urn:epc:raw:");
        URI_PREFIX_MAP.put((byte) 0x22, "urn:epc:");
        URI_PREFIX_MAP.put((byte) 0x23, "urn:nfc:");

        try {
            /*
			 * TECHLISTS = new String[][] { { NfcA.class.getName(),
			 * Ndef.class.getName(), MifareUltralight.class.getName() } };
			 */
            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "text/html");
            ndef.addDataScheme("http");
            ndef.addDataAuthority("www.tingchebao.com", "");
            FILTERS = new IntentFilter[]{ndef};
        } catch (Exception e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.makevip_activity_main);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("开通车主速通卡会员");
        actionBar.show();
//		initActionBar();
//		initView();
//		lists = DrawerItemInfo.getInstance();
//		lv_left_drawer.setAdapter(new DrawerAdapter(lists,MakeVIPCardActivity.this));
//		lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this,drawerLayout));
//		lv_left_drawer.setScrollingCacheEnabled(false);

        // --------------------读取NFC TAG------------------------------------
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT);
        onNewIntent(getIntent());

    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                MakeVIPCardActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            MyLog.w(TAG, "----------------------NFC NDEF-TAG DISCOVERED----------------------");
            setIntent(intent);
            uid = Coverter.getUid(intent);

            if (!TextUtils.isEmpty(uid) && hasWritten()) {
                // TODO 处理注册：如何输入车牌号
                Intent vipintent = new Intent(MakeVIPCardActivity.this, InputCarNumberActivity.class);
                vipintent.putExtra("add", "makevip");
                vipintent.putExtra("nfcid", uid);
                startActivity(vipintent);
                MakeVIPCardActivity.this.finish();
            } else {
                Toast.makeText(this, "请使用停车宝专用卡进行注册！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                    MakeVIPCardActivity.FILTERS, null);
    }

    // 判断nfc卡中有没有写数据
    private boolean hasWritten() {
        // 判断是否为ACTION_NDEF_DISCOVERED
        // 从标签读取数据（Parcelable对象）
        Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);

        NdefMessage msgs[] = null;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            // 标签可能存储了多个NdefMessage对象，一般情况下只有一个NdefMessage对象
            for (int i = 0; i < rawMsgs.length; i++) {
                // 转换成NdefMessage对象
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        }
        try {
            if (msgs != null) {
                // 程序中只考虑了1个NdefRecord对象，若是通用软件应该考虑所有的NdefRecord对象
                NdefRecord record = msgs[0].getRecords()[0];
                if (NdefRecord.TNF_WELL_KNOWN == record.getTnf()) {
                    return Uri.parse("http://www.tingchebao.com").equals(parseWellKnown(record));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parse an well known URI record
     */
    private static Uri parseWellKnown(NdefRecord record) {
        // 判断RTD是否为RTD_URI
        if (!Arrays.equals(record.getType(), NdefRecord.RTD_URI))
            return null;
        byte[] payload = record.getPayload();
		/*
		 * payload[0] contains the URI Identifier Code, per the NFC Forum
		 * "URI Record Type Definition" section 3.2.2.
		 * 
		 * payload[1]...payload[payload.length - 1] contains the rest of the
		 * URI.
		 */
        // payload[0]中包括URI标识代码，也就是URI_PREFIX_MAP中的key
        // 根据Uri标识代码获取Uri前缀
        String prefix = URI_PREFIX_MAP.get(payload[0]);
        // 获取Uri前缀占用的字节数
        byte[] prefixBytes = prefix.getBytes(Charset.forName("UTF-8"));
        // 为容纳完整的Uri创建一个byte数组
        byte[] fullUri = new byte[prefixBytes.length + payload.length - 1];
        // 将Uri前缀和其余部分组合，形成一个完整的Uri
        System.arraycopy(prefixBytes, 0, fullUri, 0, prefixBytes.length);
        System.arraycopy(payload, 1, fullUri, prefixBytes.length,
                payload.length - 1);
        // 根据解析出来的Uri创建Uri对象
        MyLog.w(TAG, "parse uri: --->> " + new String(fullUri, Charset.forName("UTF-8")));
        return Uri.parse(new String(fullUri, Charset.forName("UTF-8")));
    }

//	private void initView() {
//		drawerLayout = (DrawerLayout) findViewById(R.id.dl_make_vip_drawer_layout);
//		drawerLayout.setDrawerListener(new MyDrawerListener());
//		lv_left_drawer = (ListView) findViewById(R.id.ll_make_vip_left_drawer);
//		mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
//				R.drawable.ic_drawer_am, R.string.hello_world,
//				R.string.hello_world);
//		mDrawerToggle.syncState();
//	}
//
//	private void initActionBar() {
//		actionBar = getSupportActionBar();
//		actionBar.setTitle("会员开卡");
//		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
//				| ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
//				| ActionBar.DISPLAY_SHOW_TITLE);
//		actionBar.setDisplayHomeAsUpEnabled(true);
//	}
//
//	/** 抽屉的监听 */
//	private class MyDrawerListener implements DrawerLayout.DrawerListener {
//		@Override
//		public void onDrawerOpened(View drawerView) {// 打开抽屉的回调
//			mDrawerToggle.onDrawerOpened(drawerView);
//			actionBar.setTitle("停车宝");
//		}
//
//		@Override
//		public void onDrawerClosed(View drawerView) {// 关闭抽屉的回调
//			mDrawerToggle.onDrawerClosed(drawerView);
//			actionBar.setTitle("会员开卡");
//		}
//
//		@Override
//		public void onDrawerSlide(View drawerView, float slideOffset) {// 抽屉滑动的回调
//			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
//		}
//
//		@Override
//		public void onDrawerStateChanged(int newState) {// 抽屉状态改变的回调
//			mDrawerToggle.onDrawerStateChanged(newState);
//		}
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case android.R.id.home:
//			if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
//				drawerLayout.closeDrawers();
//			} else {
//				drawerLayout.openDrawer(Gravity.LEFT);
//			}
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}