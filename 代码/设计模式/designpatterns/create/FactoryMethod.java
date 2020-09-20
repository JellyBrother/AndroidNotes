package com.jelly.app.main.designpatterns.create;

/**
 * 工厂方法模式”是对简单工厂模式的进一步抽象化，其好处是可以使系统在不修改原来代码的情况下引进新的产品，即满足开闭原则。
 * 优点：
 * 用户只需要知道具体工厂的名称就可得到所要的产品，无须知道产品的具体创建过程。
 * 灵活性增强，对于新产品的创建，只需多写一个相应的工厂类。
 * 典型的解耦框架。高层模块只需要知道产品的抽象类，无须关心其他实现类，满足迪米特法则、依赖倒置原则和里氏替换原则。
 * 缺点：
 * 类的个数容易过多，增加复杂度
 * 增加了系统的抽象性和理解难度
 * 抽象产品只能生产一种产品，此弊端可使用抽象工厂模式解决。
 */
public class FactoryMethod {

    public interface IProduct {
        void show();
    }

    public class Product1 implements IProduct {
        private String name;

        public Product1(String name) {
            this.name = name;
        }

        @Override
        public void show() {
            System.out.println(name + " Product1 show");
        }
    }

    public class Product2 implements IProduct {
        private String name;

        public Product2(String name) {
            this.name = name;
        }

        @Override
        public void show() {
            System.out.println(name + " Product2 show");
        }
    }

    public interface IFactory {
        IProduct newProduct(int type);
    }

    public class Factory1 implements IFactory {
        public static final int FACTORY_TYPE1 = 1;
        public static final int FACTORY_TYPE2 = 2;

        @Override
        public IProduct newProduct(int type) {
            IProduct product = null;
            switch (type) {
                case FACTORY_TYPE1:
                    product = new Product1("Factory1");
                    break;
                case FACTORY_TYPE2:
                    product = new Product2("Factory1");
                    break;
            }
            return product;
        }
    }

    public class Factory2 implements IFactory {
        public static final int FACTORY_TYPE1 = 1;
        public static final int FACTORY_TYPE2 = 2;

        @Override
        public IProduct newProduct(int type) {
            IProduct product = null;
            switch (type) {
                case FACTORY_TYPE1:
                    product = new Product1("Factory2");
                    break;
                case FACTORY_TYPE2:
                    product = new Product2("Factory2");
                    break;
            }
            return product;
        }
    }
}
