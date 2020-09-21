package com.jelly.app.main.syncrhoized;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch 是一个同步类工具，不涉及锁定，当count的值为零时当前线程继续运行，不涉及同步，只涉及线程通信的时候，使用它较为合适
 * 主线程main被end.await();阻塞，两个副线程继续往下运行，因为benin已经为0，所以不阻塞，操作两次end的downcount之后，主线程继续往下执行。如果不理解可以将线程数改成3试一下
 * 注：end.countDown() 可以在多个线程中调用 计算调用次数是所有线程调用次数的总和
 */
public class CountDownLatchTest {
    public class testLatch {

        public void main(String[] args) {
            CountDownLatch begin = new CountDownLatch(1);
            CountDownLatch end = new CountDownLatch(2);

            for (int i = 0; i < 2; i++) {
                Thread thread = new Thread(new Player(begin, end), String.valueOf(i));
                thread.start();
            }

            try {
                System.out.println("the race begin");
                begin.countDown();
                end.await();//await() 方法具有阻塞作用，也就是说主线程在这里暂停
                System.out.println("the race end");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    class Player implements Runnable {

        private CountDownLatch begin;

        private CountDownLatch end;

        Player(CountDownLatch begin, CountDownLatch end) {
            this.begin = begin;
            this.end = end;
        }

        public void run() {

            try {

                System.out.println(Thread.currentThread().getName() + " start !");
                ;
                begin.await();//因为此时已经为0了，所以不阻塞
                System.out.println(Thread.currentThread().getName() + " arrived !");

                end.countDown();//countDown() 并不是直接唤醒线程,当end.getCount()为0时线程会自动唤醒

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
