package com.jelly.baselibrary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jelly.baselibrary.R;

/**
 * Description: Glide封装 图片加载
 */
public class ImageUtil {

    /**
     * Description：使用Glide 加载 网络图片 设置为圆形
     *
     * @param context      上下文
     * @param imageUrl     图片地址
     * @param imageView    图片控件
     * @param defaultImage 默认图片
     */
    public static void loadCircleImage(Context context, String imageUrl, final ImageView imageView, int defaultImage) {
        if (imageView == null) {
            return;
        }
        if (defaultImage < 0) {// 设置默认图片
            defaultImage = R.drawable.base_ic_default;
        }
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(defaultImage);
            return;
        }
        if (context == null) {
            context = imageView.getContext();
        }
        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)// 全部缓存模式
                .priority(Priority.HIGH)// 优先级高
                .placeholder(defaultImage)// 默认图片
                .error(defaultImage);// 错误提示图片;
        final Context finalContext = context;
        Glide.with(context).asBitmap()
                .apply(requestOptions)
                .load(imageUrl)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(finalContext.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }
}
