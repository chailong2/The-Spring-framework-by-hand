package springframework;

import org.junit.Test;
import springframework.bean.userService;
import springframework.beans.context.support.ClassPathXmlApplicationContext;

/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_xml2() throws BeansException {
        //初始化BeanFactory接口
        ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.registerShutdownHook();
        //获取bean对象的调用方法
        userService userservice=applicationContext.getBean("userService",userService.class);
        userservice.queryUserInfo();
    }
}

