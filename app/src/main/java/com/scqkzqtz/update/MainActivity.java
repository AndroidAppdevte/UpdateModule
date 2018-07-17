package com.scqkzqtz.update;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.scqkzqtz.update.update.UpdateUtils;
import com.scqkzqtz.update.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceUtils.init(this);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateUtils(MainActivity.this).setVersion("4.0.0")
                        .setContent("1.这是我更新的哦\n2.这升格为是我更新的哦")
                        .setDownloadUrl("http://app-global.pgyer.com/a8bd87d337d9f9380d428d4f5ab6c652.apk?attname=yds_alibabaRelease_3.4.1.2018_7_17_Beta.apk&sign=bc51aef338b88b61e778ff9c00adc7e8&t=5b4d9fc9")
                        .setCompulsory(false)
                        .setFlag(true)
                        .start();
            }
        });
    }
}
