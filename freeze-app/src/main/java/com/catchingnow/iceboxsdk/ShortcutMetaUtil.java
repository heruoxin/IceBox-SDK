package com.catchingnow.iceboxsdk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

public class ShortcutMetaUtil {

    private static final String NAME = "android.app.shortcuts";

    @WorkerThread
    public static List<AppShortcutModel> readShortcut(Context context, String packageName) throws Exception {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = pm.getApplicationInfo(packageName, GET_UNINSTALLED_PACKAGES);
        Resources appRes = pm.getResourcesForApplication(ai);
        return Observable.merge(readApplicationLevelMeta(pm, ai),
                readActivityLevelMeta(pm, ai))
                .flatMap(meta -> {
                    int resId = meta.getInt(NAME);
                    if (resId == 0) return Observable.empty();
                    try {
                        return Observable.just(appRes.getXml(resId));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return Observable.empty();
                    }
                })
                .flatMap(xml -> {
                    List<AppShortcutModel> modelList = new ArrayList<>();
                    int event = xml.getEventType();
                    while (event != XmlPullParser.END_DOCUMENT){
                        switch (event){
                            case XmlPullParser.START_TAG:
                                if ("shortcut".equals(xml.getName())) {
                                    AppShortcutModel model = readXml2Model(appRes, xml, ai);
                                    if (model != null) modelList.add(model);
                                }
                                break;
                            case XmlPullParser.TEXT:
                                break;
                            case XmlPullParser.END_TAG:
                                break;
                            default:
                                break;
                        }
                        event = xml.next();
                    }
                    return Observable.fromIterable(modelList);
                })
                .toList()
                .blockingGet();
    }

    private static Observable<Bundle> readActivityLevelMeta(PackageManager pm, ApplicationInfo appInfo) {
        return Observable.fromCallable(() -> {
            Intent intent = new Intent(Intent.ACTION_MAIN, null).setPackage(appInfo.packageName)
                    .addCategory(Intent.CATEGORY_LAUNCHER);
            return pm.queryIntentActivities(intent, GET_UNINSTALLED_PACKAGES | GET_DISABLED_COMPONENTS | GET_META_DATA);
        })
                .flatMap(Observable::fromIterable)
                .filter(ri -> ri.activityInfo.metaData != null)
                .map(ri -> ri.activityInfo.metaData);
    }

    private static Observable<Bundle> readApplicationLevelMeta(PackageManager pm, ApplicationInfo ai) {
        return Observable.fromCallable(() -> pm.getApplicationInfo(ai.packageName, GET_UNINSTALLED_PACKAGES  | GET_META_DATA))
                .filter(a -> a.metaData != null)
                .map(a -> a.metaData);
    }

    private static AppShortcutModel readXml2Model(Resources res, XmlResourceParser xml, ApplicationInfo ai) {
        try {
            List<Intent> intentList = new ArrayList<>();
            String shortcutId = ai.packageName;
            String shortLabel = null;
            String longLabel = null;
            Drawable icon = null;

            int event = xml.getEventType();   //先获取当前解析器光标在哪
            while (!(event == XmlPullParser.END_TAG && "shortcut".equals(xml.getName()))){
                switch (event){
                    case XmlPullParser.START_TAG:
                        switch (xml.getName()) {
                            case "shortcut":
                                shortcutId = readAndroidString(res, xml, "shortcutId");
                                shortLabel = readAndroidString(res, xml, "shortcutShortLabel");
                                longLabel = readAndroidString(res, xml, "shortcutLongLabel");
                                icon = readAndroidDrawable(res, xml, "icon");
                                break;
                            case "intent":
                                try {
                                    Intent intent = Intent.parseIntent(res, xml, Xml.asAttributeSet(xml));
                                    intentList.add(intent);
                                } catch (XmlPullParserException | IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "categories":
                            default:
                                break;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                event = xml.next();   //将当前解析器光标往下一步移
            }


            AppShortcutModel appShortcutModel = new AppShortcutModel();

            appShortcutModel.drawable = icon;
            appShortcutModel.originalId =shortcutId;
            appShortcutModel.shortLabel = shortLabel;
            appShortcutModel.longLabel = longLabel;
            appShortcutModel.intents = intentList;

            return appShortcutModel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static String readAndroidString(Resources res, XmlResourceParser xml, String attrName) {
        String value = xml.getAttributeValue("http://schemas.android.com/apk/res/android", attrName);
        if (TextUtils.isEmpty(value)) return null;
        try {
            Integer id = Integer.valueOf(value.replace("@", ""));
            return res.getString(id);
        } catch (Exception e) {
            if (!(e instanceof NumberFormatException)) e.printStackTrace();
            return value;
        }
    }

    @Nullable
    private static Drawable readAndroidDrawable(Resources res, XmlResourceParser xml, String attrName) {
        String value = xml.getAttributeValue("http://schemas.android.com/apk/res/android", attrName);
        if (TextUtils.isEmpty(value)) return null;
        try {
            Integer id = Integer.valueOf(value.replace("@", ""));
            return res.getDrawable(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class AppShortcutModel {

        public String originalId;
        public String shortLabel;
        public String longLabel;
        public List<Intent> intents = new ArrayList<>();
        public Drawable drawable;

        @Nullable
        public Intent getIntent() {
            return intents.size() > 0 ? intents.get(0) : null;
        }

    }

}
