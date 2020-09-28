package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 访问者（Visitor）模式是一种对象行为型模式，其主要优点如下。
 * 扩展性好。能够在不修改对象结构中的元素的情况下，为对象结构中的元素添加新的功能。
 * 复用性好。可以通过访问者来定义整个对象结构通用的功能，从而提高系统的复用程度。
 * 灵活性好。访问者模式将数据结构与作用于结构上的操作解耦，使得操作集合可相对自由地演化而不影响系统的数据结构。
 * 符合单一职责原则。访问者模式把相关的行为封装在一起，构成一个访问者，使每一个访问者的功能都比较单一。
 * 访问者（Visitor）模式的主要缺点如下。
 * 增加新的元素类很困难。在访问者模式中，每增加一个新的元素类，都要在每一个具体访问者类中增加相应的具体操作，这违背了“开闭原则”。
 * 破坏封装。访问者模式中具体元素对访问者公布细节，这破坏了对象的封装性。
 * 违反了依赖倒置原则。访问者模式依赖了具体类，而没有依赖抽象类。
 */
public class VisitorPattern implements ITest {
    public interface IVisitor {
        void visit(Element1 element1);

        void visit(Element2 element2);
    }

    public class Visitor1 implements IVisitor {

        @Override
        public void visit(Element1 element1) {
            System.out.println("Visitor1 visit element1:::" + element1.work());
        }

        @Override
        public void visit(Element2 element2) {
            System.out.println("Visitor1 visit element2:::" + element2.work());
        }
    }

    public class Visitor2 implements IVisitor {

        @Override
        public void visit(Element1 element1) {
            System.out.println("Visitor2 visit element1:::" + element1.work());
        }

        @Override
        public void visit(Element2 element2) {
            System.out.println("Visitor2 visit element2:::" + element2.work());
        }
    }

    public interface IElement {
        void accept(IVisitor visitor);
    }

    public class Element1 implements IElement {

        @Override
        public void accept(IVisitor visitor) {
            visitor.visit(this);
        }

        public String work() {
            return "Element1 work";
        }
    }

    public class Element2 implements IElement {

        @Override
        public void accept(IVisitor visitor) {
            visitor.visit(this);
        }

        public String work() {
            return "Element2 work";
        }
    }


    public class VisitorManager {
        private List<IElement> list = new ArrayList<>();

        public void accept(IVisitor visitor) {
            Iterator<IElement> iterator = list.iterator();
            while (iterator.hasNext()) {
                IElement next = iterator.next();
                next.accept(visitor);
            }
        }

        public void add(IElement element) {
            list.add(element);
        }

        public void remove(IElement element) {
            list.remove(element);
        }
    }

    @Override
    public void test() {
        VisitorManager manager = new VisitorManager();
        IVisitor visitor1 = new Visitor1();
        IVisitor visitor2 = new Visitor2();
        IElement element1 = new Element1();
        IElement element2 = new Element2();
        manager.add(element1);
        manager.add(element2);
        manager.accept(visitor1);
        manager.accept(visitor2);
    }
}
