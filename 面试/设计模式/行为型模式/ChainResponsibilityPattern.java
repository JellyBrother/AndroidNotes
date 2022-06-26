package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 责任链（Chain of Responsibility）模式的定义：为了避免请求发送者与多个请求处理者耦合在一起，将所有请求的处理者通过前一对象记住其下一个对象的引用而连成一条链；当有请求发生时，可将请求沿着这条链传递，直到有对象处理它为止。
 * 注意：责任链模式也叫职责链模式。
 * 在责任链模式中，客户只需要将请求发送到责任链上即可，无须关心请求的处理细节和请求的传递过程，所以责任链将请求的发送者和请求的处理者解耦了。
 * 责任链模式是一种对象行为型模式，其主要优点如下。
 * 降低了对象之间的耦合度。该模式使得一个对象无须知道到底是哪一个对象处理其请求以及链的结构，发送者和接收者也无须拥有对方的明确信息。
 * 增强了系统的可扩展性。可以根据需要增加新的请求处理类，满足开闭原则。
 * 增强了给对象指派职责的灵活性。当工作流程发生变化，可以动态地改变链内的成员或者调动它们的次序，也可动态地新增或者删除责任。
 * 责任链简化了对象之间的连接。每个对象只需保持一个指向其后继者的引用，不需保持其他所有处理者的引用，这避免了使用众多的 if 或者 if···else 语句。
 * 责任分担。每个类只需要处理自己该处理的工作，不该处理的传递给下一个对象完成，明确各类的责任范围，符合类的单一职责原则。
 * 其主要缺点如下。
 * 不能保证每个请求一定被处理。由于一个请求没有明确的接收者，所以不能保证它一定会被处理，该请求可能一直传到链的末端都得不到处理。
 * 对比较长的职责链，请求的处理可能涉及多个处理对象，系统性能将受到一定影响。
 * 职责链建立的合理性要靠客户端来保证，增加了客户端的复杂性，可能会由于职责链的错误设置而导致系统出错，如可能会造成循环调用。
 */
public class ChainResponsibilityPattern implements ITest {
    public abstract class Handler {
        private Handler handler;

        public Handler getHandler() {
            return handler;
        }

        public void setHandler(Handler handler) {
            this.handler = handler;
        }

        abstract void handMessage(String message);
    }

    public class Handler1 extends Handler {

        @Override
        void handMessage(String message) {
            if (message.equalsIgnoreCase("1")) {
                System.out.println("Handler1 handMessage");
                return;
            }
            if (message.equalsIgnoreCase("2")) {
                Handler handler2 = getHandler();
                if (handler2 == null) {
                    System.out.println("Handler1 return handMessage:2");
                    return;
                }
                handler2.handMessage(message);
                return;
            }
        }
    }

    public class Handler2 extends Handler {

        @Override
        void handMessage(String message) {
            if (message.equalsIgnoreCase("1")) {
                System.out.println("Handler2 return handMessage:1");
                return;
            }
            if (message.equalsIgnoreCase("2")) {
                System.out.println("Handler2 handMessage");
                return;
            }
            if (message.equalsIgnoreCase("3")) {
                Handler handler3 = getHandler();
                if (handler3 == null) {
                    System.out.println("Handler2 return handMessage:3");
                    return;
                }
                handler3.handMessage(message);
                return;
            }
        }
    }

    @Override
    public void test() {
        Handler1 handler1 = new Handler1();
        Handler2 handler2 = new Handler2();
        handler1.setHandler(handler2);
        handler1.handMessage("1");
        handler1.handMessage("2");
    }
}
