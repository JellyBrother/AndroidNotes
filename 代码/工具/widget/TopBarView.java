package com.jelly_.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jelly_.BaseActivity;
import com.jelly_.Constants.Constants;
import com.jelly_.IBaseApp;
import com.jelly_.R;
import com.jelly_.basemsgcount.PaoPaoRefreshMgr;
import com.jelly_.basemsgcount.event.BaseMsgEvent;
import com.jelly_.entity.commonentity.PilotTypeListEntity;
import com.jelly_.hrservice.DataHelpService;
import com.jelly_.hrservice.GlobalJumpSerice;
import com.jelly_.sdkapi.hwa.HWAUtil;
import com.jelly_.utils.DimenUtils;
import com.jelly_.utils.LogUtils;
import com.jelly_.utils.PublicUtil;
import com.jelly_.utils.SearchUtil;
import com.jelly_.widgets.percentlayout.PercentFrameLayout;
import com.jelly_.mjet.utility.CR;

import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 使用：左边默认是back,如果显示自定义图片，toggleLeftView(resId).如果不显示 toggleLeftView();
 * 中间默认展示文字，需要调用者传入 toggleCenterView(title) ;
 * 右边默认不显示，如果需要显示图片调用toggleRightView(resId),如果需要显示文字调用 toggleRightView(text).
 *
 * @author pWX184170
 */
public class TopBarView extends LinearLayout {

    // title 显示模式，不显示，文字,图片
    protected static final int FLAG_TITLE_NONE = 0;
    protected static final int FLAG_TITLE_TEXT = 1;
    protected static final int FLAG_TITLE_IMG = 2;

    private TextView leftView;

    private TextView centerView;

    private TextView rightView;

    private FrameLayout frameBg1;

    private LinearLayout relativeBg2;

    private View tv_padding;

    private TextView titleArrow;
    /**
     * 三点图标
     */
    private ImageView imgMore;
    /**
     * 搜索
     */
    private ImageView imgSearch;
    /**
     * 在线咨询
     */
    private ImageView imgOnline;
//    /**
//     * 标题栏分割线
//     */
//    private View viewLine;
    /**
     * 右侧图标1 三点的id
     */
    private int id = 200;
    /**
     * 右侧图标2 搜索的id
     */
    private int id2 = 300;

