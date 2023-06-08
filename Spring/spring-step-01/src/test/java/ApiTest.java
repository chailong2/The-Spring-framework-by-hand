import org.junit.Test;
import bean.Userservice;
public class ApiTest {
    @Test
    public void test_BeanFactory(){
        //初始化BeanFactory
        BeanFactory beanFactory=new BeanFactory();

        //注册Bean对象
        BeanDefinition beanDefinition=new BeanDefinition(new Userservice());
        beanFactory.registerBeanDefinition("userservice",beanDefinition);

        //获取bean对象
        Userservice userservice= (Userservice) beanFactory.getBean("userservice");
        userservice.queryUerInfo();
    }
}
