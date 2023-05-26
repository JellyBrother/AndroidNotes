### OkHttp的调用
//[1]、创建OkhttpClient对象
OkHttpClient client = new OkHttpClient.Builder().readTimeout(5000, TimeUnit.MILLISECONDS).build();
//[2]、创建请求体Request，包含常用的请求信息，如url、get/post方法，设置请求头等
Request request = new Request.Builder().url("http://www.baidu.com").get().build();
//[3]、创建Call对象
Call call = client.newCall(request);
//[4]、同步请求，发送请求后，就会进入阻塞状态，直到收到响应
Response response = call.execute();
// 或者发起异步请求
call.enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
    }
    @Override
    public void onResponse(Call call, Response response) throws IOException {
    }
}); 

### 调用流程
构建OkhttpClient--构建Request--OkhttpClient.newCall创建Call对象--调用execute或者enqueue发起请求
--Dispatcher分发请求--Interceptor拦截器--获取响应体Response--调用client.dispatcher.finished(this)完成调用

### Dispatcher分发器
* 有execute、enqueue、cancel方法。
* 内部维护了三个队列：同步队列、异步队列、异步等待队列。
* 线程池管理异步任务。
* 当异步队列小于最大并发数64 并且 正在执行请求的请求数小于5 时，会把请求直接添加啦异步队列中，否则添加到异步等待队列。
* 线程池配置：
核心线程池的数量：0
最大线程数量：Integer.MAX_VALUE
空闲线程的闲置时间为60s
线程等待队列：new SynchronousQueue() 没有容量的队列
线程创建工厂：Util.threadFactory(“OkHttp Dispatcher”, false))
* SynchronousQueue：没有容量的队列
使用此队列意味着希望获得最大并发量。因为无论如何，向线程池提交任务，往队列提交任务都会失败，而失败后如果没有空闲的非核心线程，就会检查如果当前线程池中的线程数未达到最大线程，则会新建线程执行新提交的任务。完全没有任何等待，唯一制约它的就是最大线程池的个数。因此一般配合Integer.MAX_VALUE 就实现了真正的无等待。

### Interceptors拦截器
* 拦截器是一个集合，先添加自定义的拦截器，再添加okhttp的默认拦截器。见RealCall的getResponseWithInterceptorChain方法
List<Interceptor> interceptors = new ArrayList<>();
// 添加自定义拦截器
interceptors.addAll(client.interceptors());
// 重试重定向拦截器：负责请求失败的时候实现重试重定向功能
interceptors.add(retryAndFollowUpInterceptor);
// 桥接拦截器：将用户构造的请求转换为向服务器发送的请求，将服务器返回的响应转换为对用户友好的响应
// 主要对 Request 中的 Head 设置默认值，比如 Content-Type、Keep-Alive、Cookie、Gzip 等
interceptors.add(new BridgeInterceptor(client.cookieJar()));
// 缓存拦截器：读取缓存、更新缓存
interceptors.add(new CacheInterceptor(client.internalCache()));
// 连接拦截器：负责建立与服务器地址之间的连接，也就是 TCP 链接。
interceptors.add(new ConnectInterceptor(client));
* RealInterceptorChain的proceed方法循环遍历interceptors，返回Response，先遍历自定义的拦截器。
* RealCall的run方法请求成功回调onResponse，请求异常回调onFailure，最后在finally中关闭调用client.dispatcher.finished(this)

### 自定义拦截器
class TokenInvalidInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val response: Response = chain.proceed(request)
        return response
    }
}

### BridgeInterceptor拦截器
https://blog.51cto.com/u_13794952/5558186
客户端发起请求时在请求头里增加 Accept-Encoding: gzip，服务端响应时在返回的头信息里增加 Content-Encoding: gzip，这表示传输的数据是采用 gzip 压缩的。

### CacheInterceptor拦截器
https://blog.51cto.com/u_15456329/4801700












