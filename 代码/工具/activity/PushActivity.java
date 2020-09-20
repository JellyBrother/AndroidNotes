import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PushActivity extends BaseDetailActivity<PushViewModel> {
    private static final String TAG = "PushActivity";
    /** 标题栏 */
    private TopBar topBar;
    /** webView */
    private WebView webView;
    /** 消息处理 */
    private H5Handler mHandler;
    /** 上下文 */
    private Activity mActivity;
    /** 选择分类控件 */
    private SelectCategoryView selectCategoryView;
    /** 底部表情/选择图片/拍照 */
    private SelectView selectView;
    /** 标题 */
    private EditText etTitle;
    /** webView桥接 */
    private JsInterface JsInterface;
    /** json解析 */
    private Gson gson;
    /** 写实体类 */
    private PushBean PushBean;
    /** 是否正在转圈请求 */
    private boolean isLoading = false;
    /** 页面根布局 **/
    private RelativeLayout rltContent;
    /** 监控软键盘显示隐藏 **/
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    /** 是否隐藏键盘 **/
    private boolean isSoftInputShow = false;
    /** 是否请求布局 **/
    private boolean isRequestLayout = false;
    /** 包容器高度 **/
    private int contentHeight;
    /** 从哪个页面过来（默认） **/
    private String from = Constant.Intent.VALUE_FROM_;
    /** selectView的类型 **/
    private String selectViewType = Constant..SELECTVIEW_SELECTPHOTO;

    /**
     * 消息处理
     */
    private static class H5Handler extends BaseHandler {
        public H5Handler(Activity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg, int what) {
            final PushActivity activity = (PushActivity) wActivity.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            switch (msg.what) {
                // 界面渲染完成回调
                case Constant.Detail.WHAT_RENDER_END:
                    activity.initCache();
                    break;
                // 加载url
                case Constant.Detail.WHAT_LOAD_URL:
                    if (msg.obj != null && activity.webView != null) {
                        activity.webView.loadUrl(msg.obj.toString());
                    }
                    break;
                // 初始化缓存
                case Constant..WHAT_INIT_CACHE:
                    activity.initPage();
                    break;
                // 是否可以
                case Constant..WHAT_SET_PUSHSTATE:
                    if (msg.obj != null) {
                        activity.PushBean.showPublishBtn = msg.obj.toString();
                        activity.setPushState(activity.PushBean.isPushAndCache());
                    }
                    break;
                // 安卓调用js方法getImgCount获取图片总数，js再调用桥接设置图片总数
                case Constant..WHAT_SET_IMG_COUNT:
                    if (msg.obj != null) {
                        activity.setImgCount(msg.obj.toString());
                    }
                    break;
                // 图片上传完成
                case Constant..WHAT_UPLOAD_IMAGE_FINISH:
                    ArrayList<ImageUploadBean> uploadFailImages = (ArrayList<ImageUploadBean>) msg.obj;
                    if (uploadFailImages == null || uploadFailImages.isEmpty()) {//全部上传成功，获取编辑器内容
                        activity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                /** 延时200毫秒让H5替换图片地址成功后，再获取H5内容进行提交 **/
                                String js = "javascript: " + Constant.JsInterface.JSMETHOD_GETCONTENTS + "(" + Constant.JsInterface.JSMETHOD_GETCONTENT_PARAM2 + ")";
                                activity.webView.loadUrl(js);
                            }
                        }, 200);
                    } else {
                        ToastUtils.makeTextShowErr(R.string.*__push_fail);
                    }
                    break;
                // 保存内容
                case Constant..WHAT_SAVE_CONTENT:
                    if (msg.obj != null) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(msg.obj.toString());
                        } catch (JSONException e) {
                            LogUtils.e(TAG, e.getMessage(), e);
                        }
                        String code = jsonObject.optString("type");
                        String Content = jsonObject.optString("content");
                        Content = StringUtils.replaceText(Content);
                        activity.PushBean.Content = Content;
                        if (Constant.JsInterface.JSMETHOD_GETCONTENT_PARAM1.equals(code)) {
                            //传 1 安卓页面点击取消按钮，获取页面js富文本编辑器的内容。
                            activity.showCacheDialog();
                        }
                        if (Constant.JsInterface.JSMETHOD_GETCONTENT_PARAM2.equals(code)) {
                            /**
                             * 传 2 安卓页面点击按钮，获取页面js富文本编辑器的内容。 到这里是已经点击了按钮了，可以了的
                             * 先调桥接，获取所有file开头本地图片文件
                             * 然后安卓对所有图片进行上传，上传失败则提示失败，用户需要再次点击按钮，上传成功就调桥接进行替换
                             * 所有调上传成功后，获取编辑器文本内容，调接口，进行操作。
                             */
                            activity.mViewModel.requestDataPost(activity, activity.PushBean);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public PushViewModel initViewModel() {
        return new PushViewModel();
    }

    @Override
    public void initViews() {
        mActivity = this;
        mHandler = new H5Handler(this);
        gson = new GsonBuilder().create();
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.Intent.KEY_FROM)) {
            from = intent.getStringExtra(Constant.Intent.KEY_FROM);
        }
        Helper.from = from;
        setContentView(R.layout.*_activity__push);
        webView = findViewById(R.id.webview);
        //初始化WebView
        initWebView();
        rltContent = findViewById(R.id.rlt_content);
        topBar = findViewById(R.id.topbar);
        selectCategoryView = findViewById(R.id.selectCategoryView);
        selectCategoryView.initCategoryFrom(from);
        selectView = findViewById(R.id.selectView);
        etTitle = findViewById(R.id.et_title);
        etTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, AppEnvironment.getEnvironment().getDetailTextSize());
        //初始化头部
        initTopBar();
    }

    /**
     * 初始化WebView
     */
    private void initWebView() {
        webView.getSettings().setGeolocationEnabled(false);
        webView.getSettings().setAllowContentAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        DetailHelper.configWebSettings(webView.getSettings());
        JsInterface = new *JsInterface(this, mHandler);
        webView.addJavascriptInterface(JsInterface, Constant.JsInterface.JSINTERFACENAME_JSCALLJAVA);
        webView.setWebViewClient(new H5WebClient(this, mHandler, false));
        webView.setWebChromeClient(new H5WebChrome());
        String url = Constant.Url.LOCAL_H5__WRITE + "?lang=" + LanguageUtil.getLang() + "&env=" + AppUtils.getEnv();
        if (TextUtils.equals(from, Constant.Intent.VALUE_FROM_ASK)) {
            url = Constant.Url.LOCAL_H5_ASK_WRITE + "?lang=" + LanguageUtil.getLang() + "&env=" + AppUtils.getEnv();
        }
        webView.loadUrl(url);
        DetailHelper.initWebViewLang(this);
        PushBean = new PushBean();
    }

    /**
     * 初始化头部标题
     */
    private void initTopBar() {
        topBar.getImgLeft().setVisibility(View.GONE);
        topBar.getTvLeft().setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams leftLayoutParams = (RelativeLayout.LayoutParams) topBar.getTvLeft().getLayoutParams();
        leftLayoutParams.leftMargin = DensityUtils.dip2px(1);
        topBar.getTvLeft().setLayoutParams(leftLayoutParams);
        topBar.getTvLeft().setTextColor(getResources().getColor(R.color.*_white));
        RelativeLayout.LayoutParams rightLayoutParams = (RelativeLayout.LayoutParams) topBar.getTvRight().getLayoutParams();
        rightLayoutParams.rightMargin = DensityUtils.dip2px(6);
        topBar.getTvRight().setLayoutParams(rightLayoutParams);
        if (LanguageUtil.isEnglish()) {
            topBar.getTvRight().setText(Constant..*__PUSH_EN);
            topBar.getTvLeft().setText(Constant..*_CANCEL_EN);
            topBar.getMiddleTitle().setText(Constant..*__WRITE_EN);
            if (TextUtils.equals(from, Constant.Intent.VALUE_FROM_ASK)) {
                topBar.getMiddleTitle().setText(Constant..*_ASK_EN);
                topBar.getTvRight().setText(R.string.*__push);
            }
        } else {
            topBar.getTvRight().setText(Constant..*__PUSH_CN);
            topBar.getTvLeft().setText(Constant..*_CANCEL_CN);
            topBar.getMiddleTitle().setText(Constant..*__WRITE_CN);
            if (TextUtils.equals(from, Constant.Intent.VALUE_FROM_ASK)) {
                topBar.getMiddleTitle().setText(Constant..*_ASK_CN);
            }
        }
        setPushState(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
        /** 初始化键盘信息 **/
        contentHeight = 0;
        isRequestLayout = true;
        isSoftInputShow = false;
        LogUtils.i(TAG, "**onResume**");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /** 初始化键盘信息 **/
        contentHeight = 0;
        isRequestLayout = true;
        isSoftInputShow = false;
        LogUtils.i(TAG, "**onConfigurationChanged**");
    }

    private void initListener() {
        /** 监控软键盘 **/
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) rltContent.getLayoutParams();
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //动态获取屏幕高度，应对横竖屏切换
//                DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
//                int rootViewHeight = displayMetrics.heightPixels;
                // 这个方法获取到到屏幕高才是真正到这个屏幕高度，其余到方法获取到的有问题，当是水滴屏幕的时候，拿到到高度是去掉水滴后到高度
                int rootViewHeight = getWindow().getDecorView().getHeight();
                //判断窗口可见区域大小
                Rect rect = new Rect();
                rltContent.getWindowVisibleDisplayFrame(rect);
                //如果屏幕高度和window可见区域高度差值大于整个屏幕高度的1/4，则表示软键盘正在显示，否则软键盘为隐藏
                int rectBottom = rect.bottom - rect.top;
                int keyboardHeight = rootViewHeight - rectBottom;
                if (keyboardHeight <= 0) {
                    // 如果屏幕高度小于等于可见区域高度，说明不是发页面，而是拍照页面返回，发页面顶部是有距离的，进而防止contentHeight为0
                    return;
                }
                if (contentHeight < rectBottom) {
                    // 保证contentHeight是最大的
                    contentHeight = rectBottom;
                }
                boolean isKeyboardShow = keyboardHeight > (rootViewHeight / 4);//除以4是为了兼容平板
                //如果之前软键盘状态为显示，现在为关闭，或者之前为关闭，现在为显示，则表示软键盘状态发生了改变
                if ((isSoftInputShow && !isKeyboardShow) || (!isSoftInputShow && isKeyboardShow)) {
                    isSoftInputShow = isKeyboardShow;
                }
                LogUtils.i(TAG, "onGlobalLayout2:rootViewHeight:*" + rootViewHeight + "*rectBottom:*" +
                        rectBottom + "*contentHeight:*" + contentHeight + "*isSoftInputShow:*" + isSoftInputShow +
                        "*isRequestLayout:*" + isRequestLayout);
                // 修复软件盘挡住编辑输入框的问题
                if (isRequestLayout) {//避免再次请求布局的时候，死循环
                    if (isSoftInputShow) {
                        layoutParams.height = rectBottom;
                        selectView.hideEmoji();
                        //弹出键盘，需要隐藏表情，需要h5滑动到光标位置，需要设置键盘高度给h5
                        //                    int showHeight = keyboardHeight + DensityUtil.dip2px(49);
                        //                    webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_KEYBOARDHEIGHT + "(" + showHeight + ")");
                    } else {
//                        layoutParams.height = contentHeight;
                        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
                    }
                    isRequestLayout = false;
                    rltContent.requestLayout();
                } else {
                    isRequestLayout = true;
                }
            }
        };
        rltContent.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        /** 头部标题 **/
        topBar.getTvLeft().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCache();
            }
        });
        topBar.getTvRight().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PushBean == null) {
                    return;
                }
                if (!PushBean.isPushAndCache()) {
                    return;
                }
                if (isLoading) {
                    return;
                }
                //判断是否有网络
                if (!NetworkUtils.isNetworkConnected()) {
                    ToastUtils.makeTextShow(R.string.*_topbar_no_network);
                    return;
                }
                Helper.hidenSoftInput(mActivity, rltContent);
                rltContent.clearFocus();
                LoadingUtils.show(mActivity);
                webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_GETLOCALEIMAGE + "()");
            }
        });
        /** 输入标题 **/
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String Title = s.toString();
                PushBean.Title = Title;
                setPushState(PushBean.isPushAndCache());
            }
        });
        etTitle.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        /** 如果是￼或者换行则返回空字符 **/
                        if (source == null || source.equals("")) {
                            return "";
                        }
                        String sourceString = source.toString();
                        if (sourceString.contains(Constant.OtherConstant.ENTER)) {
                            return sourceString.replace(Constant.OtherConstant.ENTER, "");
                        }
                        if (sourceString.contains(Constant.OtherConstant.OBJ)) {
                            return sourceString.replace(Constant.OtherConstant.OBJ, "");
                        }
                        return null;
                    }
                }
                , new InputFilter.LengthFilter(60) // 限制长度为60
                //                , new InputFilter.AllCaps()  // 小写转换成大小
        });
        /** 标题编辑器不让复制粘贴 **/
        etTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        etTitle.setLongClickable(false);
        etTitle.setTextIsSelectable(false);
        ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        };
        etTitle.setCustomSelectionActionModeCallback(callback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            etTitle.setCustomInsertionActionModeCallback(callback);
        }
        /** 富文本编辑器 **/
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return Config.H5_DETAIL_COPY;
            }
        });
        webView.setLongClickable(false);
        webView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectView.hideEmoji();
                    selectView.setVisibility(View.VISIBLE);
                } else {
                    selectView.setVisibility(View.GONE);
                }
            }
        });
        // 解决H5编辑器出现 OBJ 的问题
        InputConnection.InputConnectionListener InputConnectionListener = new InputConnection.InputConnectionListener() {
            @Override
            public boolean setText(CharSequence text, int newCursorPosition) {
                //                if (!TextUtils.isEmpty(text)) {
                //                    JSONObject jsonObject = new JSONObject();
                //                    try {
                //                        jsonObject.put("type", "emoji");
                //                        jsonObject.put("content", text);
                //                        String js = "javascript:" + Constant.JsInterface.JSMETHOD_NATIVEEXECCOMMAND + "(" + jsonObject.toString() + ")";
                //                        mHandler.obtainMessage(Constant.Detail.WHAT_LOAD_URL, js).sendToTarget();
                //                    } catch (JSONException e) {
                //                        LogUtils.e(TAG, e.getMessage(), e);
                //                    }
                //                    return true;
                //                }
                return false;
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getFlags() != 0) {
                    /** 执行输入法自带的删除 **/
                    return false;
                }
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        /** js提供给安卓调用的js方法：主动删除编辑器内容 */
                        String js = "javascript:" + Constant.JsInterface.JSMETHOD_ANDROIDDELETEKEY + "()";
                        mHandler.obtainMessage(Constant.Detail.WHAT_LOAD_URL, js).sendToTarget();
                        return true;
                    }
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        return true;
                    }
                }
                return false;
            }
        };
        webView.setKInputConnectionListener(kInputConnectionListener);
        /** 底部选中框 **/
        selectView.setOnEmojiListener(new SelectView.OnEmojiListener() {
            @Override
            public void onEmojiSelect(String emoji) {
                if (TextUtils.isEmpty(emoji)) {
                    return;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "emoji");
                    jsonObject.put("content", emoji);
                    webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_NATIVEEXECCOMMAND + "(" + jsonObject.toString() + ")");
                } catch (JSONException e) {
                    LogUtils.e(TAG, e.getMessage(), e);
                }
            }

            @Override
            public void onClickEmojiHide() {
                Helper.showSoftInput(mActivity);
            }

            @Override
            public void onClickEmojiShow() {
                Helper.hidenSoftInput(mActivity, rltContent);
                int showHeight = DensityUtils.dip2px(169);
                webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_KEYBOARDHEIGHT + "(" + showHeight + ")");
            }
        });
        if (TextUtils.equals(from, Constant.Intent.VALUE_FROM_ASK)) {
            /** 问答的时候，调用H5桥接获取当前图片数超过9张就不能加图片了 提示：最多可选9张图片 **/
            selectView.setOnSetPicListener(new SelectView.OnGetImgCountListener() {
                @Override
                public void getImgCount(String type) {
                    selectViewType = type;
                    webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_GETIMGCOUNT + "()");
                }
            });
        }
    }

    private void setImgCount(String imgCount) {
        int count = Integer.parseInt(imgCount);
        /** 问答的时候，调用H5桥接获取当前图片数超过9张就不能加图片了 提示：最多可选9张图片 **/
        if (count > 8) {
            ToastUtils.makeTextShowErr(R.string.);
            return;
        }
        if (TextUtils.equals(selectViewType, Constant..SELECTVIEW_OPENCAMERA)) {
            PhotoSelectHelper.openCamera(mActivity);
            return;
        }
        int picMax = 9 - Integer.parseInt(imgCount);
        PhotoSelectHelper.selectPhoto(mActivity, picMax, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ImagePickerConstants.REQUEST_CODE:
                if (data != null && resultCode == ImagePickerConstants.RESULT_CODE_OK) {
                    ArrayList<MediaItem> mediaItems = data.getParcelableArrayListExtra(ImagePickerConstants.EXTRA_SELECTED_RESULT);
                    ArrayList<String> images = ReplyHelper.getSelectedImgs(mediaItems);
                    if (null != images && images.size() > 0) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", "image");
                            JSONArray jsonArray = new JSONArray();
                            for (String image : images) {
//                                String encode = StringUtils.encode(image);
                                jsonArray.put(image);
                            }
                            jsonObject.put("content", jsonArray);
                            webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_NATIVEEXECCOMMAND + "(" + jsonObject.toString() + ")");
                        } catch (Exception e) {
                            LogUtils.e(TAG, e.getMessage(), e);
                        }
                    }
                }
                break;
            case CameraConstants.REQUEST_CODE_CAMERA:
                if (data != null && resultCode == CameraConstants.RESULT_CODE_TYPE_IMAGE) {
                    String path = data.getStringExtra(CameraConstants.EXTRA_PATH);
                    if (!TextUtils.isEmpty(path)) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", "image");
                            JSONArray jsonArray = new JSONArray();
//                            String encode = StringUtils.encode(path);
                            jsonArray.put(path);
                            jsonObject.put("content", jsonArray);
                            webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_NATIVEEXECCOMMAND + "(" + jsonObject.toString() + ")");
                        } catch (JSONException e) {
                            LogUtils.e(TAG, e.getMessage(), e);
                        }
                    }
                }
                break;
            case Constant.App.REQUEST_GOTO_OPEN_PREVIEW:
                if (data != null && resultCode == Activity.RESULT_OK) {
                    String delIndex = data.getStringExtra(Constant.App.INDEX);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("delIndex", delIndex);
                        webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_DELETEIMAGES + "(" + jsonObject.toString() + ")");
                    } catch (JSONException e) {
                        LogUtils.e(TAG, e.getMessage(), e);
                    }
                }
                break;
            case Constant.App.REQUEST_GOTO_CATEGORY:
                if (data != null && resultCode == Activity.RESULT_OK) {
                    PushBean.categoryName = data.getStringExtra(Constant..INTENT_CATEGORYACTIVITY_NAME);
                    PushBean.categoryId = data.getStringExtra(Constant..INTENT_CATEGORYACTIVITY_ID);
                    selectCategoryView.setData(PushBean.categoryName, PushBean.categoryId, from);
                    setPushState(PushBean.isPushAndCache());
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void observeData() {
        mViewModel.loadingState.observe(new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    if (integer == PageLoadingState.START_PROGRESS) {
                        LoadingUtils.show(mActivity);
                        isLoading = true;
                    } else if (integer == PageLoadingState.HIDE) {
                        LoadingUtils.dismiss();
                        isLoading = false;
                    } else {
                        LoadingUtils.dismiss();
                        isLoading = false;
                    }
                }
            }
        });
        mViewModel.toastState.observe(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                ToastUtils.makeTextShow(s);
            }
        });
        mViewModel.toastStateErr.observe(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                ToastUtils.makeTextShow(s);
            }
        });
    }

    /**
     * 初始化缓存，让js主动触发renderEnd完成后初始化缓存
     */
    private void initCache() {
        ThreadPoolUtil.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                String cache = Helper.getCache();
                PushBean = gson.fromJson(cache, PushBean.class);
                mHandler.obtainMessage(Constant..WHAT_INIT_CACHE).sendToTarget();
            }
        });
    }

    /**
     * 初始化页面
     */
    private void initPage() {
        if (PushBean == null) {
            PushBean = new PushBean();
        } else {
            ExitAlertDialog dialog = new ExitAlertDialog(mActivity);
            dialog.builder();
            dialog.setCancelable(false);
            dialog.getLeftButton().setTextColor(getResources().getColor(R.color.*_gray9));
            dialog.getLeftButton().setTypeface(Typeface.DEFAULT);
            dialog.getRightButton().setTextColor(getResources().getColor(R.color.welink_main_color));
            dialog.getRightButton().setTypeface(Typeface.DEFAULT);
            dialog.setMsg(getString(R.string.*__draft_tips));
            dialog.setNegativeButton(getString(R.string.*__clear_draft), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //清空草稿
                    PushBean = new PushBean();
                    Helper.saveCache("");
                }
            });
            dialog.setPositiveButton(getString(R.string.*_continue), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 加载草稿
                    selectCategoryView.setData(PushBean.categoryName, PushBean.categoryId, from);
                    setPushState(PushBean.isPush);
                    try {//给js富文本传草稿
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("content", PushBean.Content);
                        webView.loadUrl("javascript: " + Constant.JsInterface.JSMETHOD_GETDRAFT + "(" + jsonObject.toString() + ")");
                    } catch (JSONException e) {
                        LogUtils.e(TAG, e.getMessage(), e);
                    }
                    etTitle.setText(PushBean.Title);
                }
            });
            dialog.show();
        }
        //初始化监听，解决H5加载慢，快速点击头部标题可以提交的问题
        initListener();
    }

    /**
     * 是否可以
     *
     * @param isPush 是否可以
     */
    private void setPushState(boolean isPush) {
        topBar.getTvRight().setTextColor(isPush ? getResources().getColor(R.color.*_white) : getResources().getColor(R.color.*_gray9));
        topBar.getTvRight().setClickable(isPush);
    }

    /**
     * 显示缓存弹框
     */
    private void showCacheDialog() {
        PushBean.isPushAndCache();
        if (PushBean.isCache) {
            ExitAlertDialog cacheDialog = Helper.getCacheDialog(mActivity, PushBean);
            cacheDialog.show();
        } else {
            finish();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FontSizeEvent event) {
        if (webView != null) {//切换字体大小
            webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_CHANGEFONTSIZE + "()");
        }
    }

    @Override
    public void releaseViews() {
        EventBus.getDefault().unregister(this);
        if (webView != null) {
            webView.setKInputConnectionListener(null);
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearCache(true);
            webView.setOnTouchListener(null);
            webView.setOnLongClickListener(null);
            webView.removeJavascriptInterface(Constant.JsInterface.JSINTERFACENAME_JSCALLJAVA);
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (rltContent != null) {
            rltContent.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        PhotoSelectHelper.onPermissionsGranted(requestCode, perms, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        PhotoSelectHelper.onPermissionsDenied(requestCode, perms, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onBackPressed() {//复写返回键
        saveCache();
    }

    private void saveCache() {
        if (isLoading) {
            finish();
            return;
        }
        Helper.hidenSoftInput(mActivity, rltContent);
        rltContent.clearFocus();
        //点击取消或者返回上一页面端时候，调H5方法，让H5调桥接，告诉当前页面调内容，来判断是否需要缓存
        webView.loadUrl("javascript:" + Constant.JsInterface.JSMETHOD_GETCONTENTS + "(" + Constant.JsInterface.JSMETHOD_GETCONTENT_PARAM1 + ")");
    }
}
