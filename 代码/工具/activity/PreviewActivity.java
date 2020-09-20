import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PreviewActivity extends BaseActivity implements View.OnClickListener, I*Result {
    private static final int HAVE_NET = 1600;//有网络
    private static final int NO_NET = 1601;//无网络

    public class UIHandler extends Handler {
        private WeakReference<PreviewActivity> self;

        public UIHandler(PreviewActivity self) {
            this.self = new WeakReference<PreviewActivity>(self);
        }

        @Override
        public void handleMessage(Message msg) {
            if (self.get() == null) {
                return;
            }
            try {
                switch (msg.what) {
                    case NO_NET:
                        network_erro.setVisibility(View.VISIBLE);
                        break;
                    case HAVE_NET:
                        network_erro.setVisibility(View.GONE);
                        break;
                    case CHANGE_PROPAGATEVIEW:
//                        StringUtils.destroyMemory(PreviewActivity.this);
                        if (showNumber > DateUtils.propagatePics.size() - 1) {
                            showNumber = 0;
                        }
                        DateUtils.propagatePicsCache.clear();
                        DateUtils.propagatePicsCache.add(DateUtils.propagatePics.get(showNumber));
                        showNumber++;

//                        if (DateUtils.propagateBitmapCache != null && !DateUtils.propagateBitmapCache.isRecycled()) {
//                            propagateImageView.setImageBitmap(DateUtils.propagateBitmapCache);
//                            mHandler.sendEmptyMessageDelayed(DEAL_VIEWFLIPPER_COM, IPreferences.getPicChangeTime());
//                        } else {
                        showImageView();
//                        }
//                                propagateImageView.setVisibility(View.VISIBLE);
//                                llt_preview_contain.removeAllViews();
//                                llt_preview_contain.setVisibility(View.GONE);
//
//                        FileUtil.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (showNumber > DateUtils.propagatePics.size() - 1) {
//                                    StringUtils.readBitMap(PreviewActivity.this, DateUtils.propagatePics.get(0));
//                                } else {
//                                    StringUtils.readBitMap(PreviewActivity.this, DateUtils.propagatePics.get(showNumber));
//                                }
//                            }
//                        });
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LogTools.getInstance().d("PreviewActivity", "App异常：PreviewActivity", e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.tv_preview_activity);
        initView();
    }

    private void initView() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstantUtil.BROADCAST_DOWNLOAD_ACTION);
        localReceiver = new LocalReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, filter);
        timer = new Timer();
        checkUpdateTask = new CheckUpdateTask();
        timer.schedule(checkUpdateTask, IPreferences.getUpgradeCheckTime());

        if (timer == null) {
            timer = new Timer();
        }
        if (checkUpdateTask == null) {
            checkUpdateTask = new PreviewActivity.CheckUpdateTask();
            timer.schedule(checkUpdateTask, IPreferences.getUpgradeCheckTime());
        }
    }

    private void initPopWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pop_menu_layout, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.px300));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        popupWindow.setBackgroundDrawable(dw);
        initPopView(view);
    }

    public void initPopView(View view) {
        RelativeLayout rl_pop = (RelativeLayout) view.findViewById(R.id.rl_pop);
        rl_pop.getBackground().setAlpha(178);
        ll_menu = (LinearLayout) view.findViewById(R.id.ll_menu);
        ibv_logoff = (TextView) view.findViewById(R.id.ibv_logoff);
        ibv_logoff.requestFocus();
        ibv_logout = (TextView) view.findViewById(R.id.ibv_logout);
        ibv_update = (TextView) view.findViewById(R.id.ibv_update);
        ibv_about = (TextView) view.findViewById(R.id.ibv_about);
        tv_logout_title = (TextView) view.findViewById(R.id.tv_logout_title);
        ll_logout = (LinearLayout) view.findViewById(R.id.ll_logout);
        ibv_navigate = (TextView) view.findViewById(R.id.ibv_navigate);
        ibv_positive = (TextView) view.findViewById(R.id.ibv_positive);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibv_navigate:
                setPopContentVisible(PPW_MENU);
                popupWindow.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            setPopContentVisible(PPW_MENU);
            showPopup();
            return super.onKeyDown(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setPopContentVisible(PPW_LOGOUT);
            showPopup();
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPopup() {
        if (popupWindow != null) {
            popupWindow.showAtLocation(this.findViewById(R.id.rlt_preview), Gravity.BOTTOM, 0, 0);
        }
    }

    @Subscribe
    public void onEventMainThread(UpdateEvent event) {
        if (event.getIsUpdated()) {
            Intent intent;
            intent = new Intent(this, UpdateActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public class CheckUpdateTask extends TimerTask {
        @Override
        public void run() {
            if (IPreferences.getAppStore().equals("0")) {
                StringUtils.checkUpdate(PreviewActivity.this, 0);
            } else {
                if (checkUpdateTask != null) {
                    checkUpdateTask.cancel();
                    checkUpdateTask = null;
                }
                tvPreviewRequest.checkVersion();
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            if (null != popupWindow) {
                popupWindow.dismiss();
            }
            if (EventBus.getDefault() != null) {
                EventBus.getDefault().unregister(this);
            }
            if (LocalBroadcastManager.getInstance(this) != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
            }
            if (llt_preview_contain != null) {
                llt_preview_contain.removeAllViews();
            }
            if (checkUpdateTask != null) {
                checkUpdateTask.cancel();
                checkUpdateTask = null;
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Exception e) {
            LogTools.getInstance().d("PreviewActivity", "页面销毁异常==" + e.toString());
        }
        super.onDestroy();
    }

    public void showImageView() {
        final long firstTime = System.currentTimeMillis();
        LogTools.getInstance().d("TVHandler", "问题定位异常：去文件夹找到对应的宣传图片展示");
        //左边图片展示,null显示默认图片，先显示缓存文件夹的图片
        String fileDirectoryPath = *CacheUtils.getPropagateFilePath(this);
        File fileDirectory = new File(fileDirectoryPath);
        File[] files = fileDirectory.listFiles();
        if (files == null || files.length < 1) {
            changeView();
            return;
        }

        boolean hasFile = false;
        for (File imageFile : files) {
            for (String picName : DateUtils.propagatePicsCache) {
                if (imageFile.getName().contains(picName)) {
//                    showView(imageFile);

                    showView(imageFile, new GetBitmapCallBack() {
                        @Override
                        public void getSuccess(Bitmap bitmap) {
                            LogTools.getInstance().d("TVHandler", "问题定位异常：得到宣传图片Bitmap成功，隔0.5秒展示");
                            propagateImageView.setImageBitmap(bitmap);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    propagateImageView.setVisibility(View.VISIBLE);
                                    llt_preview_contain.setVisibility(View.GONE);
                                    llt_preview_contain.removeAllViews();
                                    //设置轮播时长
                                    long currentTime = System.currentTimeMillis();
                                    mHandler.sendEmptyMessageDelayed(DEAL_VIEWFLIPPER_COM, IPreferences.getPicChangeTime() + currentTime - firstTime);
                                }
                            }, 500);
                        }
                    });
                    hasFile = true;
                }
            }
        }
        if (!hasFile) {
            changeView();
        }
    }

    public void showView(final File imageFile, final GetBitmapCallBack callback) {
        LogTools.getInstance().d("TVHandler", "问题定位异常：先得到宣传图片Bitmap");
        FileUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    bitmap = Glide.with(PreviewActivity.this)
                            .load(imageFile)
                            .asBitmap()
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.getSuccess(bitmap);
                        }
                    });
                } catch (Exception e) {
                    LogTools.getInstance().d("TVHandler", "问题定位异常：开始展示宣传图片", e);
                }
            }
        });
    }

    public void showView(File imageFile) {
        LogTools.getInstance().d("TVHandler", "问题定位异常：开始展示宣传图片");
        ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);
        Glide.with(this)//在子线程操作时，需要换成context.getApplicationContext()
                .load(imageFile)//加载图片
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存模式
                .priority(Priority.HIGH)//优先级
                .placeholder(drawable)//默认图片
                .error(drawable)//加载错误提示的图片
                .into(propagateImageView);
    }

    public interface GetBitmapCallBack {
        void getSuccess(Bitmap bitmap);
    }
}
