package com.catchingnow.installapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
        mBinding.setPackagename("com.some.app");
        mBinding.btnInstall.setOnClickListener(v -> installApp(mBinding.getPath()));
        mBinding.btnUninstall.setOnClickListener(v -> uninstallApp(mBinding.getPackagename()));
        mBinding.btnRequest.setOnClickListener(v -> requestPermission());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updatePermissionState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposeSafety();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{IceBox.SDK_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0x233);
    }

    private void updatePermissionState() {
        IceBox.SilentInstallSupport state = IceBox.querySupportSilentInstall(this);
        mBinding.setIceboxState(state.toString());

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mBinding.setSdcardState(permission == PackageManager.PERMISSION_GRANTED ? "PERMISSION_GRANTED" : "PERMISSION_DENIED");
    }

    private void installApp(String path) {
        String authority = getPackageName() + ".FILE_PROVIDER";
        Uri uri = FileProvider.getUriForFile(this, authority, new File(path));
        disposeSafety();
        mSubscribe = Single.fromCallable(() -> IceBox.installPackage(this, uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    Toast.makeText(this, success ? "安装成功" : "安装失败", Toast.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);
    }

    private void uninstallApp(String packageName) {
        disposeSafety();
        mSubscribe = Single.fromCallable(() -> IceBox.uninstallPackage(this, packageName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    Toast.makeText(this, packageName + (success ? " 卸载成功" : " 卸载失败"), Toast.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);
    }

    private void disposeSafety() {
        if (mSubscribe != null && !mSubscribe.isDisposed()) mSubscribe.dispose();
        mSubscribe = null;
    }

}
