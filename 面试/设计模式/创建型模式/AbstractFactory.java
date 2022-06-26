package com.jelly.app.main.designpatterns.create;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 抽象工厂（AbstractFactory）模式的定义：是一种为访问类提供一个创建一组相关或相互依赖对象的接口，且访问类无须指定所要产品的具体类就能得到同族的不同等级的产品的模式结构。
 * 抽象工厂模式除了具有工厂方法模式的优点外，其他主要优点如下。
 * 可以在类的内部对产品族中相关联的多等级产品共同管理，而不必专门引入多个新的类来进行管理。
 * 当需要产品族时，抽象工厂可以保证客户端始终只使用同一个产品的产品组。
 * 抽象工厂增强了程序的可扩展性，当增加一个新的产品族时，不需要修改原代码，满足开闭原则。
 * 其缺点是：当产品族中需要增加一个新的产品时，所有的工厂类都需要进行修改。增加了系统的抽象性和理解难度。
 */
public class AbstractFactory implements ITest {

    public interface IClothe {
        void show();
    }

    public class Clothe1 implements IClothe {
        private String name;

        public Clothe1(String name) {
            this.name = name;
        }

        @Override
        public void show() {
            System.out.println(name + " Clothe1 show");
        }
    }

    public class Clothe2 implements IClothe {
        private String name;

        public Clothe2(String name) {
            this.name = name;
        }

        @Override
        public void show() {
            System.out.println(name + " Clothe2 show");
        }
    }

    public interface IToy {
        void show();
    }

    public class Toy1 implements IToy {
        private String name;

        public Toy1(String name) {
            this.name = name;
        }

        @Override
        public void show() {
            System.out.println(name + " Toy1 show");
        }
    }

    public class Toy2 implements IToy {
        private String name;

        public Toy2(String name) {
            this.name = name;
        }

        @Override
        public void show() {
            System.out.println(name + " Toy2 show");
        }
    }

    public interface IFactory {
        IClothe newClothe(int type);

        IToy newToy(int type);
    }

    public class Factory1 implements IFactory {
        public static final int FACTORY_TYPE1 = 1;
        public static final int FACTORY_TYPE2 = 2;

        @Override
        public IClothe newClothe(int type) {
            IClothe clothe = null;
            switch (type) {
                case FACTORY_TYPE1:
                    clothe = new Clothe1("Factory1");
                    break;
                case FACTORY_TYPE2:
                    clothe = new Clothe2("Factory1");
                    break;
            }
            return clothe;
        }

        @Override
        public IToy newToy(int type) {
            IToy toy = null;
            switch (type) {
                case FACTORY_TYPE1:
                    toy = new Toy1("Factory1");
                    break;
                case FACTORY_TYPE2:
                    toy = new Toy2("Factory1");
                    break;
            }
            return toy;
        }
    }

    @Override
    public void test() {
        IFactory factory1 = new Factory1();
        IClothe clothe1 = factory1.newClothe(Factory1.FACTORY_TYPE1);
        IToy toy2 = factory1.newToy(Factory1.FACTORY_TYPE2);
        clothe1.show();
        toy2.show();
    }
}
