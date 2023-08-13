package springframework;

import org.junit.Test;
import springframework.beans.context.support.ClassPathXmlApplicationContext;
import springframework.event.CUstomEvent;

/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_prototype() throws BeansException {
        //1. 初始化BeanFactory接口
        ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.publishEvent(new CUstomEvent(applicationContext,120031203021L,"成功了"));
        applicationContext.registerShutdownHook();
    }
}

