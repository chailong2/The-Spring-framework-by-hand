package springframework;

import org.junit.Test;
import springframework.bean.userService;
import springframework.beans.factory.support.DefaultListableBeanFactory;
import springframework.beans.factory.xml.XmlBeanDefinitionReader;
/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_xml() throws BeansException {
        //初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();

        //读取配置文件注册bean对象
        XmlBeanDefinitionReader reader=new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions("classpath:spring.xml");

        //获取bean对象的调用方法
        userService userService= (userService) beanFactory.getBean("userService",userService.class);
        userService.queryUserInfo();
    }
}

