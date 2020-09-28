package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

import java.util.ArrayList;
import java.util.List;

/**
 * 观察者（Observer）模式的定义：指多个对象间存在一对多的依赖关系，当一个对象的状态发生改变时，所有依赖于它的对象都得到通知并被自动更新。这种模式有时又称作发布-订阅模式、模型-视图模式，它是对象行为型模式。
 * 观察者模式是一种对象行为型模式，其主要优点如下。
 * 降低了目标与观察者之间的耦合关系，两者之间是抽象耦合关系。
 * 目标与观察者之间建立了一套触发机制。
 * 它的主要缺点如下。
 * 目标与观察者之间的依赖关系并没有完全解除，而且有可能出现循环引用。
 * 当观察者对象很多时，通知的发布会花费很多时间，影响程序的效率。
 */
public class ObserverPattern implements ITest {
    public interface IObserver {
        void onChange();
    }

    public class Observer1 implements IObserver {
        @Override
        public void onChange() {
            System.out.println("Observer1 onChange");
        }
    }

    public class Observer2 implements IObserver {
        @Override
        public void onChange() {
            System.out.println("Observer2 onChange");
        }
    }

    public abstract class Subject {
        List<IObserver> list = new ArrayList<>();

        public void addObserver(IObserver observer) {
            list.add(observer);
        }

        public void remove(IObserver observer) {
            list.remove(observer);
        }

        public abstract void notifyObserver();
    }

    public class Subject1 extends Subject {
        @Override
        public void notifyObserver() {
            for (IObserver observer : list) {
                observer.onChange();
            }
        }
    }

    @Override
    public void test() {
        Subject subject = new Subject1();
        Observer1 observer1 = new Observer1();
        Observer2 observer2 = new Observer2();
        subject.addObserver(observer1);
        subject.addObserver(observer2);
        subject.notifyObserver();
    }
}
