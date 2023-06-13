import bean.userService;
import org.junit.Test;
import springframework.BeansException;
import springframework.factory.config.BeanDefinition;
import springframework.factory.support.DefaultListableBeanFactory;

public class ApiTest {
    @Test
    public void test_BeanFactory() throws BeansException {
        //1. 初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        //2. 注册bean对象
        BeanDefinition beanDefinition=new BeanDefinition(userService.class);
        beanFactory.registerBeanDefinition("userService",beanDefinition);
        //3. 获取bean对象
        userService userService= (userService) beanFactory.getBean("userService");
        userService.queryUserInfo();
        //4.再次获取和调用对象
        userService userService_singleton=(userService) beanFactory.getBean("userService");
        userService_singleton.queryUserInfo();
        System.out.println("两个bean是否为同一个bean："+(userService_singleton.equals(userService)));
        System.out.println("userService："+userService.hashCode());
        System.out.println("userService_singleton："+userService_singleton.hashCode());
    }
}
