package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 模板方法（Template Method）模式的定义如下：定义一个操作中的算法骨架，而将算法的一些步骤延迟到子类中，使得子类可以不改变该算法结构的情况下重定义该算法的某些特定步骤。它是一种类行为型模式。
 * 该模式的主要优点如下。
 * 它封装了不变部分，扩展可变部分。它把认为是不变部分的算法封装到父类中实现，而把可变部分算法由子类继承实现，便于子类继续扩展。
 * 它在父类中提取了公共的部分代码，便于代码复用。
 * 部分方法是由子类实现的，因此子类可以通过扩展方式增加相应的功能，符合开闭原则。
 * 该模式的主要缺点如下。
 * 对每个不同的实现都需要定义一个子类，这会导致类的个数增加，系统更加庞大，设计也更加抽象。
 * 父类中的抽象方法由子类实现，子类执行的结果会影响父类的结果，这导致一种反向的控制结构，它提高了代码阅读的难度。
 */
public class TemplateMethod implements ITest {

    public abstract class People {
        public void getUP() {
            System.out.println("人 起床");
            init();
            init2();
        }

        public void init() {
            System.out.println("洗脸刷牙");
        }

        abstract void init2();
    }

    public class Man extends People {

        @Override
        void init2() {
            System.out.println("男人刮胡须");
        }
    }

    public class WoMan extends People {

        @Override
        void init2() {
            System.out.println("女人化妆");
        }
    }

    @Override
    public void test() {
        People man = new Man();
        People woman = new WoMan();
        man.getUP();
        woman.getUP();
    }
}
