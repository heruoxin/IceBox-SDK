# IceBox-SDK

冰箱的 SDK，可以在已安装并启用了冰箱的设备上，为 App 提供冻结/解冻的功能。

需要最新版本冰箱（版本号大于等于 3.6.0）支持。

## 使用方法

#### 依赖

添加依赖，在 app 下的 build.gradle 文件中：
```groovy
repositories {
    ......
    ......
    maven { url 'https://dl.bintray.com/heruoxin/icebox' }
}

dependencies {
    ......
    ......
    implementation 'com.catchingnow.icebox:SDK:1.0.1'
}
```

#### 请求权限

在 Android 6.0+ 设备上，冰箱 SDK 需要先请求权限再使用。
```java
 if (ContextCompat.checkSelfPermission(this, IceBox.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{IceBox.PERMISSION}, 0x233);
 }
```

#### 调用接口

查询工作模式，冰箱可能工作在 pm disable-user 或 pm hide 两种模式下。
```java
 IceBox.WorkMode workMode = IceBox.queryWorkMode(getContext());
```

查询 App 状态，是正常/ pm disable 冻结/ pm hide 冻结/ both：
```java
IceBox.getAppEnabledSetting(context, packageName);
// 或者
IceBox.getAppEnabledSetting(applicationInfo);
```


冻结/解冻 App，支持多个
```java
 IceBox.setAppEnabledSettings(getContext(), false, PACKAGE_NAME...);
```

