package com.jelly.app.main.designpatterns.structural;

import com.jelly.app.main.designpatterns.ITest;

import java.util.HashMap;

/**
 * 享元（Flyweight）模式的定义：运用共享技术来有效地支持大量细粒度对象的复用。它通过共享已经存在的对象来大幅度减少需要创建的对象数量、避免大量相似类的开销，从而提高系统资源的利用率。
 * 主要优点是：
 * 相同对象只要保存一份，这降低了系统中对象的数量，从而降低了系统中细粒度对象给内存带来的压力。
 * 其主要缺点是：
 * 为了使对象可以共享，需要将一些不能共享的状态外部化，这将增加程序的复杂性。
 * 读取享元模式的外部状态会使得运行时间稍微变长。
 */
public class FlyweightPattern implements ITest {

    public class NoShare {

        private String info;

        public NoShare(String info) {
            this.info = info;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    public interface IShare {
        void share(NoShare noShare);
    }

    public class Share implements IShare {
        private String key;

        public Share(String key) {
            this.key = key;
        }

        @Override
        public void share(NoShare noShare) {
            System.out.println("NoShare:" + noShare.getInfo() + "*key*" + key);
        }
    }

    public class Factory {
        private HashMap<String, Share> hashMap = new HashMap();

        public Share getShare(String key) {
            Share share = hashMap.get(key);
            if (share == null) {
                share = new Share(key);
                hashMap.put(key, share);
                System.out.println("Factory getShare key:" + key);
            }
            return share;
        }
    }

    @Override
    public void test() {
        Factory factory = new Factory();
        // 这些是分享的，创建一次后，后面直接使用
        Share a1 = factory.getShare("a");
        Share a2 = factory.getShare("a");
        Share b1 = factory.getShare("b");
        Share b2 = factory.getShare("b");
        // 这些是不共享的
        a1.share(new NoShare("a1"));
        a2.share(new NoShare("a2"));
        b1.share(new NoShare("b1"));
        b2.share(new NoShare("b2"));
    }
}
