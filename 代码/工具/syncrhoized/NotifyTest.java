package com.jelly.app.main.syncrhoized;

import java.util.ArrayList;
import java.util.List;

public class NotifyTest {
    static class ValueObject {
        public static List<String> list = new ArrayList<String>();
    }

    //元素添加线程
    class ThreadAdd extends Thread {

        private String lock;

        public ThreadAdd(String lock, String name) {
            super(name);
            this.lock = lock;
        }

        @Override
        public void run() {
            synchronized (lock) {
                ValueObject.list.add("anyString");
                lock.notifyAll();               // 唤醒所有 wait 线程
            }
        }
    }

    //元素删除线程
    class ThreadSubtract extends Thread {

        private String lock;

        public ThreadSubtract(String lock, String name) {
            super(name);
            this.lock = lock;
        }

        @Override
        public void run() {
            try {
                synchronized (lock) {
                    while (ValueObject.list.size() == 0) {
                        System.out.println("wait begin ThreadName=" + Thread.currentThread().getName());
                        lock.wait();
                        System.out.println("wait   end ThreadName=" + Thread.currentThread().getName());
                    }
                    ValueObject.list.remove(0);
                    System.out.println("list size=" + ValueObject.list.size());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //测试类
    public class Run {
        public void main(String[] args) throws InterruptedException {

            //锁对象
            String lock = new String("");

            ThreadSubtract subtract1Thread = new ThreadSubtract(lock, "subtract1Thread");
            subtract1Thread.start();

            ThreadSubtract subtract2Thread = new ThreadSubtract(lock, "subtract2Thread");
            subtract2Thread.start();

            Thread.sleep(1000);

            ThreadAdd addThread = new ThreadAdd(lock, "addThread");
            addThread.start();

        }
    }
}
