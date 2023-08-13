package springframework;

import org.junit.Test;
import springframework.bean.IUserService;
import springframework.bean.UserService;
import springframework.beans.context.support.ClassPathXmlApplicationContext;
public class ApiTest {
    @Test
    public void test() throws BeansException {
        ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        IUserService userService=applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果:"+userService);
    }
}

