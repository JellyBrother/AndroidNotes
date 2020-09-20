package com.*.weight;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.*.R;
import com.*.utils.IPreferences;

public class ProgressImageView extends ImageView {
    private Resources resources;
    private Paint acceptPaint;
    private Paint checkPaint;
    private Paint servicePaint;
    private Paint getPaint;

    public ProgressImageView(Context context) {
        super(context);
        initView(context);
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
         super(context, attrs);
        initView(context);
    }

    public ProgressImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        resources = context.getResources();
        acceptPaint = new Paint();
        checkPaint = new Paint();
        servicePaint = new Paint();
        getPaint = new Paint();

        //设置画笔宽度，线宽
        int paintSize = 1;
        //画笔颜色
        int blueColor = resources.getColor(R.color.blue_2d9dff);
        /**
         * Paint.Style.STROKE 只绘制图形轮廓（描边）
         Paint.Style.FILL 只绘制图形内容
         Paint.Style.FILL_AND_STROKE 既绘制轮廓也绘制内容
         */
        Paint.Style style = Paint.Style.FILL;

        acceptPaint.setStrokeWidth(paintSize);
        checkPaint.setStrokeWidth(paintSize);
        servicePaint.setStrokeWidth(paintSize);
        getPaint.setStrokeWidth(paintSize);

        acceptPaint.setColor(blueColor);
        checkPaint.setColor(blueColor);
        servicePaint.setColor(blueColor);
        getPaint.setColor(blueColor);

        acceptPaint.setStyle(style);
        checkPaint.setStyle(style);
        servicePaint.setStyle(style);
        getPaint.setStyle(style);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //先画图片
        super.onDraw(canvas);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        //偏移量
        int offset = (viewWidth - viewHeight) / 2;
        //加判断，如果是英文，字体要调整
//        int textSize = (int) resources.getDimension(R.dimen.px36);
        int textSize = viewHeight / 11;
        switch (IPreferences.getLanguage()) {
            case IPreferences.KEY_CHINESE:
                acceptPaint.setTextScaleX((float) 1);
                checkPaint.setTextScaleX((float) 1);
                servicePaint.setTextScaleX((float) 1);
                getPaint.setTextScaleX((float) 1);
                break;
            case IPreferences.KEY_ENGLISH:
            default:
//            textSize = viewHeight/14;
                acceptPaint.setTextScaleX((float) 0.7);
                checkPaint.setTextScaleX((float) 0.7);
                servicePaint.setTextScaleX((float) 0.7);
                getPaint.setTextScaleX((float) 0.7);
                break;
        }

        acceptPaint.setTextSize(textSize);
        checkPaint.setTextSize(textSize);
        servicePaint.setTextSize(textSize);
        getPaint.setTextSize(textSize);

        //画文字路径：RectF是文字线路，第二个参数是起点，第三个参数是终点
        RectF acceptRefct = new RectF(offset + viewHeight * 5 / 16, viewHeight * 5 / 16, viewHeight * 11 / 16 + offset, viewHeight * 11 / 16);
        Path acceptPath = new Path();
        acceptPath.addArc(acceptRefct, 285, 80);

//        RectF checkRefct = new RectF(offset + viewHeight / 4, viewHeight / 4, viewHeight * 3 / 4 + offset, viewHeight * 3 / 4);
        RectF checkRefct = new RectF(offset + viewHeight * 5 / 16, viewHeight * 5 / 16, viewHeight * 11 / 16 + offset, viewHeight * 11 / 16);
//        canvas.drawArc(checkRefct, 0, 360, false, getPaint);
        Path checkPath = new Path();
        checkPath.addArc(checkRefct, 75, -80);

        RectF serviceRefct = new RectF(offset + viewHeight * 5 / 16, viewHeight * 5 / 16, viewHeight * 11 / 16 + offset, viewHeight * 11 / 16);
        Path servicePath = new Path();
        servicePath.addArc(serviceRefct, 165, -80);

        RectF getRefct = new RectF(offset + viewHeight * 5 / 16, viewHeight * 5 / 16, viewHeight * 11 / 16 + offset, viewHeight * 11 / 16);
        Path getPath = new Path();
        getPath.addArc(getRefct, 195, 80);

        //进度提示文字
        String acceptText = resources.getString(R.string.main_repair_accept);//受理
        String checkText = resources.getString(R.string.main_repair_check);//检测
        String serviceText = resources.getString(R.string.main_repair_service);//维修
        String getText = resources.getString(R.string.main_repair_get);//取机

        //把文字画上去
        canvas.drawTextOnPath(acceptText, acceptPath, 0, 0, acceptPaint);
        canvas.drawTextOnPath(checkText, checkPath, 0, 25, checkPaint);
        canvas.drawTextOnPath(serviceText, servicePath, 0, 25, servicePaint);
        canvas.drawTextOnPath(getText, getPath, 0, 0, getPaint);
    }

    /**
     * 设置维修进度,供外部调用
     */
    public void setRepairSchedule(String type) {
//        Drawable checkDrawable = resources.getDrawable(R.mipmap.main_detail_green);
//        setUp(checkPaint, checkDrawable);

        switch (type + "") {
            case "2":
                Drawable checkDrawable = resources.getDrawable(R.mipmap.main_detail_check);
                setUp(checkPaint, checkDrawable);
                break;
            case "3":
                Drawable serviceDrawable = resources.getDrawable(R.mipmap.main_detail_service);
                setUp(servicePaint, serviceDrawable);
                break;
            case "4":
                Drawable getDrawable = resources.getDrawable(R.mipmap.main_detail_get);
                setUp(getPaint, getDrawable);
                break;
            case "1":
            default: 
                Drawable accpetDrawable = resources.getDrawable(R.mipmap.main_detail_accpet);
                setUp(acceptPaint, accpetDrawable);
                break;
        }
    }

    private void setUp(Paint paint, Drawable drawable) {
        setImageDrawable(drawable);
        int whiteColor = resources.getColor(R.color.white);
        //设置画笔颜色
        paint.setColor(whiteColor);
        invalidate();
    }
}