package com.yufuchang.bridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yufuchang.bridge.utils.Res;
import com.yufuchang.bridge.view.AndroidWebBridgeActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Res.init(this);

        findViewById(R.id.btn_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AndroidWebBridgeActivity.class);
                intent.putExtra("uri", "file:////android_asset/test.html");
//                intent.putExtra(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
                startActivity(intent);
            }
        });
    }
}
