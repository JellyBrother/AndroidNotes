#########################################################################################
####### 1,Memory Analyzer (MAT) 使用
#########################################################################################

1)、强大的Java Heap分析工具，查找内存泄漏及内存占用
2)、生成整体报告、分析问题等

使用Memory Profiler 导出内存文件后，使用如下命令，将hprof文件 转换成标准格式的hprof文件，再使用MAT打开。
hprof-conv source.hprof target.hprof


### 界面图标介绍

overview ：概览信息
Histogram: 直方图；列出每个class的对象 有多少个实例，以及每个实例的内存占用情况， 
           可以在直方图中，检索某个具体的类； 基于类的角度分析问题
dominator_tree: 每个对象的支配树； 基于实例的角度来分析问题
OQL: 对象查询语言， 检索数据库， 查询具体类的信息
thread_overview: 详细的展示线程信息

Top Consumers: 以图形的形式 列出来 占用内存比较多的对象， 适用于降低内存占用的情况
Leak Suspects: 分析内存泄漏的可能的情况


Object	该类在内存当中的对象个数
Shallow Heap 对象自身所占用的内存大小，不包括它所引用的对象的内存大小
Retained Heap 该对象被垃圾回收器回收之后，会释放的内存大小

with outgoing references: 自身引用了哪些类
with incoming references: 自身被哪些类引用



#########################################################################################
######  2,TraceView 
#########################################################################################

1，图形的形式展示执行时间、调用栈等， 信息全面，包含所有线程
2，运行时开销严重，整体都会变慢，可能会带偏优化方向
3，相对于CPU Profiler, TraceView能进行更灵活的监控

使用方式：
~
    Debug.startMethodTracing("");
    ...
    Debug.stopMethodTracing();
~
生成的trace文件在sd卡： Android/data/packagename/files

界面介绍
call chart:
垂直方向： A调用B, A在上面，B在下面
橙色：系统API 调用
绿色：应用自身调用
蓝色：第三方api调用

Top Down:
函数的调用列表
total: 执行总时间
self:  自身执行所耗时间 
children:  内部调用的函数 执行耗时

Thread Time:  代码消耗CPU的时间
Wall Clock Time: 代码执行时间


#########################################################################################
######  3,SysTrace
#########################################################################################

1, 结合Android 内核的数据，生成html报告，API18以上使用，推荐TraceCompat
2, 轻量级，开销小； 直观反映cpu利用率

使用方式：
添加
~
    TraceCompact.beginSection("");
    ...
    TraceCompact.endSection();
~

使用命令导出文件
    python systrace.py -t 10 [other-options] [categories]
示例：
    python systrace.py -b 32768 -t 5 -a packageName -o outFileName.hitm sched gfx view wm am app
注：-b 设置buffer;  -t 设置时间; -a 设置包名; -o 设置输出文件名;


