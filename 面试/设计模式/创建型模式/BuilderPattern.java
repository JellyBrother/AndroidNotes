package com.jelly.app.main.designpatterns.create;

import com.jelly.app.main.designpatterns.ITest;

/**
 * 建造者（Builder）模式的定义：指将一个复杂对象的构造与它的表示分离，使同样的构建过程可以创建不同的表示，这样的设计模式被称为建造者模式。它是将一个复杂的对象分解为多个简单的对象，然后一步一步构建而成。它将变与不变相分离，即产品的组成部分是不变的，但每一部分是可以灵活选择的。
 * 优点如下：
 * 封装性好，构建和表示分离。
 * 扩展性好，各个具体的建造者相互独立，有利于系统的解耦。
 * 客户端不必知道产品内部组成的细节，建造者可以对创建过程逐步细化，而不对其它模块产生任何影响，便于控制细节风险。
 * 其缺点如下：
 * 产品的组成部分必须相同，这限制了其使用范围。
 * 如果产品的内部变化复杂，如果产品内部发生变化，则建造者也要同步修改，后期维护成本较大。
 * 这种建造者模式，可以复用已有对象的参数，任意设置参数。
 */
public class BuilderPattern implements ITest {

    public static final class Product {
        private String partA;
        private String partB;
        private String partC;

        Product(Builder builder) {
            partA = builder.partA;
            partB = builder.partB;
            partC = builder.partC;
        }

        public String getPartA() {
            return partA;
        }

        public String getPartB() {
            return partB;
        }

        public String getPartC() {
            return partC;
        }

        public void show() {
            System.out.println("Product partA:" + partA + ", partB:" + partB + ", partC:" + partC);
        }

        public Builder newBuilder() {
            return new Builder(this);
        }

        public static class Builder {
            private String partA;
            private String partB;
            private String partC;

            public Builder() {
            }

            public Builder(Product product) {
                partA = product.partA;
                partB = product.partB;
                partC = product.partC;
            }

            public Builder partA(String partA) {
                if (partA == null) {
                    throw new NullPointerException("url == null");
                }
                this.partA = partA;
                return this;
            }

            public Builder partB(String partB) {
                if (partB == null) {
                    throw new NullPointerException("url == null");
                }
                this.partB = partB;
                return this;
            }

            public Builder partC(String partC) {
                if (partC == null) {
                    throw new NullPointerException("url == null");
                }
                this.partC = partC;
                return this;
            }

            public Product builder() {
                return new Product(this);
            }
        }
    }

    @Override
    public void test() {
        Product product = new Product.Builder()
                .partA("aa").partB("bb").partC("cc")
                .builder();
        product.show();
        Product product2 = product.newBuilder()
                .partA("aaaa").partB("bbbb")
                .builder();
        product2.show();
    }
}
