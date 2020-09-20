package com.jelly.app.main.designpatterns.structural;

/**
 * 桥接（Bridge）模式的定义如下：将抽象与实现分离，使它们可以独立变化。它是用组合关系代替继承关系来实现，从而降低了抽象和实现这两个可变维度的耦合度。
 * 桥接（Bridge）模式的优点是：
 * 由于抽象与实现分离，所以扩展能力强；
 * 其实现细节对客户透明。
 * 缺点是：由于聚合关系建立在抽象层，要求开发者针对抽象化进行设计与编程，这增加了系统的理解与设计难度。
 */
public class BridgePattern {

    public interface ICar {
        void run();
    }

    public class F1Car implements ICar {

        @Override
        public void run() {
            System.out.println("F1Car run");
        }
    }

    public abstract class AbstractCar {
        ICar iCar;

        public AbstractCar(ICar fiCar1Car) {
            this.iCar = iCar;
        }

        abstract void video();
    }

    public class F2Car extends AbstractCar {

        public F2Car(F1Car f1Car) {
            super(f1Car);
        }

        @Override
        public void video() {
            iCar.run();
            System.out.println("F2Car video");
        }
    }
}
