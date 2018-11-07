package com.catchingnow.installapp;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.catchingnow.icebox.sdk_client.IceBox;
import com.catchingnow.installapp.databinding.ActivityMainBinding;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private Disposable mSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setPath("/sdcard/1.apk");
        mBinding.btnInstall.setOnClickListener(v -> installApk(mBinding.getPath()));

    }

    private void installApk(String path) {
        String authority = getPackageName() + ".FILE_PROVIDER";
        Uri uri = FileProvider.getUriForFile(this, authority, new File(path));
        mSubscribe = Single.fromCallable(() -> IceBox.silentInstallPackage(this, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    Toast.makeText(this, success ? "安装成功" : "安装失败", Toast.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscribe != null && !mSubscribe.isDisposed()) mSubscribe.dispose();
    }
}
