package com.jelly_.jelly_.mobile.tv.weight;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jelly_.jelly_.mobile.tv.R;
import com.jelly_.jelly_.mobile.tv.activity.TVPreviewActivity;
import com.jelly_.jelly_.mobile.tv.db.Entity;
import com.jelly_.jelly_.mobile.tv.utils.DateUtils;
import com.jelly_.jelly_.mobile.tv.utils.HMACShaUtils;
import com.jelly_.jelly_.mobile.tv.utils.IPreferences;
import com.jelly_.jelly_.mobile.tv.utils.ShopHwaConstants;
import com.jelly_.jelly_.mobile.tv.utils.ShopHwaTools;
import com.jelly_.jelly_.mobile.tv.utils.StringUtils;
import com.jelly_.jelly_.mobile.tv.utils.zip.Zip4jUtil;
import com.jelly_.hae.mcloud.bundle.logbundle.utils.LogTools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 员工列表页面
 * v1.2开发
 * Created by lwx334725 on 2017/07/04.
 */
public class StaffListView extends LinearLayout {
    private static final int DEAL_EMPLOYEEINFOR = 1300;//间隔处理员工信息
    private static final int DEAL_CHANGEVIEW = 1301;//开始切换view
    private Context context;
    private TextView tvw_date;
    private TextView tvw_time;
    private LinearLayout llt_first_row;
    private LinearLayout llt_second_row;
    private StaffListViewHandler mHandler;
    private List<Entity> allEmployeeInforList = new ArrayList<>();
    private List<Entity> employeeInforSubList = new ArrayList<>();
    private ShopManagerView shopManagerView;
    private int size = 0;
    private int showSize = 11;//显示条数
    private int itemSize = 12;//当前也能总共显示条数
    private List<Entity> allShopAssistantList = new ArrayList<>();
    private List<Entity> shopAssistantSubList = new ArrayList<>();
    //因为这个页面图片太多，当页面切换到下一个页面的时候，会有空白出现，所以当切换的时候，显示一个华为logo
    private ImageView ivw_default;
    private List<ShopAssistantView> shopViewList = new ArrayList<>();

    public class StaffListViewHandler extends Handler {
        private WeakReference<StaffListView> self;

        public StaffListViewHandler(StaffListView self) {
            this.self = new WeakReference<StaffListView>(self);
        }

        @Override
        public void handleMessage(Message msg) {
            if (self.get() == null) {
                return;
            }
            try {
                switch (msg.what) {
                    case DEAL_EMPLOYEEINFOR:
                        dealEmployeeInfor();
                        break;
                    case DEAL_CHANGEVIEW:
                        changeView();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (Exception e) {
                LogTools.getInstance().d("StaffListView", "App异常：员工列表页面", e);
                Map<String, String> hwaMap = HMACShaUtils.getHwaMap();
                hwaMap.put("code", "0");
                hwaMap.put(ShopHwaConstants.APP_LAUCHER_EXCEPTION, "App异常::员工列表页面" + StringUtils.getExceptionString(e));
                ShopHwaTools.recordAction(context, ShopHwaConstants.APP_LAUCHER_EXCEPTION, Zip4jUtil.toJSON(hwaMap));
                changeView();
            }
        }
    }

    public StaffListView(Context context) {
        this(context, null);
    }

    public StaffListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StaffListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LogTools.getInstance().d("TVHandler", "问题定位异常：StaffListView初始化");
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        mHandler = new StaffListViewHandler(this);
        View root = View.inflate(context, R.layout.view_staff_list, this);
        tvw_date = (TextView) root.findViewById(R.id.tvw_date);
        tvw_time = (TextView) root.findViewById(R.id.tvw_time);
        ivw_default = (ImageView) root.findViewById(R.id.ivw_default);
        llt_first_row = (LinearLayout) root.findViewById(R.id.llt_first_row);
        llt_second_row = (LinearLayout) root.findViewById(R.id.llt_second_row);
        //展示员工信息
//        showEmployeeInfor();
    }

