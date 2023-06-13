package springframework;

import org.junit.Test;
import springframework.bean.userService;
import springframework.factory.config.BeanDefinition;
import springframework.factory.support.BeanDefinitionRegistry;
import springframework.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_BeanFactory(){
        //1. 初始化BeanFactory
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        //2. 注册Bean对象
        BeanDefinition beanDefinition=new BeanDefinition(userService.class);
        /*注册BeanDefinition*/
        beanFactory.registerBeanDefinition("userService",beanDefinition);
        //3. 获取Bean对象
        userService userService= null;
        try {
            userService = (userService) beanFactory.getBean("userService","jakiechai");
        } catch (BeansException e) {
            throw new RuntimeException(e);
        }
        userService.queryUserInfo();
    }
}

