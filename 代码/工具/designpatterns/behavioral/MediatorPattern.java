package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

import java.util.ArrayList;
import java.util.List;

/**
 * 中介者（Mediator）模式的定义：定义一个中介对象来封装一系列对象之间的交互，使原有对象之间的耦合松散，且可以独立地改变它们之间的交互。中介者模式又叫调停模式，它是迪米特法则的典型应用。
 * 中介者模式是一种对象行为型模式，其主要优点如下。
 * 降低了对象之间的耦合性，使得对象易于独立地被复用。
 * 将对象间的一对多关联转变为一对一的关联，提高系统的灵活性，使得系统易于维护和扩展。
 * 其主要缺点是：当同事类太多时，中介者的职责将很大，它会变得复杂而庞大，以至于系统难以维护。
 */
public class MediatorPattern implements ITest {
    public abstract class Message {
        IChat chat;

        public void register(IChat chat) {
            this.chat = chat;
        }

        abstract void send(String message);

        abstract void receive(String message);
    }

    public class Message1 extends Message {

        @Override
        void send(String message) {
            System.out.println("Message1 send:" + message);
            chat.forward(this, message);
        }

        @Override
        void receive(String message) {
            System.out.println("Message1 receive:" + message);
        }
    }

    public class Message2 extends Message {

        @Override
        void send(String message) {
            System.out.println("Message2 send:" + message);
            chat.forward(this, message);
        }

        @Override
        void receive(String message) {
            System.out.println("Message2 receive:" + message);
        }
    }

    public interface IChat {
        void addMessage(Message message);

        void forward(Message message, String msg);
    }

    public class WeiXin implements IChat {
        List<Message> list = new ArrayList<>();

        @Override
        public void addMessage(Message message) {
            list.add(message);
            message.register(this);
        }

        @Override
        public void forward(Message message, String msg) {
            for (Message me : list) {
                if (!me.equals(message)) {
                    me.receive(msg);
                }
            }
        }
    }

    @Override
    public void test() {
        Message1 message1 = new Message1();
        Message2 message2 = new Message2();
        WeiXin weiXin = new WeiXin();
        weiXin.addMessage(message1);
        weiXin.addMessage(message2);
        message1.send("1111");
        message2.send("2222");
    }
}
