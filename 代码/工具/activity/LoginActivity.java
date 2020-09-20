import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_activity);
        initView();//初始化布局
        getResolution();//获取屏幕尺寸
    }

    private void initView() {
        tvw_change_language.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //移除监听
                tvw_change_language.removeOnLayoutChangeListener(this);
                initLanguagePopWindow();
            }
        });
    }

    private void getResolution() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        DateUtils.window_width = displayMetrics.widthPixels;
        DateUtils.window_height = displayMetrics.heightPixels;
        String strOpt = "屏幕分辨率为::" + displayMetrics.widthPixels + "*" + displayMetrics.heightPixels;
    }

    private void setListener() {
        cbx_save.setOnCheckedChangeListener(this);
        cbx_autologin.setOnCheckedChangeListener(this);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLogin();
            }
        });
        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                et_userpsw.setText("");
            }
        });
    }

    //初始化语言弹框
    private void initLanguagePopWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.show_version_activity, null);
        languagePopupWindow = new PopupWindow(view, tvw_change_language.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        languagePopupWindow.setFocusable(true);
        languagePopupWindow.setOutsideTouchable(true);
        // 实例化一个ColorDrawable颜色为半透明,不然点击外部不会消失
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        languagePopupWindow.setBackgroundDrawable(dw);
        tvw_show_language = (TextView) view.findViewById(R.id.tvw_show_language);
        setLanguageText(tvw_show_language, true);

        tvw_show_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != languagePopupWindow) {
                    //切换语言
                    IPreferences.setIsChangeLanguage(true);
                    IPreferences.setIsClickLanguage(true);
                    String tvw_text = tvw_show_language.getText().toString();
                    String string = getResources().getString(R.string.china);
                    if (string.equalsIgnoreCase(tvw_text)) {
                        IPreferences.setLanguage(IPreferences.KEY_CHINESE);
                    } else {
                        IPreferences.setLanguage(IPreferences.KEY_ENGLISH);
                    }
                    languagePopupWindow.dismiss();
                    recreate();
                }
            }
        });
        languagePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tvw_change_language.setBackgroundResource(R.drawable.selector_blue_white_empty_bg);
            }
        });
    }

    /**
     * 语音切换后，即使用户设置了系统字体大小，默认还是app的字体大小
     *
     * @return
     */
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration configuration = resources.getConfiguration();
        switch (IPreferences.getLanguage()) {
            case IPreferences.KEY_CHINESE:
                configuration.locale = Locale.SIMPLIFIED_CHINESE;
//                configuration.locale = new Locale("zh");
                break;
            case IPreferences.KEY_ENGLISH:
            default:
                configuration.locale = Locale.ENGLISH;
//                configuration.locale = new Locale("en");
                break;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return resources;
    }
}
