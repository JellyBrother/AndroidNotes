package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 命令（Command）模式的定义如下：将一个请求封装为一个对象，使发出请求的责任和执行请求的责任分割开。这样两者之间通过命令对象进行沟通，这样方便将命令对象进行储存、传递、调用、增加与管理。
 * 命令模式的主要优点如下。
 * 降低系统的耦合度。命令模式能将调用操作的对象与实现该操作的对象解耦。
 * 增加或删除命令非常方便。采用命令模式增加与删除命令不会影响其他类，它满足“开闭原则”，对扩展比较灵活。
 * 可以实现宏命令。命令模式可以与组合模式结合，将多个命令装配成一个组合命令，即宏命令。
 * 方便实现 Undo 和 Redo 操作。命令模式可以与后面介绍的备忘录模式结合，实现命令的撤销与恢复。
 * 其缺点是：可能产生大量具体命令类。因为计对每一个具体操作都需要设计一个具体命令类，这将增加系统的复杂性。
 */
public class CommandPattern implements ITest {
    public class Send {
        private ICommand command;

        public Send(ICommand command) {
            this.command = command;
        }

        public void setCommand(ICommand command) {
            this.command = command;
        }

        public String execute(String command) {
            return this.command.execute(command);
        }
    }

    public interface ICommand {
        String execute(String command);
    }

    public class Command implements ICommand {
        private Receiver receiver;

        public Command() {
            receiver = new Receiver();
        }

        @Override
        public String execute(String command) {
            return receiver.execute(command);
        }
    }

    public class Receiver {
        public String execute(String command) {
            System.out.println("Receiver command:" + command);
            return "Receiver";
        }
    }

    @Override
    public void test() {
        Command command = new Command();
        Send send = new Send(command);
        String result = send.execute("send");
        System.out.println("Send result:" + result);
    }
}
