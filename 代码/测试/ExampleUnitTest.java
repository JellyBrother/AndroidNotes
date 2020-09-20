
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ExampleUnitTest {
    private String mark = "";

    @Before
    public void testBefore() {
        // 在任何一个测试方法执行之前
        mark = "Before";
        System.out.println("Before");
    }

    @Test
    public void testTest() {
        // 测试方法
        System.out.println("Test1 :" + mark);
        mark = "Test";
        System.out.println("Test2 :" + mark);
    }

    @Test(timeout = 100, expected = NullPointerException.class)
    public void testTestExpected() {
        // 超过timeout，就会提示超时，没有抛出expected设置的异常就会提示
        System.out.println("testTestExpected1 :" + mark);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    @After
    public void testAfter() {
        // 在任何一个测试方法执行之后
        System.out.println("After1 :" + mark);
        mark = "After";
        System.out.println("After2 :" + mark);
    }

    @Ignore
    public void testIgnore() {
        System.out.println("Ignore");
    }
}