package com.jelly.app.main.designpatterns.structural;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 适配器模式（Adapter）的定义如下：将一个类的接口转换成客户希望的另外一个接口，使得原本由于接口不兼容而不能一起工作的那些类能一起工作。适配器模式分为类结构型模式和对象结构型模式两种，前者类之间的耦合度比后者高，且要求程序员了解现有组件库中的相关组件的内部结构，所以应用相对较少些。
 * 该模式的主要优点如下。
 * 客户端通过适配器可以透明地调用目标接口。
 * 复用了现存的类，程序员不需要修改原有代码而重用现有的适配者类。
 * 将目标类和适配者类解耦，解决了目标类和适配者类接口不一致的问题。
 * 其缺点是：对类适配器来说，更换适配器的实现过程比较复杂。
 */
public class AdapterPattern implements ITest {

    public interface ICar {
        void run();
    }

    public class FuTeCar {

        public void fuTeRun() {
            System.out.println("FuTeCar fuTeRun");
        }
    }

    public class BenTianCar {

        public void benTianRun() {
            System.out.println("BenTianCar benTianRun");
        }
    }

    public class FuTeAdapter implements ICar {
        FuTeCar fuTeCar;

        public FuTeAdapter() {
            fuTeCar = new FuTeCar();
        }

        @Override
        public void run() {
            fuTeCar.fuTeRun();
        }
    }

    @Override
    public void test() {
        FuTeAdapter fuTeAdapter = new FuTeAdapter();
        fuTeAdapter.run();
    }
}
