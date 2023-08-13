package springframework;

import org.junit.Test;
import springframework.bean.UserService;
import springframework.beans.context.support.ClassPathXmlApplicationContext;
import org.openjdk.jol.info.ClassLayout;
/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_prototype() throws BeansException {
        //1. 初始化BeanFactory接口
        ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.registerShutdownHook();

        //2. 获取Bean对象的调用方法
        UserService userService1=applicationContext.getBean("UserService",UserService.class);
        UserService userService2=applicationContext.getBean("UserService",UserService.class);

        //3. 配置scope="prototype/singeton"
        System.out.println(userService1);
        System.out.println(userService2);

        //4. 输出十六进制哈希值
        System.out.println(userService1+" 十六进制hash值："+Integer.toHexString(userService1.hashCode()));
        //使用ClassLayout来打印userService1对象的内存布局信息。
        System.out.println(ClassLayout.parseInstance(userService1).toPrintable());
    }
}

