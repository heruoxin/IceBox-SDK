# IceBox-SDK

冰箱的 SDK，可以在已安装并启用了冰箱的设备上，为 App 提供冻结/解冻的功能。

## 使用方法

#### 依赖

添加依赖，在 app 下的 build.gradle 文件中：
```
repositories {
    ......
    ......
    maven { url 'https://dl.bintray.com/heruoxin/icebox' }
}

dependencies {
    ......
    ......
    implementation 'com.catchingnow.icebox:SDK:1.0.0'
}
```

#### 请求权限

对 Android 6.0+，冰箱 SDK 需要先请求权限再使用。
```
 if (ContextCompat.checkSelfPermission(this, IceBox.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{IceBox.PERMISSION}, 0x233);
 }
```

#### 调用接口

查询工作模式，冰箱可能工作在 pm disable-user 或 pm hide 两种模式下。
```
 IceBox.WorkMode workMode = IceBox.queryWorkMode(getContext());
```

冻结/解冻 App
```
 IceBox.setAppEnabledSettings(this, false, Process.myUserHandle().hashCode(), PACKAGE_NAME);
```

