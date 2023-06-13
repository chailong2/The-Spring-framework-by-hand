package springframework;

import org.junit.Test;
import springframework.bean.UserDao;
import springframework.bean.userService;
import springframework.factory.config.BeanDefinition;
import springframework.factory.config.BeanReference;
import springframework.factory.support.BeanDefinitionRegistry;
import springframework.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_BeanFactory() throws BeansException {
        //初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        //注册UserDao
        beanFactory.registerBeanDefinition("userDao",new BeanDefinition(UserDao.class));
        //使用userService填充属性
        PropertyValues propertyValues=new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("uId","10001"));
        propertyValues.addPropertyValue(new PropertyValue("userDao",new BeanReference("userDao")));
        //使用userService注册Bean对象
        BeanDefinition beanDefinition=new BeanDefinition(userService.class,propertyValues);
        beanFactory.registerBeanDefinition("userService",beanDefinition);
        //使用userServices获取Bean对象
        userService userService=(userService) beanFactory.getBean("userService");
        userService.queryUserInfo();
    }
}

