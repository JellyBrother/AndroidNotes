package com.jelly.app.main.designpatterns.structural;

import com.jelly.app.main.designpatterns.ITest;

import java.util.ArrayList;

/**
 * 组合（Composite）模式的定义：有时又叫作部分-整体模式，它是一种将对象组合成树状的层次结构的模式，用来表示“部分-整体”的关系，使用户对单个对象和组合对象具有一致的访问性。
 * 组合模式的主要优点有：
 * 组合模式使得客户端代码可以一致地处理单个对象和组合对象，无须关心自己处理的是单个对象，还是组合对象，这简化了客户端代码；
 * 更容易在组合体内加入新的对象，客户端不会因为加入了新的对象而更改源代码，满足“开闭原则”；
 * 其主要缺点是：
 * 设计较复杂，客户端需要花更多时间理清类之间的层次关系；
 * 不容易限制容器中的构件；
 * 不容易用继承的方法来增加构件的新功能；
 */
public class CompositePattern implements ITest {

    public interface Component {
        void add(Component component);

        Component get(int position);

        void remove(Component component);

        void show();
    }

    public class Tree implements Component {
        private ArrayList<Component> list = new ArrayList<>();
        private String name;

        public Tree(String name) {
            this.name = name;
        }

        @Override
        public void add(Component component) {
            list.add(component);
        }

        @Override
        public Component get(int position) {
            return list.get(position);
        }

        @Override
        public void remove(Component component) {
            list.remove(component);
        }

        @Override
        public void show() {
            for (Component component : list) {
                component.show();
            }
        }
    }

    public class Branch implements Component {
        private String name;

        public Branch(String name) {
            this.name = name;
        }

        @Override
        public void add(Component component) {

        }

        @Override
        public Component get(int position) {
            return null;
        }

        @Override
        public void remove(Component component) {

        }

        @Override
        public void show() {
            System.out.println(name + " show");
        }
    }

    @Override
    public void test() {
        Branch branch1 = new Branch("branch1");
        Branch branch2 = new Branch("branch2");
        Tree tree = new Tree("tree");
        tree.add(branch1);
        tree.add(branch2);
        tree.show();
    }
}