    public void showEmployeeInfor() {
        LogTools.getInstance().d("TVHandler", "问题定位异常：StaffListView处理数据");
        StringUtils.showTime(tvw_time);
        StringUtils.showDate(context, tvw_date);
        ivw_default.setVisibility(INVISIBLE);

        if (DateUtils.shopManagerList == null || DateUtils.shopManagerList.isEmpty()) {
            //没有店长，也没有店员
            if (DateUtils.shopAssistantList == null || DateUtils.shopAssistantList.isEmpty()) {
                llt_first_row.setVisibility(GONE);
                llt_second_row.setVisibility(GONE);
                changeView();
                return;
            }
            //全部都是店员
            allShopAssistantList.clear();
            allShopAssistantList.addAll(DateUtils.shopAssistantList);
            showShopAssistant();
            return;
        }
        //全部都是店长(只取第一个店长展示)
        if (DateUtils.shopAssistantList == null || DateUtils.shopAssistantList.isEmpty()) {
            llt_second_row.setVisibility(GONE);
            llt_first_row.removeAllViews();
            shopManagerView = getShopManagerView(DateUtils.shopManagerList, 0);//店长
            llt_first_row.addView(shopManagerView);
            llt_first_row.setVisibility(VISIBLE);
            mHandler.sendEmptyMessageDelayed(DEAL_CHANGEVIEW, IPreferences.getEmployeeInfoTime());
            return;
        }
        //有店长，也有店员(只取第一个店长展示)
        setEmployeeInforResult();
    }

