package com.jelly.app.main.designpatterns.create;

/**
 * 单例（Singleton）模式的定义：指一个类只有一个实例，且该类能自行创建这个实例的一种模式。例如，Windows 中只能打开一个任务管理器，这样可以避免因打开多个任务管理器窗口而造成内存资源的浪费，或出现各个窗口显示内容的不一致等错误。
 * 单例模式的优点：
 * 单例模式可以保证内存里只有一个实例，减少了内存的开销。
 * 可以避免对资源的多重占用。
 * 单例模式设置全局访问点，可以优化和共享资源的访问。
 * 单例模式的缺点：
 * 单例模式一般没有接口，扩展困难。如果要扩展，则除了修改原来的代码，没有第二种途径，违背开闭原则。
 * 在并发测试中，单例模式不利于代码调试。在调试过程中，如果单例中的代码没有执行完，也不能模拟生成一个新的对象。
 * 单例模式的功能代码通常写在一个类中，如果功能设计不合理，则很容易违背单一职责原则。
 */
public class Singleton {

    /**
     * 懒汉式
     */
    public static class LazySingleton {
        private static volatile LazySingleton instance = null;    //保证 instance 在所有线程中同步

        private LazySingleton() {
        }    //private 避免类在外部被实例化

        public static synchronized LazySingleton getInstance() {
            //getInstance 方法前加同步
            if (instance == null) {
                synchronized (LazySingleton.class) {
                    if (instance == null) {
                        instance = new LazySingleton();
                    }
                }
            }
            return instance;
        }
    }

    /**
     * 饿汉式
     */
    public static class HungrySingleton {
        private static final HungrySingleton instance = new HungrySingleton();

        private HungrySingleton() {
        }

        public static HungrySingleton getInstance() {
            return instance;
        }
    }

    /**
     * 饿汉式
     */
    public static class HungrySingleton2 {
        private HungrySingleton2() {
        }

        public static HungrySingleton2 getInstance() {
            return SingletonInstanceHolder.INSTANCE;
        }

        private static class SingletonInstanceHolder {
            private SingletonInstanceHolder() {
            }

            public static final HungrySingleton2 INSTANCE = new HungrySingleton2();
        }
    }
}
