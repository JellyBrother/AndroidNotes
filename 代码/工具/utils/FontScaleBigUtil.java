package com.jelly.baselibrary.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.jelly.baselibrary.common.BaseCommon;

public class FontScaleBigUtil {
    private static final String TAG = "FontScaleBig";
    private static final String FONT_SCALE_BIG = "font_scale_big";
    private float mFontScale;
    private Context mContext;
    private static FontScaleBigUtil sInstance = null;

    private FontScaleBigUtil(Context context) {
        mContext = context;
        try {
            mFontScale = Settings.System.getFloat(context.getContentResolver(), FONT_SCALE_BIG);
        } catch (Settings.SettingNotFoundException e) {
            LogUtil.getInstance().e(TAG, "android.provider.Settings.SettingNotFoundException");
        }
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(FONT_SCALE_BIG), false, mFontScaleBigObserver);
    }

    public static FontScaleBigUtil getInstance() {
        if (null == sInstance) {
            sInstance = new FontScaleBigUtil(BaseCommon.Base.application);
        }
        return sInstance;
    }

    private ContentObserver mFontScaleBigObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            try {
                mFontScale = Settings.System.getFloat(mContext.getContentResolver(), FONT_SCALE_BIG);
            } catch (Settings.SettingNotFoundException e) {
                LogUtil.getInstance().e(TAG, "android.provider.Settings.SettingNotFoundException");
            }
        }
    };

    public float getFontScale() {
        return mFontScale;
    }
}
