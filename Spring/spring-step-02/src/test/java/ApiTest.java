import bean.Userservice;
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
        BeanDefinition beanDefinition=new BeanDefinition(Userservice.class);
        beanFactory.registerBeanDefinition("userService",beanDefinition);
        //3. 获取bean对象
        Userservice userservice= (Userservice) beanFactory.getBean("userService");
        userservice.queryUserInfo();
        //4.再次获取和调用对象
        Userservice userservice_singleton=(Userservice) beanFactory.getBean("userService");
        userservice_singleton.queryUserInfo();
        System.out.println("两个bean是否为同一个bean："+(userservice_singleton.equals(userservice)));
        System.out.println("userservice："+userservice.hashCode());
        System.out.println("userservice_singleton："+userservice_singleton.hashCode());
    }
}
