package com.jelly.app;

import com.jelly.app.main.designpatterns.ITest;
import com.jelly.app.main.designpatterns.behavioral.ChainResponsibilityPattern;
import com.jelly.app.main.designpatterns.behavioral.CommandPattern;
import com.jelly.app.main.designpatterns.behavioral.InterpreterPattern;
import com.jelly.app.main.designpatterns.behavioral.IteratorPattern;
import com.jelly.app.main.designpatterns.behavioral.MediatorPattern;
import com.jelly.app.main.designpatterns.behavioral.MementoPattern;
import com.jelly.app.main.designpatterns.behavioral.ObserverPattern;
import com.jelly.app.main.designpatterns.behavioral.StatePattern;
import com.jelly.app.main.designpatterns.behavioral.StrategyPattern;
import com.jelly.app.main.designpatterns.behavioral.TemplateMethod;
import com.jelly.app.main.designpatterns.behavioral.VisitorPattern;
import com.jelly.app.main.designpatterns.create.AbstractFactory;
import com.jelly.app.main.designpatterns.create.BuilderPattern;
import com.jelly.app.main.designpatterns.create.BuilderPattern2;
import com.jelly.app.main.designpatterns.create.FactoryMethod;
import com.jelly.app.main.designpatterns.create.SimpleFactory;
import com.jelly.app.main.designpatterns.structural.AdapterPattern;
import com.jelly.app.main.designpatterns.structural.BridgePattern;
import com.jelly.app.main.designpatterns.structural.CompositePattern;
import com.jelly.app.main.designpatterns.structural.DecoratorPattern;
import com.jelly.app.main.designpatterns.structural.FacadePattern;
import com.jelly.app.main.designpatterns.structural.FlyweightPattern;
import com.jelly.app.main.designpatterns.structural.ProxyPattern;

import org.junit.Test;

public class DesignPatternsTest {
    @Test
    public void testSimpleFactory() throws Exception {
        ITest test = new SimpleFactory();
        test.test();
    }

    @Test
    public void testFactoryMethod() throws Exception {
        ITest test = new FactoryMethod();
        test.test();
    }

    @Test
    public void testAbstractFactory() throws Exception {
        ITest test = new AbstractFactory();
        test.test();
    }

    @Test
    public void testBuilderPattern() throws Exception {
        ITest test = new BuilderPattern();
        test.test();
    }

    @Test
    public void testBuilderPattern2() throws Exception {
        ITest test = new BuilderPattern2();
        test.test();
    }

    @Test
    public void testProxyPattern() throws Exception {
        ITest test = new ProxyPattern();
        test.test();
    }

    @Test
    public void testAdapterPattern() throws Exception {
        ITest test = new AdapterPattern();
        test.test();
    }

    @Test
    public void testBridgePattern() throws Exception {
        ITest test = new BridgePattern();
        test.test();
    }

    @Test
    public void testDecoratorPattern() throws Exception {
        ITest test = new DecoratorPattern();
        test.test();
    }

    @Test
    public void testFacadePattern() throws Exception {
        ITest test = new FacadePattern();
        test.test();
    }

    @Test
    public void testFlyweightPattern() throws Exception {
        ITest test = new FlyweightPattern();
        test.test();
    }

    @Test
    public void testCompositePattern() throws Exception {
        ITest test = new CompositePattern();
        test.test();
    }

    @Test
    public void testTemplateMethod() throws Exception {
        ITest test = new TemplateMethod();
        test.test();
    }

    @Test
    public void testStrategyPattern() throws Exception {
        ITest test = new StrategyPattern();
        test.test();
    }

    @Test
    public void testCommandPattern() throws Exception {
        ITest test = new CommandPattern();
        test.test();
    }

    @Test
    public void testChainResponsibilityPattern() throws Exception {
        ITest test = new ChainResponsibilityPattern();
        test.test();
    }

    @Test
    public void testStatePattern() throws Exception {
        ITest test = new StatePattern();
        test.test();
    }

    @Test
    public void testObserverPattern() throws Exception {
        ITest test = new ObserverPattern();
        test.test();
    }

    @Test
    public void testMediatorPattern() throws Exception {
        ITest test = new MediatorPattern();
        test.test();
    }

    @Test
    public void testIteratorPattern() throws Exception {
        ITest test = new IteratorPattern();
        test.test();
    }

    @Test
    public void testVisitorPattern() throws Exception {
        ITest test = new VisitorPattern();
        test.test();
    }

    @Test
    public void testMementoPattern() throws Exception {
        ITest test = new MementoPattern();
        test.test();
    }

    @Test
    public void testInterpreterPattern() throws Exception {
        ITest test = new InterpreterPattern();
        test.test();
    }
}