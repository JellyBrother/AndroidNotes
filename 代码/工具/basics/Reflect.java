package com.jelly.app.main.basics;

import com.jelly.app.main.basics.data.Book;
import com.jelly.app.main.designpatterns.ITest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * java 反射
 * {@link android.app.Activity#requestDragAndDropPermissions(DragEvent event)} 注释的链接用法
 * {@link https://www.jianshu.com/p/9be58ee20dee}
 * 总结
 * 本文列举了反射机制使用过程中常用的、重要的一些类及其方法，更多信息和用法需要近一步的阅读Google提供的相关文档和示例。
 * 在阅读Class类文档时发现一个特点，以通过反射获得Method对象为例，一般会提供四种方法，getMethod(parameterTypes)、getMethods()、getDeclaredMethod(parameterTypes)和getDeclaredMethods()。getMethod(parameterTypes)用来获取某个公有的方法的对象，getMethods()获得该类所有公有的方法，getDeclaredMethod(parameterTypes)获得该类某个方法，getDeclaredMethods()获得该类所有方法。带有Declared修饰的方法可以反射到私有的方法，没有Declared修饰的只能用来反射公有的方法。其他的Annotation、Field、Constructor也是如此。
 * 在ReflectClass类中还提供了两种反射PowerManager.shutdown()的方法，在调用的时候会输出如下log，提示没有相关权限。之前在项目中尝试反射其他方法的时候还遇到过有权限和没权限返回的值不一样的情况。如果源码中明确进行了权限验证，而你的应用又无法获得这个权限的话，建议就不要浪费时间反射了。
 */
public class Reflect implements ITest {
    public class ReflectClass {

        /**
         * 创建对象
         */
        public void reflectNewInstance() {
            try {
                Class<?> book = Class.forName("com.jelly.app.main.basics.data.Book");
                Object object = book.newInstance();
                Book b = (Book) object;
                b.setName("111");
                System.out.println("reflectNewInstance:" + b.toString());
            } catch (Throwable throwable) {
                System.out.println("reflectNewInstance Exception:" + throwable.toString());
            }
        }

        /**
         * 反射私有的构造方法
         */
        public void reflectPrivateConstructor() {
            try {
                Class<?> book = Class.forName("com.jelly.app.main.basics.data.Book");
                Constructor<?> constructor = book.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                Object object = constructor.newInstance("222");
                Book b = (Book) object;
                System.out.println("reflectPrivateConstructor:" + b.toString());
            } catch (Throwable throwable) {
                System.out.println("reflectPrivateConstructor Exception:" + throwable.toString());
            }
        }

        /**
         * 反射私有属性
         */
        public void reflectPrivateField() {
            try {
                Class<?> book = Class.forName("com.jelly.app.main.basics.data.Book");
                Field tag = book.getDeclaredField("TAG");
                tag.setAccessible(true);
                Object object = book.newInstance();
                String string = (String) tag.get(object);
                System.out.println("reflectPrivateField:" + string);
            } catch (Throwable throwable) {
                System.out.println("reflectPrivateField Exception:" + throwable.toString());
            }
        }

        /**
         * 反射私有方法
         */
        public void reflectPrivateMethod() {
            try {
                Class<?> book = Class.forName("com.jelly.app.main.basics.data.Book");
                Method declaredMethod = book.getDeclaredMethod("getContent", String.class);
                declaredMethod.setAccessible(true);
                Object object = book.newInstance();
                String string = (String) declaredMethod.invoke(object, "333");
                System.out.println("reflectPrivateMethod:" + string);
            } catch (Throwable throwable) {
                System.out.println("reflectPrivateMethod Exception:" + throwable.toString());
            }
        }


//        // 获得系统Zenmode值
//        public int getZenMode() {
//            int zenMode = -1;
//            try {
//                Class<?> cServiceManager = Class.forName("android.os.ServiceManager");
//                Method mGetService = cServiceManager.getMethod("getService", String.class);
//                Object oNotificationManagerService = mGetService.invoke(null, Context.NOTIFICATION_SERVICE);
//                Class<?> cINotificationManagerStub = Class.forName("android.app.INotificationManager$Stub");
//                Method mAsInterface = cINotificationManagerStub.getMethod("asInterface",IBinder.class);
//                Object oINotificationManager = mAsInterface.invoke(null,oNotificationManagerService);
//                Method mGetZenMode = cINotificationManagerStub.getMethod("getZenMode");
//                zenMode = (int) mGetZenMode.invoke(oINotificationManager);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//            return zenMode;
//        }
//        // 关闭手机
//        public void shutDown() {
//            try {
//                Class<?> cServiceManager = Class.forName("android.os.ServiceManager");
//                Method mGetService = cServiceManager.getMethod("getService",String.class);
//                Object oPowerManagerService = mGetService.invoke(null,Context.POWER_SERVICE);
//                Class<?> cIPowerManagerStub = Class.forName("android.os.IPowerManager$Stub");
//                Method mShutdown = cIPowerManagerStub.getMethod("shutdown",boolean.class,String.class,boolean.class);
//                Method mAsInterface = cIPowerManagerStub.getMethod("asInterface",IBinder.class);
//                Object oIPowerManager = mAsInterface.invoke(null,oPowerManagerService);
//                mShutdown.invoke(oIPowerManager,true,null,true);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//        public void shutdownOrReboot(final boolean shutdown, final boolean confirm) {
//            try {
//                Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
//                // 获得ServiceManager的getService方法
//                Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
//                // 调用getService获取RemoteService
//                Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);
//                // 获得IPowerManager.Stub类
//                Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
//                // 获得asInterface方法
//                Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
//                // 调用asInterface方法获取IPowerManager对象
//                Object oIPowerManager = asInterface.invoke(null, oRemoteService);
//                if (shutdown) {
//                    // 获得shutdown()方法
//                    Method shutdownMethod = oIPowerManager.getClass().getMethod(
//                            "shutdown", boolean.class, String.class, boolean.class);
//                    // 调用shutdown()方法
//                    shutdownMethod.invoke(oIPowerManager, confirm, null, false);
//                } else {
//                    // 获得reboot()方法
//                    Method rebootMethod = oIPowerManager.getClass().getMethod("reboot",
//                            boolean.class, String.class, boolean.class);
//                    // 调用reboot()方法
//                    rebootMethod.invoke(oIPowerManager, confirm, null, false);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void test() {
        ReflectClass reflectClass = new ReflectClass();
        reflectClass.reflectNewInstance();
        reflectClass.reflectPrivateConstructor();
        reflectClass.reflectPrivateField();
        reflectClass.reflectPrivateMethod();
    }
}
