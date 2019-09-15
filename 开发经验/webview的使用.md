参考网址：
https://www.jianshu.com/p/2bd29b3e12f9
https://blog.csdn.net/harvic880925/article/details/51523983
https://blog.csdn.net/KevinsCSDN/article/details/52241334

//////////////////////////////////////webview的设置///////////////////////////////////////////////////	
setDomStorageEnabled(true);//DOM存储API是否可用，默认false。
setDatabaseEnabled(true);//数据库存储API是否可用，默认值false。
setAllowFileAccess(true);//是否允许访问文件，默认允许
setSavePassword(false);//是否保存密码
//是否允许运行在一个URL环境（the context of a file scheme URL）中的JavaScript访问来自其他URL环境的内容，为了保证安全，应该不允许
setAllowFileAccessFromFileURLs(false);
//是否允许运行在一个file schema URL环境下的JavaScript访问来自其他任何来源的内容，包括其他file schema URLs，为了保证安全，应该不允许
setAllowUniversalAccessFromFileURLs(false);
//设置WebView是否允许执行JavaScript脚本，默认false，不允许。
setJavaScriptEnabled(true);
/**
 *WebView是否支持HTML的“viewport”标签或者使用wide viewport。设置值为true时，
 *布局的宽度总是与WebView控件上的设备无关像素（device-dependent pixels）宽度一致。
 *当值为true且页面包含viewport标记，将使用标签指定的宽度。如果页面不包含标签或者标签没有提供宽度，那就使用wide viewport。
 **/
setUseWideViewPort(true);
//是否允许WebView度超出以概览的方式载入页面，默认false。即缩小内容以适应屏幕宽度。
setLoadWithOverviewMode(true);
//WebView是否支持使用屏幕上的缩放控件和手势进行缩放，默认值true
setSupportZoom(true);
setBuiltInZoomControls(true);//是否使用内置的缩放机制
setDisplayZoomControls(false);//使用内置的缩放机制时是否展示缩放控件，默认值true
setSaveFormData(false);//WebView是否保存表单数据，默认值true。
if (VERSION.SDK_INT >= 21) {
    //当一个安全的来源（origin）试图从一个不安全的来源加载资源时配置WebView的行为
    setMixedContentMode(2);//1、不允许 2、允许 3、用户决定
}
setAppCacheEnabled(true);//应用缓存API是否可用，默认值false, 结合setAppCachePath(String)使用。
String path = activity.getCacheDir().getPath() + "/appCache";
File localFile = new File(path);
if ((!(localFile.exists())) && (!(localFile.mkdir()))){
    LogUtils.d("H5WebView:Make dir failed");
}
setAppCachePath(path);//设置应用缓存文件的路径
//让JavaScript自动打开窗口，默认false。适用于JavaScript方法window.open()。
setJavaScriptCanOpenWindowsAutomatically(true); 
//设置布局，会引起WebView的重新布局（relayout）,默认值NARROW_COLUMNS
//setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//调用requestFocus(int, Android.graphics.Rect)时是否需要设置节点获取焦点，默认值为true
setNeedInitialFocus(true);
setTextZoom (int textZoom) //设置页面上的文本缩放百分比，默认100。
setMinimumFontSize (int size) //设置最小的字号，默认为8
setMinimumLogicalFontSize (int size) //设置最小的本地字号，默认为8。
setCursiveFontFamily (String font) //设置WebView字体库字体，默认“cursive”
setDefaultFixedFontSize (int size) //设置默认固定的字体大小，默认为16，可取值1到72
setDefaultFontSize (int size) //设置默认的字体大小，默认16，可取值1到72
setDefaultTextEncodingName (String encoding) //设置默认的字符编码集，默认”UTF-8”.
setFantasyFontFamily (String font) //设置fantasy字体集（font family）的名字默认为“fantasy”
setFixedFontFamily (String font) //设置固定的字体集的名字，默认为”monospace”。
setSansSerifFontFamily (String font) //设置无衬线字体集（sans-serif font family）的名字。默认值”sans-serif”.
setSerifFontFamily (String font) //设置衬线字体集（serif font family）的名字，默认“sans-serif”
setStandardFontFamily (String font) //设置标准字体集的名字，默认值“sans-serif”。
setGeolocationEnabled (boolean flag) //定位是否可用，默认为true
setBlockNetworkImage(boolean) //只控制使用网络URI的图片的下载
setMediaPlaybackRequiresUserGesture (boolean require) //WebView是否需要用户的手势进行媒体播放，默认值为true。
//设置WebView是否支持多窗口。如果设置为true，主程序要实现onCreateWindow(WebView, boolean, boolean, Message)，默认false。
setSupportMultipleWindows (boolean support) 
//设置WebView的用户代理字符串。如果字符串为null或者empty，将使用系统默认值。注意从KITKAT版本开始，加载网页时改变用户代理会让WebView再次初始化加载。
setUserAgentString (String ua) 
supportMultipleWindows () //获取WebView是否支持多窗口的值。
supportZoom () //获取WebView是否支持缩放的值。
//////////////////////////////////////webview的设置///////////////////////////////////////////////////

