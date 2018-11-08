# IceBox-SDK

冰箱 SDK，可以在已安装并启用了冰箱的设备上，为第三方 App 提供冻结/解冻的功能。

冻结/解冻需要冰箱版本号 >= 3.6.0；

静默安装需要冰箱版本号 >= 3.9.5。

## 使用方法

### 依赖

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
    implementation 'com.catchingnow.icebox:SDK:1.0.5'
}
```

### 请求权限

在 Android 6.0+ 设备上，冰箱 SDK 的操作类接口（冻结/安装 APK）需要先请求权限再使用，查询类的接口都不需要。

```java
 if (ContextCompat.checkSelfPermission(this, IceBox.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{IceBox.PERMISSION}, 0x233);
 }
```

### 调用接口

#### 冻结解冻相关接口

##### 查询工作模式

不需要权限，冰箱可能工作在 pm disable-user 或 pm hide 两种模式下。

```java
 IceBox.WorkMode workMode = IceBox.queryWorkMode(getContext());
```

##### 查询 App 状态

不需要权限，是正常/ pm disable 冻结/ pm hide 冻结/ both：

```java
IceBox.getAppEnabledSetting(context, packageName);
// 或者
IceBox.getAppEnabledSetting(applicationInfo);
```

##### 冻结/解冻 App

需要权限，支持批量操作

```java
 IceBox.setAppEnabledSettings(getContext(), enable, PACKAGE_NAME...);
```

#### 静默安装卸载相关接口

当冰箱运行在设备管理员（俗称免 Root）模式下时，可对外提供静默安装和卸载 APK 的接口。

##### 查询是否支持

```java
 IceBox.querySupportSilentInstall(context);
```

返回为一个枚举类型

```java

    public enum SilentInstallSupport {
        SUPPORTED,

        NOT_INSTALLED,          //未安装冰箱 IceBox;
        UPDATE_REQUIRED,        //冰箱 IceBox 版本过低;
        SYSTEM_NOT_SUPPORTED,   //当前系统版本过低，不支持静默安装;
        NOT_DEVICE_OWNER,       //冰箱 IceBox 不是设备管理员;
        PERMISSION_REQUIRED,    //当前 App 未取得权限，请发起标准的 Android 权限请求;
    }
```

##### 安装/卸载 APK

在冰箱支持，并且用户授权了的前提下，可以调用实现静默安装卸载，无需用户确认。方法均为同步，直到安装完成或失败后才会返回。

安装成功后通知栏会由系统发送通知提示，同时冰箱的 SDK 日志页面中也会留存记录。

```java
// 安装
IceBox.installPackage(context, uriToApk);

// 卸载
IceBox.uninstallPackage(context, packageName);
```


更详细的代码示例可见 demo app。

<img src="/screenshot/screenshot_freeze.png?raw=true" width="320">
<img src="/screenshot/screenshot_install.png?raw=true" width="320">
