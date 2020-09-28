package com.jelly.app.main.designpatterns.structural;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 装饰（Decorator）模式的定义：指在不改变现有对象结构的情况下，动态地给该对象增加一些职责（即增加其额外功能）的模式，它属于对象结构型模式。
 * 装饰（Decorator）模式的主要优点有：
 * 采用装饰模式扩展对象的功能比采用继承方式更加灵活。
 * 可以设计出多个不同的具体装饰类，创造出多个不同行为的组合。
 * 其主要缺点是：装饰模式增加了许多子类，如果过度使用会使程序变得很复杂。
 */
public class DecoratorPattern implements ITest {

    public interface ICar {
        void run();
    }

    /**
     * 等待被装饰的福特汽车（本尊，需要被装饰的对象）
     */
    public class FuTeCar implements ICar {

        @Override
        public void run() {
            System.out.println("FuTeCar run");
        }
    }

    /**
     * 对福特汽车进行抽象包装和扩展（抽象装饰）
     */
    public abstract class ChangeCar implements ICar {
        ICar iCar;

        public ChangeCar(ICar iCar) {
            this.iCar = iCar;
        }

        @Override
        public void run() {
            iCar.run();
        }

        public void music() {
            // 还可以添加功能进行扩展
        }
    }

    /**
     * 抽象装饰的实现
     */
    public class RedCar extends ChangeCar {

        public RedCar(ICar iCar) {
            super(iCar);
        }

        @Override
        public void run() {
            // 装饰前执行
            System.out.println("before FuTeCar run");
            super.run();
            // 装饰后执行
            System.out.println("affter FuTeCar run");
        }
    }

    @Override
    public void test() {
        FuTeCar fuTeCar = new FuTeCar();
        fuTeCar.run();
        RedCar redCar = new RedCar(fuTeCar);
        redCar.run();
    }
}
