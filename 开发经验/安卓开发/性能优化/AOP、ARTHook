### ARTHook
介绍：
挂钩，将额外的代码钩住原有方法，修改执行逻辑；运行时插桩、性能分析

集成步骤

1. 项目Gradle添加
classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.0'

2. module添加
apply plugin: 'android-aspectjx'
implementation 'org.aspectj:aspectjrt:1.8.9'

3. 编写类
~
@Aspect
public class TimeCounterAop {
    private final static String TAG = TimeCounterAop.class.getSimpleName();

    @Around("execution(* com.android.mms.MmsApplication.**(..))")
    public void onMmsApplicationMethodAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        processMethod(proceedingJoinPoint);
    }
}
~



### Epic简介（https://github.com/tiann/epic）

Epic是一个虚拟机层面、以java Method为粒度的运行时Hook框架；支持Android4.0-10.0









