package com.jelly.app.main.syncrhoized;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest {
    class ThreadA extends Thread {
        private MyService service;
        public ThreadA(MyService service) {
            super();
            this.service = service;
        }
        @Override
        public void run() {
            service.awaitA();
        }
    }
    // 线程 B
    class ThreadB extends Thread {
        private MyService service;
        public ThreadB(MyService service) {
            super();
            this.service = service;
        }
        @Override
        public void run() {
            service.awaitB();
        }
    }

    public class MyService {
        private Lock lock = new ReentrantLock();
        // 使用多个Condition实现通知部分线程
        public Condition conditionA = lock.newCondition();
        public Condition conditionB = lock.newCondition();

        public void awaitA() {
            lock.lock();
            try {
                System.out.println("begin awaitA时间为" + System.currentTimeMillis()
                        + " ThreadName=" + Thread.currentThread().getName());
                conditionA.await();
                System.out.println("  end awaitA时间为" + System.currentTimeMillis()
                        + " ThreadName=" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void awaitB() {
            lock.lock();
            try {
                System.out.println("begin awaitB时间为" + System.currentTimeMillis()
                        + " ThreadName=" + Thread.currentThread().getName());
                conditionB.await();
                System.out.println("  end awaitB时间为" + System.currentTimeMillis()
                        + " ThreadName=" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void signalAll_A() {
            try {
                lock.lock();
                System.out.println("  signalAll_A时间为" + System.currentTimeMillis()
                        + " ThreadName=" + Thread.currentThread().getName());
                conditionA.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void signalAll_B() {
            try {
                lock.lock();
                System.out.println("  signalAll_B时间为" + System.currentTimeMillis()
                        + " ThreadName=" + Thread.currentThread().getName());
                conditionB.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    // 测试
    public class Run {
        public void main(String[] args) throws InterruptedException {
            MyService service = new MyService();

            ThreadA a = new ThreadA(service);
            a.setName("A");
            a.start();

            ThreadB b = new ThreadB(service);
            b.setName("B");
            b.start();

            Thread.sleep(3000);
            service.signalAll_A();
        }
    }
}
