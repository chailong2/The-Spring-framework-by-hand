import org.junit.Test;
import bean.userService;
public class ApiTest {
    @Test
    public void test_BeanFactory(){
        //初始化BeanFactory
        BeanFactory beanFactory=new BeanFactory();

        //注册Bean对象
        BeanDefinition beanDefinition=new BeanDefinition(new userService());
        beanFactory.registerBeanDefinition("userService",beanDefinition);

        //获取bean对象
        userService userService= (userService) beanFactory.getBean("userService");
        userService.queryUerInfo();
    }
}
