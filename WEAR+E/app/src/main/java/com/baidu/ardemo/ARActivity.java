package com.baidu.ardemo;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.ar.ARFragment;
import com.baidu.ar.constants.ARConfigKey;
import com.baidu.ar.external.ARCallbackClient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ARActivity extends FragmentActivity {

    private ARFragment mARFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        if (findViewById(R.id.bdar_id_fragment_container) != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // 准备调起AR的必要参数
            // AR_KEY:AR内容平台里申请的每个case的key
            // AR_TYPE:AR类型，目前0代表2D跟踪类型，5代表SLAM类型，后续会开放更多类型
            String arkey = getIntent().getStringExtra(ARConfigKey.AR_KEY);
            int arType = getIntent().getIntExtra(ARConfigKey.AR_TYPE, 0);
            Bundle data = new Bundle();
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put(ARConfigKey.AR_KEY, arkey);
                jsonObj.put(ARConfigKey.AR_TYPE, arType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data.putString(ARConfigKey.AR_VALUE, jsonObj.toString());
            mARFragment = new ARFragment();
            mARFragment.setArguments(data);
            mARFragment.setARCallbackClient(new ARCallbackClient() {
                // 分享接口
                @Override
                public void share(String title, String content, String shareUrl, String resUrl, int type) {
                    // type = 1 视频，type = 2 图片
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, content);
                    shareIntent.putExtra(Intent.EXTRA_TITLE, title);
                    shareIntent.setType("text/plain");
                    // 设置分享列表的标题，并且每次都显示分享列表
                    startActivity(Intent.createChooser(shareIntent, "分享到"));
                }

                // 透传url接口：当AR Case中需要传出url时通过该接口传出url
                @Override
                public void openUrl(String url) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri contentUrl = Uri.parse(url);
                    intent.setData(contentUrl);
                    startActivity(intent);
                }

                // AR黑名单回调接口：当手机不支持AR时，通过该接口传入退化H5页面的url
                @Override
                public void nonsupport(String url) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri contentUrl = Uri.parse(url);
                    intent.setData(contentUrl);
                    startActivity(intent);
                    ARActivity.this.finish();
                }
            });
            // 将trackArFragment设置到布局上
            fragmentTransaction.replace(R.id.bdar_id_fragment_container, mARFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        boolean backFlag = false;
        if (mARFragment != null) {
            backFlag = mARFragment.onFragmentBackPressed();
        }
        if (!backFlag) {
            super.onBackPressed();
        }
    }

}