//////////////////////////////////////WebViewClient中函数概述///////////////////////////////////////////////////	
onPageStarted //在开始加载网页时会回调
boolean shouldOverrideUrlLoading //在url发生改变时会回调，返回值是boolean类型，表示是否屏蔽WebView继续加载URL的默认行为
onPageFinished //在结束加载网页时会回调
onReceivedError //加载错误的时候会回调，在其中可做错误处理，比如再请求加载一次，或者提示404的错误页面
onReceivedSslError //当接收到https错误时，会回调此函数，在其中可以做错误处理
/**
 *在每一次请求资源时，比如超链接、JS文件、CSS文件、图片等，都会通过这个函数来回调，
 *但是回调是在子线程执行的，安卓需要用handler刷新页面，不处理就直接返回null，，系统会继续加载该资源。
 **/
WebResourceResponse shouldInterceptRequest(WebView view, String url) 
备注：
1、在SSL出错时，是不会触发onReceivedError回调的，
只会执行onReceivedSslError，而系统在super.onReceivedSslError(view, handler, error)会默认关闭请求。
所以我们必须注释掉super.onReceivedSslError(view, handler, error)来取消这个默认行为！ 再调用handler.proceed();来忽略错误继续加载页面。
当再次出现错误的时候，就会调用 onReceivedError了。
2、滑动事件监听：
继承webview，重写onScrollChanged方法，自己定义接口，给外部进行回调。
3、使用外部浏览器打开网页
Uri uri = Uri.parse("http://www.example.com"); 
Intent intent = new Intent(Intent.ACTION_VIEW, uri); 
startActivity(intent);

其余不常用api：
onLoadResource //在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次
onScaleChanged //WebView发生改变时调用
/**
 * 重写此方法才能够处理在浏览器中的按键事件。
 * 是否让主程序同步处理Key Event事件，如过滤菜单快捷键的Key Event事件。
 * 如果返回true，WebView不会处理Key Event，
 * 如果返回false，Key Event总是由WebView处理。默认：false
 */
public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event)
onFormResubmission //是否重发POST请求数据，默认不重发。
doUpdateVisitedHistory //更新访问历史
/**
 * 通知主程序输入事件不是由WebView调用。是否让主程序处理WebView未处理的Input Event。
 * 除了系统按键，WebView总是消耗掉输入事件或shouldOverrideKeyEvent返回true。
 * 该方法由event 分发异步调用。注意：如果事件为MotionEvent，则事件的生命周期只存在方法调用过程中，
 * 如果WebViewClient想要使用这个Event，则需要复制Event对象。
 */
onUnhandledInputEvent(WebView view, InputEvent event)
 /**
 * 通知主程序执行了自动登录请求。
 */
onReceivedLoginRequest(WebView view, String realm, String account, String args)
/**
 * 通知主程序：WebView接收HTTP认证请求，主程序可以使用HttpAuthHandler为请求设置WebView响应。默认取消请求。
 */
onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm)
/**
 * 通知主程序处理SSL客户端认证请求。如果需要提供密钥，主程序负责显示UI界面。
 * 有三个响应方法：proceed(), cancel() 和 ignore()。
 * 如果调用proceed()和cancel()，webview将会记住response，
 * 对相同的host和port地址不再调用onReceivedClientCertRequest方法。
 * 如果调用ignore()方法，webview则不会记住response。该方法在UI线程中执行，
 * 在回调期间，连接被挂起。默认cancel()，即无客户端认证
 */
onReceivedClientCertRequest(WebView view, ClientCertRequest request)
//////////////////////////////////////WebViewClient中函数概述///////////////////////////////////////////////////	


