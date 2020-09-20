package com.jelly.app.main.designpatterns.structural;

/**
 * 代理模式的定义：由于某些原因需要给某对象提供一个代理以控制对该对象的访问。这时，访问对象不适合或者不能直接引用目标对象，代理对象作为访问对象和目标对象之间的中介。
 * 代理模式的主要优点有：
 * 代理模式在客户端与目标对象之间起到一个中介作用和保护目标对象的作用；
 * 代理对象可以扩展目标对象的功能；
 * 代理模式能将客户端与目标对象分离，在一定程度上降低了系统的耦合度，增加了程序的可扩展性
 * 其主要缺点是：
 * 代理模式会造成系统设计中类的数量增加
 * 在客户端和目标对象之间增加一个代理对象，会造成请求处理速度变慢；
 * 增加了系统的复杂度；
 */
public class ProxyPattern {

    public interface INetWork {
        void request();
    }

    public class RealRequest implements INetWork {

        @Override
        public void request() {
            System.out.println("RealRequest request");
        }
    }

    public class Proxy implements INetWork {
        RealRequest realRequest;

        @Override
        public void request() {
            if (realRequest == null) {
                realRequest = new RealRequest();
            }
            beforeRequest();
            realRequest.request();
            endRequest();
        }

        private void beforeRequest() {
            System.out.println("Proxy beforeRequest");
        }

        private void endRequest() {
            System.out.println("Proxy endRequest");
        }
    }
}