    private void showShopAssistant() {
        long firstTime = System.currentTimeMillis();
        llt_first_row.removeAllViews();
        llt_second_row.removeAllViews();
        int size = allShopAssistantList.size();
        if (size <= itemSize / 2) {//小于等于6个
            llt_second_row.setVisibility(GONE);
            for (int i = 0; i < size; i++) {
                ShopAssistantView shopAssistantView = getShopAssistantView(allShopAssistantList, i);
                llt_first_row.addView(shopAssistantView);
            }
            llt_first_row.setVisibility(VISIBLE);
            long currentTime = System.currentTimeMillis();
            mHandler.sendEmptyMessageDelayed(DEAL_CHANGEVIEW, IPreferences.getEmployeeInfoTime() + currentTime - firstTime);
            return;
        }
        if (size <= itemSize) {//大于6小于12
            for (int i = 0; i < size; i++) {
                ShopAssistantView shopAssistantView = getShopAssistantView(allShopAssistantList, i);
                int model = i % 2;//模
                if (model == 0) {//整除，双数，第一行
                    llt_first_row.addView(shopAssistantView);
                } else {//不整除，单数，第二行
                    llt_second_row.addView(shopAssistantView);
                }
            }
            llt_first_row.setVisibility(VISIBLE);
            llt_second_row.setVisibility(VISIBLE);
            long currentTime = System.currentTimeMillis();
            mHandler.sendEmptyMessageDelayed(DEAL_CHANGEVIEW, IPreferences.getEmployeeInfoTime() + currentTime - firstTime);
            return;
        }
        //大于12，分页
        for (int i = 0; i < itemSize; i++) {
            ShopAssistantView shopAssistantView = getShopAssistantView(allShopAssistantList, i);
            int model = i % 2;//模
            if (model == 0) {//整除，双数，第一行
                llt_first_row.addView(shopAssistantView);
            } else {//不整除，单数，第二行
                llt_second_row.addView(shopAssistantView);
            }
        }
        llt_first_row.setVisibility(VISIBLE);
        llt_second_row.setVisibility(VISIBLE);

        shopAssistantSubList.clear();
        shopAssistantSubList.addAll(allShopAssistantList.subList(itemSize, size));
        allShopAssistantList.clear();
        allShopAssistantList.addAll(shopAssistantSubList);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //去掉时间接收器
                StringUtils.showTime(tvw_time);
                StringUtils.showDate(context, tvw_date);
                showShopAssistant();
            }
        }, IPreferences.getEmployeeInfoTime());
    }

    private void setEmployeeInforResult() {
        allEmployeeInforList.clear();
        allEmployeeInforList.add(DateUtils.shopManagerList.get(0));
        allEmployeeInforList.addAll(DateUtils.shopAssistantList);
        size = allEmployeeInforList.size();
        shopManagerView = getShopManagerView(allEmployeeInforList, 0);//店长
        if (size > showSize) {
            showEmployeeInforFirst(allEmployeeInforList.subList(0, showSize));
            employeeInforSubList.clear();
            employeeInforSubList.addAll(allEmployeeInforList.subList(showSize, size));
            allEmployeeInforList.clear();
            allEmployeeInforList.addAll(employeeInforSubList);
        } else {
            showEmployeeInforFirst(allEmployeeInforList);
            allEmployeeInforList.clear();
        }
        mHandler.sendEmptyMessageDelayed(DEAL_EMPLOYEEINFOR, IPreferences.getEmployeeInfoTime());
    }

    private void dealEmployeeInfor() {
        //去掉时间接收器
        StringUtils.showTime(tvw_time);
        StringUtils.showDate(context, tvw_date);
        if (allEmployeeInforList == null || allEmployeeInforList.isEmpty()) {
            llt_first_row.setVisibility(GONE);
            llt_second_row.setVisibility(GONE);
            changeView();
            return;
        }
        long firstTime = System.currentTimeMillis();
        int size = allEmployeeInforList.size();
        if (size > showSize - 1) {
            showEmployeeInforSecond(allEmployeeInforList.subList(0, showSize - 1));
            employeeInforSubList.clear();
            employeeInforSubList.addAll(allEmployeeInforList.subList(showSize - 1, size));
            allEmployeeInforList.clear();
            allEmployeeInforList.addAll(employeeInforSubList);
        } else {
            showEmployeeInforSecond(allEmployeeInforList);
            allEmployeeInforList.clear();
        }
        long currentTime = System.currentTimeMillis();
        mHandler.sendEmptyMessageDelayed(DEAL_EMPLOYEEINFOR, IPreferences.getEmployeeInfoTime() + currentTime - firstTime);
    }

    private void showEmployeeInforFirst(List<Entity> employeeInforList) {
        llt_first_row.removeAllViews();
        llt_second_row.removeAllViews();
        llt_first_row.addView(shopManagerView);
        int size = employeeInforList.size();
        if (size <= 5) {//店长+员工<=5人时，显示方式为：只显示一排，上下居中；
            llt_second_row.setVisibility(GONE);
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    ShopAssistantView shopAssistantView = getShopAssistantView(employeeInforList, i);
                    llt_first_row.addView(shopAssistantView);
                }
            }
            llt_first_row.setVisibility(VISIBLE);
            return;
        }
        //11>当店长+员工>5人时，显示方式按原型2中的方式展示，显示两排，左右居中
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                ShopAssistantView shopAssistantView = getShopAssistantView(employeeInforList, i);
                if (i == 1) {//第二个
                    llt_second_row.addView(shopAssistantView);
                } else {
                    int model = i % 2;//模
                    if (model == 0) {//整除，双数，第一行
                        llt_second_row.addView(shopAssistantView);
//                            llt_first_row.addView(shopAssistantView);
                    } else {//不整除，单数，第二行
                        llt_first_row.addView(shopAssistantView);
                    }
                }
            }
        }
        llt_first_row.setVisibility(VISIBLE);
        llt_second_row.setVisibility(VISIBLE);
    }

    @NonNull
    private ShopAssistantView getShopAssistantView(List<Entity> employeeInforList, int i) {
        Entity shopAssistant = employeeInforList.get(i);//店员
        if (shopViewList.size() > 0) {
            for (ShopAssistantView shopAssistantView : shopViewList) {
                if (shopAssistantView.getParent() == null) {
                    shopAssistantView.setShopAssistant(shopAssistant);
                    return shopAssistantView;
                }
            }
        }
        ShopAssistantView shopAssistantView = new ShopAssistantView(context);
        shopAssistantView.setShopAssistant(shopAssistant);
        shopViewList.add(shopAssistantView);
        return shopAssistantView;
    }

    @NonNull
    private ShopManagerView getShopManagerView(List<Entity> list, int i) {
        if (shopManagerView == null) {
            shopManagerView = new ShopManagerView(context);
        }
        Entity shopManager = list.get(i);//店员
        shopManagerView.setShopManager(shopManager);
        return shopManagerView;
    }

    private void showEmployeeInforSecond(List<Entity> employeeInforList) {
        llt_first_row.removeAllViews();
        llt_second_row.removeAllViews();
        llt_first_row.addView(shopManagerView);
        int size = employeeInforList.size();
        if (size <= 4) {//店长+员工<=5人时，显示方式为：只显示一排，上下居中；
            llt_second_row.setVisibility(GONE);
            for (int i = 0; i < size; i++) {
                ShopAssistantView shopAssistantView = getShopAssistantView(employeeInforList, i);
                llt_first_row.addView(shopAssistantView);
            }
            llt_first_row.setVisibility(VISIBLE);
            return;
        }
        //11>当店长+员工>5人时，显示方式按原型2中的方式展示，显示两排，左右居中
        for (int i = 0; i < size; i++) {
            ShopAssistantView shopAssistantView = getShopAssistantView(employeeInforList, i);
            if (i == 0) {//第一个
                llt_second_row.addView(shopAssistantView);
            } else {
                int model = i % 2;//模
                if (model == 0) {//整除，双数，第二行
                    llt_first_row.addView(shopAssistantView);
//                        llt_second_row.addView(shopAssistantView);
                } else {//不整除，单数，第一行
                    llt_second_row.addView(shopAssistantView);
                }
            }
        }
        llt_first_row.setVisibility(VISIBLE);
        llt_second_row.setVisibility(VISIBLE);
    }

    //根据展示顺序进行判断：维修-工作人员-宣传图片
    private void changeView() {
        LogTools.getInstance().d("TVHandler", "问题定位异常：StaffListView切换页面");
        if (context instanceof TVPreviewActivity) {
            //如果宣传图片下载途中出错，只下载了部分图片
            StringUtils.isShowPropagateView(context);
            //还要判断是否有数据
            if (DateUtils.isShowPropagateView) {
                ivw_default.setVisibility(VISIBLE);
                llt_first_row.removeAllViews();
                llt_second_row.removeAllViews();
                ((TVPreviewActivity) context).changePropagateView();
                onDestroy();
                return;
            }
            if (!DateUtils.isRequestRepairComplete) {//正在请求网络,就需要让网络请求完
                mHandler.sendEmptyMessageDelayed(DEAL_CHANGEVIEW, DateUtils.handlerDelaye_RequestAgain);
                return;
            }
            if (DateUtils.isShowRepairView) {
                DateUtils.isFinishShowWait = false;
                DateUtils.isFinishShowRepair = false;
                ((TVPreviewActivity) context).changeRepairView();
                onDestroy();
                return;
            }
            if (!DateUtils.isRequestStaffComplete) {//正在请求网络,就需要让网络请求完
                mHandler.sendEmptyMessageDelayed(DEAL_CHANGEVIEW, DateUtils.handlerDelaye_RequestAgain);
                return;
            }
            if (DateUtils.isShowStaffListView) {
                ivw_default.setVisibility(VISIBLE);
                DateUtils.isChangeStaffListView = true;
                ((TVPreviewActivity) context).changeStaffListView();
            } else {
                DateUtils.isFinishShowWait = false;
                DateUtils.isFinishShowRepair = false;
                DateUtils.isShowRepairViewNoData = true;
                ((TVPreviewActivity) context).changeRepairView();
            }
            onDestroy();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDestroy();
    }

    private void onDestroy() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            LogTools.getInstance().d("StaffListView", "页面销毁异常==" + e.toString());
        }
    }
}
