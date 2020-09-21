package com.jelly.app.main.syncrhoized;

import java.util.ArrayList;
import java.util.List;

public class NotifyTest2 {
    //资源类
    class MyStack {
        // 共享队列
        private List list = new ArrayList();

        // 生产
        @SuppressWarnings("unchecked")
        public synchronized void push() {
            try {
                while (list.size() == 1) {    // 多个生产者
                    System.out.println("队列已满，线程 "
                            + Thread.currentThread().getName() + " 呈wait状态...");
                    this.wait();
                }
                list.add("anyString=" + Math.random());
                System.out.println("线程 " + Thread.currentThread().getName()
                        + " 生产了，队列已满...");
                this.notifyAll();                   // 防止生产者仅通知生产者
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 消费
        public synchronized String pop() {
            String returnValue = "";
            try {
                while (list.size() == 0) {              // 多个消费者
                    System.out.println("队列已空，线程 "
                            + Thread.currentThread().getName() + " 呈wait状态...");
                    this.wait();
                }
                returnValue = "" + list.get(0);
                list.remove(0);
                System.out.println("线程 " + Thread.currentThread().getName()
                        + " 消费了，队列已空...");
                this.notifyAll();                   // 防止消费者仅通知消费者
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return returnValue;
        }
    }

    //生产者
    class P_Thread extends Thread {

        private MyStack myStack;

        public P_Thread(MyStack myStack,String name) {
            super(name);
            this.myStack = myStack;
        }

        public void pushService() {
            myStack.push();
        }

        @Override
        public void run() {
            while (true) {
                myStack.push();
            }
        }
    }

    //消费者
    class C_Thread extends Thread {

        private MyStack myStack;

        public C_Thread(MyStack myStack,String name) {
            super(name);
            this.myStack = myStack;
        }

        @Override
        public void run() {
            while (true) {
                myStack.pop();
            }
        }
    }

    //测试类
    public class Run {
        public void main(String[] args) throws InterruptedException {
            MyStack myStack = new MyStack();

            P_Thread pThread1 = new P_Thread(myStack, "P1");
            P_Thread pThread2 = new P_Thread(myStack, "P2");
            P_Thread pThread3 = new P_Thread(myStack, "P3");
            P_Thread pThread4 = new P_Thread(myStack, "P4");
            P_Thread pThread5 = new P_Thread(myStack, "P5");
            P_Thread pThread6 = new P_Thread(myStack, "P6");
            pThread1.start();
            pThread2.start();
            pThread3.start();
            pThread4.start();
            pThread5.start();
            pThread6.start();

            C_Thread cThread1 = new C_Thread(myStack, "C1");
            C_Thread cThread2 = new C_Thread(myStack, "C2");
            C_Thread cThread3 = new C_Thread(myStack, "C3");
            C_Thread cThread4 = new C_Thread(myStack, "C4");
            C_Thread cThread5 = new C_Thread(myStack, "C5");
            C_Thread cThread6 = new C_Thread(myStack, "C6");
            C_Thread cThread7 = new C_Thread(myStack, "C7");
            C_Thread cThread8 = new C_Thread(myStack, "C8");
            cThread1.start();
            cThread2.start();
            cThread3.start();
            cThread4.start();
            cThread5.start();
            cThread6.start();
            cThread7.start();
            cThread8.start();
        }
    }
/* Output:
        线程 P1 生产了，队列已满...
        队列已满，线程 P1 呈wait状态...
        线程 C5 消费了，队列已空...
        队列已空，线程 C5 呈wait状态...
        队列已空，线程 C8 呈wait状态...
        队列已空，线程 C2 呈wait状态...
        队列已空，线程 C7 呈wait状态...
        队列已空，线程 C4 呈wait状态...
        队列已空，线程 C6 呈wait状态...
        队列已空，线程 C3 呈wait状态...
        队列已空，线程 C1 呈wait状态...
        线程 P6 生产了，队列已满...
        队列已满，线程 P6 呈wait状态...
        队列已满，线程 P5 呈wait状态...
        队列已满，线程 P4 呈wait状态...
        ...
 */
}
