package com.catchingnow.iceboxsdk;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.catchingnow.icebox.sdk_client.IceBox;
import com.catchingnow.iceboxsdktestapp.R;

public class FreezeActivity extends AppCompatActivity {

    // 测试包名随便写
    private static final String TEST_PACKAGE = "com.supercell.clashroyale";
    private Handler mBackgroundHandler;
    private TextView mWorkModeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HandlerThread backgroundThread = new HandlerThread("BACKGROUND_THREAD");
        backgroundThread.start();
        mBackgroundHandler = new Handler(backgroundThread.getLooper());

        setContentView(R.layout.activity_main);
        mWorkModeTextView = findViewById(R.id.work_mode);
        findViewById(R.id.freeze).setOnClickListener(v -> freeze());
        findViewById(R.id.defrost).setOnClickListener(v -> defrost());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果没有权限，则先请求权限
        // 走的标准系统流程
        if (ContextCompat.checkSelfPermission(this, IceBox.SDK_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{IceBox.SDK_PERMISSION}, 0x233);
        }

        IceBox.WorkMode workMode = IceBox.queryWorkMode(this);
        mWorkModeTextView.setText(workMode.toString());
    }

    private void freeze() {
        // 冻结，记得异步哦
        mBackgroundHandler.post(() -> {
            IceBox.setAppEnabledSettings(this, false, TEST_PACKAGE);

            Toast.makeText(this, "Frozen", Toast.LENGTH_SHORT).show();
        });
    }

    private void defrost() {
        // 解冻，同样记得异步哦
        mBackgroundHandler.post(() -> {
            IceBox.setAppEnabledSettings(this, true, TEST_PACKAGE);

            Toast.makeText(this, "Defrosted", Toast.LENGTH_SHORT).show();
        });
    }
}
