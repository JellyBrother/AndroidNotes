package com.jelly.app.main.designpatterns.behavioral;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 状态（State）模式的定义：对有状态的对象，把复杂的“判断逻辑”提取到不同的状态对象中，允许状态对象在其内部状态发生改变时改变其行为。
 * 状态模式是一种对象行为型模式，其主要优点如下。
 * 状态模式将与特定状态相关的行为局部化到一个状态中，并且将不同状态的行为分割开来，满足“单一职责原则”。
 * 减少对象间的相互依赖。将不同的状态引入独立的对象中会使得状态转换变得更加明确，且减少对象间的相互依赖。
 * 有利于程序的扩展。通过定义新的子类很容易地增加新的状态和转换。
 * 状态模式的主要缺点如下。
 * 状态模式的使用必然会增加系统的类与对象的个数。
 * 状态模式的结构与实现都较为复杂，如果使用不当会导致程序结构和代码的混乱。
 */
public class StatePattern implements ITest {
    public class StateManager {
        private State state;

        public StateManager() {
            state = new LowGrade(this);
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public void addGrade(int grade) {
            state.add(grade);
        }
    }

    public abstract class State {
        protected StateManager stateManager;
        protected int grade;
        protected String name;

        public void add(int grade) {
            this.grade = this.grade + grade;
            checkGrade(this.grade);
            if (stateManager == null) {
                System.out.println("grade:" + this.grade + "*stateManager* is null");
                return;
            }
            System.out.println("grade:" + this.grade + "*name*" + stateManager.getState().name);
        }

        public abstract void checkGrade(int grade);
    }

    public class LowGrade extends State {

        public LowGrade(StateManager stateManager) {
            this.stateManager = stateManager;
            name = "LowGrade";
            grade = 0;
        }

        public LowGrade(State state) {
            grade = state.grade;
            name = "LowGrade";
            stateManager = state.stateManager;
        }

        @Override
        public void checkGrade(int grade) {
            if (grade < 60) {
                return;
            }
            if (grade > 80) {
                HighGrade highGrade = new HighGrade(this);
                stateManager.setState(highGrade);
                return;
            }
            MiddleGrade middleGrade = new MiddleGrade(this);
            stateManager.setState(middleGrade);
        }
    }

    public class MiddleGrade extends State {
        public MiddleGrade(State state) {
            grade = state.grade;
            name = "MiddleGrade";
            stateManager = state.stateManager;
        }

        @Override
        public void checkGrade(int grade) {
            if (grade < 60) {
                LowGrade lowGrade = new LowGrade(this);
                stateManager.setState(lowGrade);
                return;
            }
            if (grade > 80) {
                HighGrade highGrade = new HighGrade(this);
                stateManager.setState(highGrade);
                return;
            }
        }
    }

    public class HighGrade extends State {
        public HighGrade(State state) {
            grade = state.grade;
            name = "HighGrade";
            stateManager = state.stateManager;
        }

        @Override
        public void checkGrade(int grade) {
            if (grade < 60) {
                LowGrade lowGrade = new LowGrade(this);
                stateManager.setState(lowGrade);
                return;
            }
            if (grade > 80) {
                return;
            }
            MiddleGrade middleGrade = new MiddleGrade(this);
            stateManager.setState(middleGrade);
        }
    }

    @Override
    public void test() {
        StateManager stateManager = new StateManager();
        stateManager.addGrade(50);
        stateManager.addGrade(20);
        stateManager.addGrade(20);
        stateManager.addGrade(-60);
    }
}
