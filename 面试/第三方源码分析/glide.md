### glide四级缓存
Glide 是一款强大的 Android 图片加载库，它支持四级缓存：活动缓存、内存缓存、磁盘缓存和网络缓存。
活动缓存(Activity Cache)：活动缓存是 Glide 提供的最小限度的缓存机制。它保存了当前正在运行的 Activity 的图片请求结果，以便在用户进行快速滑动等操作时能够更快地获取图片，提升用户体验。但是，当 Activity 销毁时，活动缓存也会随之清空。
内存缓存(Memory Cache)：内存缓存是指将图片数据存储在应用程序的内存中，这样可以快速访问图片而不需要再次从磁盘或网络中获取。Glide 内部使用了 LruCache 技术实现内存缓存，即根据最近最少使用原则来淘汰不常用的缓存数据，防止内存泄漏和 OOM 异常。
磁盘缓存(Disk Cache)：磁盘缓存是指将图片数据存储在设备的本地磁盘上，以便在下次访问相同图片时无需再次下载。Glide 默认使用硬盘缓存技术将图片以文件形式存储在设备的磁盘上。这种方式可以减少重复下载图片的时间和流量消耗，并且可以在无网络情况下正常加载图片。
网络缓存(Network Cache)：网络缓存是指将图片数据存储在远程服务器上，以便在下次访问相同图片时无需再次请求。Glide 会根据 HTTP 头部信息来判断是否启用网络缓存，如果服务器返回了合适的缓存头信息，那么 Glide 就会自动使用这个数据来展示图片，而不需要再次从网络中下载。

### 低内存的情况下，清除glide缓存
``` java
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }
```

### glide调用
``` java
    public static void loadImg(ImageView imageView, String imgUrl, @DrawableRes int defaultImg) {
        if (imageView == null) {
            return;
        }
        if (TextUtils.isEmpty(imgUrl)) {
            imageView.setImageResource(defaultImg);
            return;
        }
        Activity activity = Utils.getActivity(imageView.getContext());
        if (Utils.isActivityDestroy(activity)) {
            return;
        }
        RequestOptions options = RequestOptions.errorOf(defaultImg).placeholder(defaultImg);
        Glide.with(activity)
                .load(imgUrl)
                .apply(options)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }
```

### 源码分析-Glide.with(activity)
* with接收上下文或者生命周期，返回RequestManager对象，return getRetriever(activity).get(activity);
* 调用getRetriever方法返回RequestManagerRetriever对象return Glide.get(context).getRequestManagerRetriever();
* get方法是Glide中的一个单例，会传入缓存、Bitmap池相关的对象。
RequestManagerRetriever里面会创建RequestManagerFragment，并将glide和这个fragment一定绑定生命周期。

### 源码分析-RequestManager.load(imgUrl)
* load方法接收图片文件或地址，返回RequestBuilder
* 通过RequestBuilder进行glide加载配置：占位图、缓存模式等待。

### 源码分析-RequestBuilder.into(imageView)
* into方法接收imageView，根据scaleType给RequestOptions赋值。
* 创建Request，通过RequestManager管理请求，当生命周期符合的时候，通过Request.begin()发起图片加载请求。
* begin方法有我们熟悉的onLoadStarted、onResourceReady、onLoadFailed回调。
* onSizeReady方法会根据图片的宽高等信息生成一个EngineKey，这个key是唯一的，与加载的图片一一对应。
* 通过生成的EngineKey，调用loadFromMemory方法，来获取图片资源EngineResource。
* 获取到了资源，都会回调到SingleRequest的onResourceReady方法。








