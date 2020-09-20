package com.jelly.app;

import com.jelly.app.main.designpatterns.create.AbstractFactory;
import com.jelly.app.main.designpatterns.create.BuilderPattern;
import com.jelly.app.main.designpatterns.create.BuilderPattern2;
import com.jelly.app.main.designpatterns.create.FactoryMethod;
import com.jelly.app.main.designpatterns.create.SimpleFactory;
import com.jelly.app.main.designpatterns.structural.AdapterPattern;
import com.jelly.app.main.designpatterns.structural.BridgePattern;
import com.jelly.app.main.designpatterns.structural.DecoratorPattern;
import com.jelly.app.main.designpatterns.structural.FacadePattern;
import com.jelly.app.main.designpatterns.structural.ProxyPattern;

import org.junit.Test;

public class DesignPatternsTest {
    @Test
    public void testSimpleFactory() throws Exception {
        SimpleFactory simpleFactory = new SimpleFactory();
        SimpleFactory.Factory factory = simpleFactory.new Factory();
        SimpleFactory.IProduct product = factory.newProduct(SimpleFactory.Factory.FACTORY_TYPE1);
        product.show();
    }

    @Test
    public void testFactoryMethod() throws Exception {
        FactoryMethod factoryMethod = new FactoryMethod();
        FactoryMethod.IFactory factory1 = factoryMethod.new Factory1();
        FactoryMethod.IProduct product = factory1.newProduct(FactoryMethod.Factory1.FACTORY_TYPE1);
        product.show();
    }

    @Test
    public void testAbstractFactory() throws Exception {
        AbstractFactory abstractFactory = new AbstractFactory();
        AbstractFactory.IFactory factory1 = abstractFactory.new Factory1();
        AbstractFactory.IClothe clothe1 = factory1.newClothe(AbstractFactory.Factory1.FACTORY_TYPE1);
        AbstractFactory.IToy toy2 = factory1.newToy(AbstractFactory.Factory1.FACTORY_TYPE2);
        clothe1.show();
        toy2.show();
    }

    @Test
    public void testBuilderPattern() throws Exception {
        BuilderPattern.Product product = new BuilderPattern.Product.Builder()
                .partA("aa").partB("bb").partC("cc")
                .builder();
        product.show();
        BuilderPattern.Product product2 = product.newBuilder()
                .partA("aaaa").partB("bbbb")
                .builder();
        product2.show();
    }

    @Test
    public void testBuilderPattern2() throws Exception {
        BuilderPattern2.Product product = new BuilderPattern2.Product.Builder("aa")
                .partB("bb").partC("cc").builder();
        product.show();
    }

    @Test
    public void testProxyPattern() throws Exception {
        ProxyPattern proxyPattern = new ProxyPattern();
        ProxyPattern.Proxy proxy = proxyPattern.new Proxy();
        proxy.request();
    }

    @Test
    public void testAdapterPattern() throws Exception {
        AdapterPattern adapterPattern = new AdapterPattern();
        AdapterPattern.FuTeAdapter fuTeAdapter = adapterPattern.new FuTeAdapter();
        fuTeAdapter.run();
    }

    @Test
    public void testBridgePattern() throws Exception {
        BridgePattern bridgePattern = new BridgePattern();
        BridgePattern.F1Car f1Car = bridgePattern.new F1Car();
        BridgePattern.F2Car f2Car = bridgePattern.new F2Car(f1Car);
        f2Car.video();
    }

    @Test
    public void testDecoratorPattern() throws Exception {
        DecoratorPattern decoratorPattern = new DecoratorPattern();
        DecoratorPattern.FuTeCar fuTeCar = decoratorPattern.new FuTeCar();
        fuTeCar.run();
        DecoratorPattern.RedCar redCar = decoratorPattern.new RedCar(fuTeCar);
        redCar.run();
    }

    @Test
    public void testFacadePattern() throws Exception {
        FacadePattern.SystemManager.getInstance().show();
    }
}