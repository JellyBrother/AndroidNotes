package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

import java.util.ArrayList;
import java.util.List;

/**
 * 迭代器（Iterator）模式的定义：提供一个对象来顺序访问聚合对象中的一系列数据，而不暴露聚合对象的内部表示。迭代器模式是一种对象行为型模式，其主要优点如下。
 * 访问一个聚合对象的内容而无须暴露它的内部表示。
 * 遍历任务交由迭代器完成，这简化了聚合类。
 * 它支持以不同方式遍历一个聚合，甚至可以自定义迭代器的子类以支持新的遍历。
 * 增加新的聚合类和迭代器类都很方便，无须修改原有代码。
 * 封装性良好，为遍历不同的聚合结构提供一个统一的接口。
 * 其主要缺点是：增加了类的个数，这在一定程度上增加了系统的复杂性。
 */
public class IteratorPattern implements ITest {
    public interface IMap {
        void add(Object object);

        void remove(Object object);

        Iterator getIterator();
    }

    public interface Iterator {
        boolean hasNext();

        Object next();

        Object first();
    }

    public class Map implements IMap {
        private List<Object> list = new ArrayList<>();

        @Override
        public void add(Object object) {
            list.add(object);
        }

        @Override
        public void remove(Object object) {
            list.remove(object);
        }

        @Override
        public Iterator getIterator() {
            return new MapIterator(list);
        }
    }

    public class MapIterator implements Iterator {
        private List<Object> list;
        private int positon = -1;

        public MapIterator(List<Object> list) {
            this.list = list;
        }

        @Override
        public boolean hasNext() {
            if (positon < list.size() - 1) {
                return true;
            }
            return false;
        }

        @Override
        public Object next() {
            Object object = null;
            if (hasNext()) {
                positon++;
                object = list.get(positon);
            }
            return object;
        }

        @Override
        public Object first() {
            return list.get(0);
        }
    }

    @Override
    public void test() {
        Map map = new Map();
        map.add("aa");
        map.add(11);
        map.add(map);
        Iterator iterator = map.getIterator();
        System.out.println("first:" + iterator.first());
        while (iterator.hasNext()) {
            Object next = iterator.next();
            System.out.println("next:" + next);
        }
    }
}