    /**
     * 头部点击事件，包括左右事件
     */
    private OnClickListener mClickListener;
    /**
     * 搜索接口
     */
    private OnClickListener2 mClickListener2;
    /**
     * 在线咨询接口
     */
    private OnClickListener3 mClickListener3;
    /**
     * 主应用Bundle上下文
     */
    private Context mainBundleContext;
    /**
     * 当前应用的上下文
     */
    private Context mContext;
    /**
     * 主应用的版本号判断
     */
    private boolean versionCode15Plus = false;
    private View v_badge;
    private LinearLayout none_network;//无网络提示

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {//不可见
                none_network.setVisibility(View.GONE);
            } else if (msg.what == 1) {//可见
                none_network.setVisibility(View.VISIBLE);
            }
        }
    };

    public TopBarView(Context context) {
        this(context, null);
    }

    public TopBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOrientation(VERTICAL);

        mainBundleContext = PluginWrapper.getAvailableContext(context);
        mContext = context;
        int versionCode = PublicUtil.parseInt(PublicUtil.queryVersionCode(mainBundleContext), 0);
        if (versionCode < 15) {
            initView();
        } else {
            initViewForSdk15Plus();
            versionCode15Plus = true;
        }

        //“当前网络不可用，请检查你的网络设置”提示
        createNoneNetworkPanel();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onEventMainThread(BaseMsgEvent msgEvent) {
        if (msgEvent == null) return;

        if (ConnectivityManager.EXTRA_NETWORK_INFO.equals(msgEvent.type) && msgEvent.data instanceof Intent) {
            Intent intent = (Intent) msgEvent.data;
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager manager = (ConnectivityManager) DataHelpService.application.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeInfo = manager.getActiveNetworkInfo();
                if (activeInfo != null) {//网络连通
                    handler.removeMessages(1);
                    handler.sendEmptyMessage(0);
                } else {//网络断开
                    handler.removeMessages(0);
                    handler.sendEmptyMessage(1);
                }
            }
        }
    }

    public void setClickListener(OnClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public void setClickListener2(OnClickListener2 mClickListener) {
        this.mClickListener2 = mClickListener;
    }

    public void setmClickListener3(OnClickListener3 mClickListener3) {
        this.mClickListener3 = mClickListener3;
    }

    public void onLeftClick(View v) {
        onLeftBtnClick(v);
    }

    public void onRightClick(View v) {
        onRightBtnClick(v);
    }

    public void onRightClick2(View v) {
        onRightBtnClick2(v);
    }

    public void onRightClick3(View v) {
        onRightBtnClick3(v);
    }

    /**
     * 子类如须操作右侧按钮，则重写该方法
     */
    private void onRightBtnClick2(View v) {
        if (null != mClickListener2) {
            mClickListener2.onRightBtnClick2(v);
        } else {
            SearchUtil.jumpToSearch(mainBundleContext, null);
            try {
                if (null != selectPopupWindow && selectPopupWindow.isShowing())
                    selectPopupWindow.dismiss();
            } catch (Exception e) {

            }
        }
    }

    /**
     * 在线咨询点击按钮事件监听
     */
    private void onRightBtnClick3(View v) {
        if (null != mClickListener3) {
            mClickListener3.onRightBtnClick3(v);
            return;
        }
        if (DataHelpService.APPVERSIONCODE.compareTo("82") >= 0) {
            HWAUtil.recordAction("TopBar_Online", "TopBar的在线咨询", "Base框架");
        }
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(GlobalJumpSerice.ROBOT_PACKAGENAME, GlobalJumpSerice.ROBOT_MAINACTIVITY);
        intent.putExtra(Constants.BUNDLENAME, Constants.IROBOTBUNDLESERVICE);
        intent.putExtra("modePackageName", mContext.getPackageName());
        intent.putExtra("modePageTitle", centerView.getText().toString());
        PluginWrapper.startActivity(mainBundleContext, intent);
        try {
            if (null != selectPopupWindow && selectPopupWindow.isShowing())
                selectPopupWindow.dismiss();
        } catch (Exception e) {

        }
    }

    /**
     * 以动态布局，还原XML布局设计
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        frameBg1 = new FrameLayout(mainBundleContext);
        FrameLayout.LayoutParams mainParams = new PercentFrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(frameBg1, mainParams);
        //创建外部的LinearLayout
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        LinearLayout externalLayout = new LinearLayout(mainBundleContext);
        externalLayout.setOrientation(LinearLayout.VERTICAL);
        externalLayout.setBackgroundResource(R.drawable.shape_topbar_bg);
//        externalLayout.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        frameBg1.addView(externalLayout, layoutParams);
        //左边的padding图标
        tv_padding = new View(mainBundleContext);
        ViewGroup.LayoutParams paddingParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimenUtils.dip2px(mainBundleContext, 30));
//        tv_padding.setBackgroundResource(R.drawable.shape_topbar_bg);
//        tv_padding.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
//        tv_padding.setHeight(28);
        tv_padding.setVisibility(View.GONE);
        externalLayout.addView(tv_padding, paddingParams);
        // 里面的大布局
        relativeBg2 = new LinearLayout(mainBundleContext);
        LayoutParams bg2Params = new LayoutParams(LayoutParams.MATCH_PARENT, DimenUtils.dip2px(mainBundleContext, 44));
//        relativeBg2.setBackgroundResource(R.drawable.shape_topbar_bg);
//        relativeBg2.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.white));
        relativeBg2.setOrientation(LinearLayout.HORIZONTAL);
        relativeBg2.setWeightSum(10);
        externalLayout.addView(relativeBg2, bg2Params);
        //左边的TextView
        leftView = new TextView(mainBundleContext);
        LayoutParams leftViewParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
        leftView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        leftView.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        leftView.setPadding(DimenUtils.dip2px(mainBundleContext, 15), DimenUtils.dip2px(mainBundleContext, 21), 0, DimenUtils.dip2px(mainBundleContext, 21));
        leftView.setTextSize(16);
        leftView.setTextColor(mainBundleContext.getResources().getColor(R.color.main_text_colour));
        relativeBg2.addView(leftView, leftViewParams);
        //中间的textView
        centerView = new TextView(mainBundleContext);
        LayoutParams centerViewParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 6);
        centerView.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        centerView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        centerView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        centerView.setFocusable(true);
        centerView.setFocusableInTouchMode(true);
        centerView.setTextSize(16);
        centerView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
        centerView.setSingleLine(true);
        relativeBg2.addView(centerView, centerViewParams);
        //右边的大布局
        RelativeLayout rightLayout = new RelativeLayout(mainBundleContext);
        LayoutParams rightLayoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 4);
        relativeBg2.addView(rightLayout, rightLayoutParams);
        //右边布局的子控件
        imgMore = new ImageView(mainBundleContext);
        RelativeLayout.LayoutParams imgRightParams = new RelativeLayout.LayoutParams(DimenUtils.dip2px(mainBundleContext, 20), DimenUtils.dip2px(mainBundleContext, 20));
        imgRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.CENTER_VERTICAL);
        imgRightParams.rightMargin = DimenUtils.dip2px(mainBundleContext, 15);
        imgMore.setScaleType(ImageView.ScaleType.FIT_XY);
        imgMore.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.ico_clock));
        imgMore.setVisibility(View.GONE);
        imgMore.setContentDescription("imgMore");
        rightLayout.addView(imgMore, imgRightParams);
        //右边布局的文本控件
        rightView = new TextView(mainBundleContext);
        RelativeLayout.LayoutParams rightViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rightViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightViewParams.rightMargin = DimenUtils.dip2px(mainBundleContext, 15);
        rightView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        rightView.setPadding(10, 5, 15, 5);
        rightView.setSingleLine(true);
        rightView.setTextSize(16);
        rightView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
        rightLayout.addView(rightView, rightViewParams);
        //右边布局的箭头控件
        titleArrow = new TextView(mainBundleContext);
        RelativeLayout.LayoutParams titleArrowParams = new RelativeLayout.LayoutParams(DimenUtils.dip2px(mainBundleContext, 18), DimenUtils.dip2px(mainBundleContext, 12));
        titleArrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_BOTTOM);
        titleArrowParams.rightMargin = DimenUtils.dip2px(mainBundleContext, 15);
        titleArrow.setBackground(mainBundleContext.getResources().getDrawable(R.drawable.org_switch_flag));
        titleArrow.setVisibility(View.GONE);
        titleArrow.setContentDescription("titleArrow");
        rightLayout.addView(titleArrow, titleArrowParams);
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftView.setContentDescription(leftView.getId() + "");
                onLeftClick(v);
            }
        });
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick(v);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tv_padding.setVisibility(View.VISIBLE);//设置高度
        }
    }

    /**
     * 以动态布局，还原XML布局设计
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initViewForSdk15Plus() {
        frameBg1 = new FrameLayout(mainBundleContext);
        FrameLayout.LayoutParams mainParams = new PercentFrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //创建外部的LinearLayout
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        LinearLayout externalLayout = new LinearLayout(mainBundleContext);
        externalLayout.setOrientation(LinearLayout.VERTICAL);
        externalLayout.setBackgroundResource(R.drawable.shape_topbar_bg);
//        externalLayout.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.manager_topbar_color));
        frameBg1.addView(externalLayout, layoutParams);
        //左边的padding图标
        tv_padding = new View(mainBundleContext);
        ViewGroup.LayoutParams paddingParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimenUtils.dip2px(mainBundleContext, 25));
//        tv_padding.setBackgroundResource(R.drawable.shape_topbar_bg);
//        tv_padding.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
//        tv_padding.setHeight(28);
        tv_padding.setVisibility(View.GONE);
        externalLayout.addView(tv_padding, paddingParams);
        // 里面的大布局
        relativeBg2 = new LinearLayout(mainBundleContext);
        LayoutParams bg2Params = new LayoutParams(LayoutParams.MATCH_PARENT, DimenUtils.dip2px(mainBundleContext, 44));
//        relativeBg2.setBackgroundResource(R.drawable.shape_topbar_bg);
//        relativeBg2.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.white));
        relativeBg2.setOrientation(LinearLayout.HORIZONTAL);
        relativeBg2.setWeightSum(12);
        externalLayout.addView(relativeBg2, bg2Params);

//        //添加下面的线
//        viewLine = new View(mainBundleContext);
//        LayoutParams lineParams = new LayoutParams(LayoutParams.MATCH_PARENT, DimenUtils.dip2px(mainBundleContext, 1));
//        viewLine.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.main_line_color));
//        externalLayout.addView(viewLine, lineParams);

        //左边的TextView
        leftView = new TextView(mainBundleContext);
        LayoutParams leftViewParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
        leftView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        leftView.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        leftView.setPadding(DimenUtils.dip2px(mainBundleContext, 15), DimenUtils.dip2px(mainBundleContext, 21), 0, DimenUtils.dip2px(mainBundleContext, 21));
        leftView.setTextSize(16);
        leftView.setTextColor(mainBundleContext.getResources().getColor(R.color.main_text_colour));
        relativeBg2.addView(leftView, leftViewParams);
        //中间的textView
        centerView = new MarqueeTextView(mainBundleContext);
        LayoutParams centerViewParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 6);
        centerView.setPadding(DimenUtils.dip2px(mainBundleContext, 5), 0, DimenUtils.dip2px(mainBundleContext, 5), 0);
        centerView.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        centerView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        centerView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        centerView.setFocusable(true);
        centerView.setFocusableInTouchMode(true);
        centerView.setTextSize(16);
        centerView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
        centerView.setSingleLine(true);
        relativeBg2.addView(centerView, centerViewParams);
        //右边的大布局
        RelativeLayout rightLayout = new RelativeLayout(mainBundleContext);
//        rightLayout.setBackgroundColor(Color.RED);
        LayoutParams rightLayoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 4);
        relativeBg2.addView(rightLayout, rightLayoutParams);
        //右边布局的子控件
        imgMore = new ImageView(mainBundleContext);
        RelativeLayout.LayoutParams imgRightParams = new RelativeLayout.LayoutParams(DimenUtils.dip2px(mainBundleContext, 18), DimenUtils.dip2px(mainBundleContext, 18));
        imgRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        imgRightParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        imgRightParams.rightMargin = DimenUtils.dip2px(mainBundleContext, 10);
        imgMore.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.titlebar_icon_more));
        imgMore.setId(id);
        imgMore.setContentDescription("imgMore");
        imgMore.setVisibility(View.VISIBLE);
        rightLayout.addView(imgMore, imgRightParams);
        //右边布局的第二个button控件
        imgSearch = new ImageView(mainBundleContext);
        RelativeLayout.LayoutParams imgRightParams2 = new RelativeLayout.LayoutParams(DimenUtils.dip2px(mainBundleContext, 18), DimenUtils.dip2px(mainBundleContext, 18));
        imgRightParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        imgRightParams2.addRule(RelativeLayout.LEFT_OF, id);
        imgRightParams2.rightMargin = DimenUtils.dip2px(mainBundleContext, 20);
        imgSearch.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.toolbar_search_white));
        imgSearch.setId(id2);
        imgSearch.setContentDescription("imgSearch");
        imgSearch.setVisibility(View.VISIBLE);
        rightLayout.addView(imgSearch, imgRightParams2);

        //右边布局的第三个button控件  2016-10-24 在线咨询优化添加
        //ad by lwx334725 解决字体最大时候，“客服”显示向下
        imgOnline = new ImageView(mainBundleContext);
        RelativeLayout.LayoutParams imgRightParams3 = new RelativeLayout.LayoutParams(DimenUtils.dip2px(mainBundleContext, 22), DimenUtils.dip2px(mainBundleContext, 22));
//        RelativeLayout.LayoutParams imgRightParams3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imgRightParams3.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        imgRightParams3.addRule(RelativeLayout.LEFT_OF, id2);
        imgRightParams3.rightMargin = DimenUtils.dip2px(mainBundleContext, 24);
//        imgRightParams3.bottomMargin = DimenUtils.dip2px(mainBundleContext,3);
//        imgOnline.setText(mainBundleContext.getResources().getString(R.string.online_kefu));
//        imgOnline.setTextSize(16);
//        imgOnline.setTextColor(mainBundleContext.getResources().getColor(R.color.main_colour));

        //改成图标
//        Drawable onlineDrawable = getResources().getDrawable(R.drawable.topbar_online);
//        onlineDrawable.setBounds(0, 0, 24, 24);
//        imgOnline.setCompoundDrawables(onlineDrawable, null, null, null);
        imgOnline.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.topbar_online));
        imgOnline.setVisibility(View.VISIBLE);
        rightLayout.addView(imgOnline, imgRightParams3);

        // 加入客服泡泡数view
        v_badge = new View(mainBundleContext);
        RelativeLayout.LayoutParams v_badgeParams = new RelativeLayout.LayoutParams(DimenUtils.dip2px(mainBundleContext, 70), DimenUtils.dip2px(mainBundleContext, 20));
        v_badgeParams.setMargins(DimenUtils.dip2px(mainBundleContext, -22), DimenUtils.dip2px(mainBundleContext, 2), DimenUtils.dip2px(mainBundleContext, 6), 0);
        v_badgeParams.addRule(RelativeLayout.LEFT_OF, id2);
        v_badgeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//        v_badge.setPadding(DimenUtils.dip2px(mainBundleContext, -22),0,DimenUtils.dip2px(mainBundleContext, -6),0);
        rightLayout.addView(v_badge, v_badgeParams);
        // 注册客服泡泡数
//        PaoPaoRefreshMgr.getInstance().addItem(Constants.KEY_QUESTION_CONFIRMING, v_badge);

        //右边布局的文本控件
        rightView = new TextView(mainBundleContext);
        RelativeLayout.LayoutParams rightViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rightViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightViewParams.rightMargin = DimenUtils.dip2px(mainBundleContext, 15);
        rightView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        rightView.setSingleLine(true);
        rightView.setTextSize(16);
        rightView.setContentDescription("rightView");
        rightView.setTextColor(mainBundleContext.getResources().getColor(R.color.main_colour));
        rightView.setVisibility(View.GONE);
        rightLayout.addView(rightView, rightViewParams);
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftView.setContentDescription(leftView.getId() + "");
                onLeftClick(v);
            }
        });
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick(v);
            }
        });
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick(v);
            }
        });
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgSearch.setContentDescription(imgSearch.getId() + "");
                onRightClick2(v);
            }
        });
        imgOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightClick3(view);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tv_padding.setVisibility(View.VISIBLE);//设置高度
        }
        this.addView(frameBg1, mainParams);

        if (GlobalJumpSerice.ROBOT_PACKAGENAME.equals(mContext.getPackageName())) { // 在线咨询中，抬头咨询按钮要隐藏
            imgOnline.setVisibility(GONE);
            v_badge.setVisibility(GONE);
        } else {
            PaoPaoRefreshMgr.getInstance().addItem(Constants.KEY_ME_SETTING_ONLINE, v_badge);
        }
    }

    //“当前网络不可用，请检查你的网络设置”提示
    private void createNoneNetworkPanel() {
        none_network = new LinearLayout(mainBundleContext);
        LayoutParams lineParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        none_network.setLayoutParams(lineParams);
        int px15 = DimenUtils.dip2px(mainBundleContext, 15);
        int px8 = DimenUtils.dip2px(mainBundleContext, 8);

        none_network.setPadding(px15, px8, 0, px8);
        none_network.setGravity(Gravity.LEFT);
        none_network.setOrientation(LinearLayout.HORIZONTAL);
        none_network.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.none_network_bg));
        none_network.setClickable(true);
        none_network.setVisibility(GONE);

        ImageView redTipImage = new ImageView(mainBundleContext);
        LayoutParams wrapParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        redTipImage.setLayoutParams(wrapParams);
        redTipImage.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.none_network));
        redTipImage.setScaleType(ImageView.ScaleType.FIT_XY);

        TextView textView = new TextView(mainBundleContext);
        textView.setLayoutParams(wrapParams);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(px15, 0, 0, 0);
        textView.setText(mainBundleContext.getResources().getString(R.string.none_network));
        textView.setTextSize(15);
        textView.setTextColor(mainBundleContext.getResources().getColor(R.color.main_text_colour_gray));

        none_network.addView(redTipImage);
        none_network.addView(textView);

        this.addView(none_network);
    }

    /**
     * 设置中间的Title
     *
     * @param title
     */
    public void setTopTitle(String title) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).setActivityTitle(title);
        }
        LogUtils.d("Title:" + BaseActivity.mTitle);

        centerView.setText(title);
        centerView.requestFocus();
        centerView.requestFocusFromTouch();
    }

    public TextView getLeftView() {
        toggleLeftViewDefaul();
        TextView textView = new TextView(mainBundleContext);
        return textView;
    }

    public TextView getNewStyleLeftView() {
        return leftView;
    }

    public TextView getCenterView() {
        return centerView;
    }

    public TextView getRightView() {
        return rightView;
    }

    /**
     * 子类如须操作右侧按钮，则重写该方法
     */
    private void onRightBtnClick(View v) {
        if (hasUserRightToggle) {
            if (null != mClickListener) {
                mClickListener.onRightBtnClick(v);
            }
        } else {
            onRightBtnClick15Plus(v);
        }
    }

    public void onRightBtnClick15Plus(View v) {
        if (null != imgMore) {
            initPopWindow();
            if (popupwindowType == 1) {
                setPopupMsgNotifyVisibility(View.GONE);
            } else if (popupwindowType == 2) {
//                setOnlineVisibility(View.GONE);
            } else if (popupwindowType == 3) {
                setSettingsVisibility(View.GONE);
            } else {
                showPopupWindowAllItem();
            }
            showSelectPop(imgMore);
        }
    }

    /**
     * 显示选择的POPup
     */
    private void showSelectPop(View view) {
        if (selectPopupWindow != null) {
            if (selectPopupWindow.isShowing()) {
                selectPopupWindow.dismiss();
            } else {
//                selectPopupWindow.showAsDropDown(view, 0, DimenUtils.dip2px(mainBundleContext, 13));
                selectPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
            }
        }
    }

    /**
     * 设定快捷菜单中消息通知的可见性
     *
     * @param visibility
     */
    private void setPopupMsgNotifyVisibility(int visibility) {
        if (msgNotify != null) {
            vMsgLine.setVisibility(visibility);
            msgNotify.setVisibility(visibility);
        }
    }

    /**
     * 设定快捷菜单中在线咨询的可见性
     *
     * @param visibility
     */
    private void setOnlineVisibility(int visibility) {
        if (imgOnline != null) {
            imgOnline.setVisibility(visibility);
            v_badge.setVisibility(visibility);
        }
//        if (online != null) {
//            vOnlineLine.setVisibility(visibility);
//            online.setVisibility(visibility);
//        }
    }

    /**
     * 设定快捷菜单中设置的可见性
     *
     * @param visibility
     */
    private void setSettingsVisibility(int visibility) {
        if (setting != null) {
//            vOnlineLine.setVisibility(visibility);
            setting.setVisibility(visibility);
        }
    }

    /**
     * 设定快捷菜单中所有item为可见
     */
    private void showPopupWindowAllItem() {
        if (msgNotify != null) {
            msgNotify.setVisibility(View.VISIBLE);
        }
//        if (online != null) {
//            online.setVisibility(View.VISIBLE);
//        }
        if (setting != null) {
            setting.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设定快捷菜单的item
     *
     * @param popupwindowType 1->隐藏消息， 2->隐藏在线咨询，3->隐藏设置
     */
    public void setRightViewItemType(int popupwindowType) {
        this.popupwindowType = popupwindowType;
    }

    private int popupwindowType = 0;

    private View ivw_msgupdate, ivw_update;
    private PopupWindow selectPopupWindow;
    private RelativeLayout msgNotify;
    //    private RelativeLayout online;
    private RelativeLayout setting;
    private View vMsgLine;
    private LinearLayout topbarTitle;

    private void initPopWindow() {
        if (null == selectPopupWindow) {
            View view = LayoutInflater.from(mainBundleContext).inflate(
                    R.layout.title_select_pop, null);
            topbarTitle = (LinearLayout) view.findViewById(R.id.topbar_title);
            ivw_msgupdate = (View) view.findViewById(R.id.ivw_msgupdate);
            ivw_update = (View) view.findViewById(R.id.ivw_update);
            msgNotify = (RelativeLayout) view.findViewById(R.id.buddy_pop_msg_notify);
            setting = (RelativeLayout) view.findViewById(R.id.buddy_pop_setting);
            vMsgLine = view.findViewById(R.id.vv_line_for_msg);
            LinearLayout top_pop = (LinearLayout) view.findViewById(R.id.top_pop);
            /**
             * 设置pop的弹出位置
             */
            int[] location = new int[2];
            imgMore.getLocationInWindow(location);
            int height = location[1];
            int w = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            imgMore.measure(w, h);
            int heightv = imgMore.getMeasuredHeight();
            ((MarginLayoutParams) top_pop.getLayoutParams()).topMargin = height + heightv;

            selectPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, true);
            selectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            selectPopupWindow.setTouchable(true);
            selectPopupWindow.setOutsideTouchable(true);
            topbarTitle.setOnClickListener(new View.OnClickListener() {//全屏点击消失
                @Override
                public void onClick(View v) {
                    selectPopupWindow.dismiss();
                }
            });
            msgNotify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClassName(GlobalJumpSerice.NEW_MSG_PACKAGENAME, GlobalJumpSerice.EW_MSG_ACTIVITY);
                    intent.putExtra(GlobalJumpSerice.BUNDLENAME, GlobalJumpSerice.NEW_MSG_BUNDLE);
//                    intent.setClassName(GlobalJumpSerice.FRAME_PACKAGENAME, GlobalJumpSerice.MES_LIST_ACTIVITY);
                    intent.putExtra("fragmenttag", "msgfragment");
                    PluginWrapper.startActivity(mainBundleContext, intent);
                    selectPopupWindow.dismiss();
                }
            });

            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IBaseApp app = (IBaseApp) DataHelpService.application;
                    PilotTypeListEntity bundleInfo = app.getBundleInfoOnServer(GlobalJumpSerice.SETTING_BUNDLE);
                    if (bundleInfo != null && !TextUtils.isEmpty(bundleInfo.getVersion()) && "91000".compareTo(bundleInfo.getVersion()) < 0) {//跳插件
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClassName(GlobalJumpSerice.PLUGIN_SETTING_PACKAGENAME, GlobalJumpSerice.PLUGIN_SETTING_ACTIVITY);
                        intent.putExtra(Constants.BUNDLENAME, GlobalJumpSerice.SETTING_BUNDLE);
                        PluginWrapper.startActivity(mainBundleContext, intent);
                    } else {//跳底座
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClassName(GlobalJumpSerice.FRAME_PACKAGENAME, GlobalJumpSerice.FRAME_SETTING);
                        PluginWrapper.startActivity(mainBundleContext, intent);
                    }
                    selectPopupWindow.dismiss();
                }
            });

            //注册消息通知红点与key的对应关系
            PaoPaoRefreshMgr.getInstance().addItem(Constants.KEY_MESSAGE, ivw_msgupdate);
            //注册升级红点与key的对应关系
            PaoPaoRefreshMgr.getInstance().addItem(Constants.KEY_ME_SETTING, ivw_update);
        }
    }


    // 子类操作左侧按钮
    private void onLeftBtnClick(View v) {
        if (null != mClickListener) {
            mClickListener.onLeftBtnClick(v);
        } else {
            ((Activity) mContext).finish();
        }
    }


    /**
     * @param flag , 默认是 FLAG_TITLE_IMG
     * @param res  默认是back箭头 R.drawable.title_back
     * @param text 没有默认值，如果显示文字，需要用户传入
     */
    protected void toggleView(TextView view, int flag, Drawable res, String text) {
        if (flag == FLAG_TITLE_NONE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            if (flag == FLAG_TITLE_TEXT) {
                view.setText(text);
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (flag == FLAG_TITLE_IMG) {
                view.setText(null);
                if (view.getId() == R.id.titleRight) {
                    view.setCompoundDrawablesWithIntrinsicBounds(null, null, res, null);
                } else {
                    view.setCompoundDrawablesWithIntrinsicBounds(res, null, null, null);
                }
            } else {
                LogUtils.w("this flag invaild flag:" + flag);
            }
        }
    }

    /**
     * 使用自定义文字显示
     *
     * @param text
     */
    public void customeStringView(TextView view, String text) {
        toggleView(view, FLAG_TITLE_TEXT, null, text);
    }

    /**
     * 切换皮肤样式
     */
    public void switchThemeStyle(int themeNo) {
        switch (themeNo) {
            case 1:  //标题栏 透明背景， 字体、图标为白色
                imgMore.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.toolbar_more_white));
                imgSearch.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.toolbar_search_white));
//                imgOnline.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
//                viewLine.setVisibility(GONE);

                relativeBg2.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
                leftView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
                togglecustomeLeftView(R.drawable.toolbar_back_white);

                centerView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
                rightView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
                break;

            case 2:  //HRtool 标题栏背景， 字体、图标为白色
                imgMore.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.toolbar_more_white));
                imgSearch.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.toolbar_search_white));
//                imgOnline.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
//                viewLine.setVisibility(GONE);

                relativeBg2.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.hrtool_top_bg));
                leftView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
                togglecustomeLeftView(R.drawable.toolbar_back_white);

                centerView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
                rightView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
                break;
            default:
                break;
        }
    }

    /**
     * 自定义样式
     *
     * @param styles
     */
    public void switchThemeStyle(Map<String, Object> styles) {

        imgMore.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.titlebar_icon_more));
        imgSearch.setImageDrawable(mainBundleContext.getResources().getDrawable(R.drawable.toolbar_search_white));
//        imgOnline.setTextColor(mainBundleContext.getResources().getColor(R.color.main_colour));

        relativeBg2.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        leftView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
        togglecustomeLeftView(R.drawable.titlebar_back);

        centerView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
        rightView.setTextColor(mainBundleContext.getResources().getColor(R.color.white));
    }

    /**
     * 不显示
     *
     * @param view
     */
    public void viewGone(TextView view) {
        toggleView(view, FLAG_TITLE_NONE, null, null);
    }

    /**
     * 左边自定义图片
     */
    public void togglecustomeLeftView(int resId) {
        toggleView(getNewStyleLeftView(), FLAG_TITLE_IMG, mainBundleContext.
                getResources().getDrawable(resId), null);
    }

    /**
     * 左边兼容老插件图片默认
     */
    public void toggleLeftView(int resId) {
//        customeIconView(getNewStyleLeftView(), resId);
        toggleLeftViewDefaul();
    }

    /**
     * 左边默认显示返回图片
     */
    public void toggleLeftViewDefaul() {
        toggleView(getNewStyleLeftView(), FLAG_TITLE_IMG, mainBundleContext.
                getResources().getDrawable(R.drawable.titlebar_back), null);
    }

    /**
     * 右边自定义文字
     */
    public void toggleLeftView(String text) {
        customeStringView(getNewStyleLeftView(), text);
    }

    /**
     * 左边不显示
     */
    public void toggleLeftView() {
        viewGone(getNewStyleLeftView());
    }

    /**
     * 中间默认显示文字标题,必须传入title
     *
     * @param title
     */
    public void toggleCenterView(String title) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).setActivityTitle(title);
        }
        LogUtils.d("Title:" + BaseActivity.mTitle);

        customeStringView(getCenterView(), title);
    }

    /**
     * 中间默认显示文字标题,必须传入文字资源Id
     */
    public void toggleCenterView(int resId) {
        String title = getResources().getString(resId);
        if (null != title && !"".equals(title)) {
            toggleCenterView(title);
        } else {
            LogUtils.e("toggleCenterView Title is null");
        }
    }

    /**
     * 中间不显示
     */
    public void toggleCenterView() {
        viewGone(getCenterView());
    }

    /**
     * 右边自定义图片
     *
     * @param resId
     */
    public void toggleRightView(int resId) {
        try {
            Drawable res = getResources().getDrawable(resId);
            int width = res.getIntrinsicWidth();
            int height = res.getIntrinsicHeight();
            try {
                String resName = getResources().getResourceName(resId);
                int newResId = CR.getLayoutId(mainBundleContext, resName);
                Drawable newRes = mainBundleContext.getResources().getDrawable(newResId);
                int newWidth = newRes.getIntrinsicWidth();
                int newHeight = newRes.getIntrinsicHeight();
                if (Math.abs(width - newWidth) <= 10 && Math.abs(height - newHeight) <= 10) {//资源文件名完全一样,如果只相差5个像素,说明可能是新切图的问题
                    res = newRes;
                }
            } catch (Exception e) {

            }

            toggleView(getRightView(), FLAG_TITLE_IMG, res, null);
            toggleRightSeachGone();
        } catch (Exception e) {

        }
    }

    /**
     * 隐藏三点，隐藏在线咨询（兼容老版本插件）
     */
    public void dismissSetting() {
        if (null != imgMore) {
            if (null != imgSearch && imgSearch.getVisibility() != GONE) { // 搜索图标不隐藏
                RelativeLayout.LayoutParams pams = (RelativeLayout.LayoutParams) imgSearch.getLayoutParams();
                pams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                pams.rightMargin = DimenUtils.dip2px(mainBundleContext, 10);
                imgSearch.setLayoutParams(pams);
            }
            imgMore.setVisibility(View.GONE);
        }

        if (null != imgOnline) {
            imgOnline.setVisibility(GONE);
            v_badge.setVisibility(GONE);
            PaoPaoRefreshMgr.getInstance().hiddenPaoPaoRed(v_badge);
        }
    }

    /**
     * 隐藏三点，搜索,在线咨询可不隐藏（新增，自行控制显示情况）
     */
    public void dismissSettingCompatiable() {
        if (null != imgMore) {
            if (null != imgSearch && imgSearch.getVisibility() != GONE) {
                RelativeLayout.LayoutParams pams = (RelativeLayout.LayoutParams) imgSearch.getLayoutParams();
                pams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                pams.rightMargin = DimenUtils.dip2px(mainBundleContext, 10);
                imgSearch.setLayoutParams(pams);
            }

            if (null != imgOnline && imgOnline.getVisibility() != GONE) {
                if (null != imgSearch && imgSearch.getVisibility() == GONE) {
                    RelativeLayout.LayoutParams pams = (RelativeLayout.LayoutParams) imgOnline.getLayoutParams();
                    pams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    pams.rightMargin = DimenUtils.dip2px(mainBundleContext, 10);
                    imgOnline.setLayoutParams(pams);
                }
            }
            imgMore.setVisibility(View.GONE);
        }
    }

    /**
     * 只隐藏三点
     */
    public void dismissSettingOnly() {
        if (null != imgMore) {
            imgMore.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏搜素，还需添加隐藏在线咨询（兼容）
     */
    public void dismissSeacher() {
        if (null != imgSearch) {
            imgSearch.setVisibility(View.GONE);
        }

        if (null != imgOnline) {
            imgOnline.setVisibility(GONE);
            v_badge.setVisibility(GONE);
            PaoPaoRefreshMgr.getInstance().hiddenPaoPaoRed(v_badge);
        }
    }

    /**
     * 隐藏搜素，在线咨询可隐藏，可不隐藏（新增，自行控制显示情况）
     */
    public void dismissSeacherCompatiable() {
        if (null != imgSearch) {
            if (null != imgOnline && imgOnline.getVisibility() != GONE) { // 不隐藏在线咨询的情况
                RelativeLayout.LayoutParams pams = (RelativeLayout.LayoutParams) imgOnline.getLayoutParams();
                if (imgMore.getVisibility() == GONE) {
                    pams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                } else {
                    pams.addRule(RelativeLayout.LEFT_OF, id);
                }
                pams.rightMargin = DimenUtils.dip2px(mainBundleContext, 10);
                imgOnline.setLayoutParams(pams);
            }
            imgSearch.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏搜素，在线咨询可隐藏，可不隐藏（新增，自行控制显示情况）
     */
    public void dismissSeacherOnly() {
        if (null != imgSearch) {
            imgSearch.setVisibility(View.GONE);
        }
    }


    /**
     * 隐藏在线咨询图标
     */
    public void hideOnlineIcon() {
        if (null != imgOnline) {
            imgOnline.setVisibility(GONE);
            v_badge.setVisibility(GONE);
            PaoPaoRefreshMgr.getInstance().hiddenPaoPaoRed(v_badge);
        }
    }

    //for versioncode > 22
    public void setHasUserRightToggle(boolean hasUserRightToggle) {
        this.hasUserRightToggle = hasUserRightToggle;
    }

    /**
     * 用户是否toggle了右边按钮
     */
    private boolean hasUserRightToggle = false;

    private void toggleRightSeachGone() {
        hasUserRightToggle = true;
        if (versionCode15Plus) {
            if (null != imgSearch) {
                imgSearch.setVisibility(View.GONE);
            }
            if (null != imgMore) {
                imgMore.setVisibility(View.GONE);
            }

            if (null != imgOnline) {
                imgOnline.setVisibility(View.GONE);
                v_badge.setVisibility(GONE);
                PaoPaoRefreshMgr.getInstance().hiddenPaoPaoRed(v_badge);
            }
        }
    }


    /**
     * 自定义右侧第3个图标
     * @param drawableId
     */
//    public void toggleOnlineIcon(int drawableId){
//        if(null != imgOnline){
//            imgOnline.setImageResource(drawableId);
//        }
//    }

    /**
     * 消失右边自定义图片
     */
    public void dismissRightView() {
        getRightView().setVisibility(View.GONE);
    }

    /**
     * 右边自定义文字
     */
    public void toggleRightView(String text) {
        customeStringView(getRightView(), text);
        toggleRightSeachGone();
    }

    /**
     * 右边隐藏
     */
    public void toggleRightView() {
        viewGone(getRightView());
        toggleRightSeachGone();
    }

    /**
     * 隐藏背景，title不要显示背景的时候调用
     */
    public void hideTitleBg() {
        if (frameBg1 != null) {
            frameBg1.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
        }
        relativeBg2.setBackgroundColor(mainBundleContext.getResources().getColor(R.color.transport));
    }

    @TargetApi(16)
    public void setBackground() {
        relativeBg2.setBackground(mainBundleContext.getResources().getDrawable(R.drawable.hr_bg_whdetailbubble));
    }

    /**
     * 显示背景
     */
    @TargetApi(16)
    public void showTitleBg() {
        frameBg1.setBackground(mainBundleContext.getResources().getDrawable(R.drawable.img_loading));
        relativeBg2.setBackground(mainBundleContext.getResources().getDrawable(R.drawable.img_loading));
    }

//    public void setLeftWrapPerant() {
//        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
//        leftView.setLayoutParams(params);
//    }

    /**
     * 设置topView中的三角箭头可见
     */
    public void setTitleArrowVisible(boolean isVisible) {
        if (isVisible) {
            if (null != titleArrow)
                titleArrow.setVisibility(View.VISIBLE);
        }
    }

    public interface OnClickListener {
        public void onRightBtnClick(View v);

        public void onLeftBtnClick(View v);
    }

    /**
     * 搜索回调接口重写
     */
    public interface OnClickListener2 {
        public void onRightBtnClick2(View v); // 搜索
    }

    /**
     * 在线咨询接口重写
     */
    public interface OnClickListener3 {
        public void onRightBtnClick3(View v); // 在线咨询
    }

}
