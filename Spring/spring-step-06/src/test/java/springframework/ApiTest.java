package springframework;

import org.junit.Test;
import springframework.bean.userService;
import springframework.beans.context.support.ClassPathXmlApplicationContext;
import springframework.beans.factory.ConfigurableListableBeanFactory;
import springframework.beans.factory.support.DefaultListableBeanFactory;
import springframework.beans.factory.xml.XmlBeanDefinitionReader;
import springframework.common.MyBeanFactoryPostProcessor;
import springframework.common.MyBeanPostProcessor;

/**
 * 这是一个示例类的描述。
 */
public class ApiTest {
    @Test
    public void test_xml1() throws BeansException {
        //初始化BeanFactory接口
        DefaultListableBeanFactory beanFactory=new DefaultListableBeanFactory();
        //读取配置文件注册bean对象
        XmlBeanDefinitionReader reader=new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions("classpath:spring2.xml");
        //BeanDefinition加载完成，在将Bean对象实例化之前，修改BeanDefintion的属性值
        MyBeanFactoryPostProcessor beanPostProcessor=new MyBeanFactoryPostProcessor();
        beanPostProcessor.postProcessBeanFactory(beanFactory);
        //获取bean对象的调用方法
        userService userService= (userService) beanFactory.getBean("userService",userService.class);
        userService.queryUserInfo();
    }
    @Test
    public void test_xml2() throws BeansException {
        //初始化BeanFactory接口
        ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring2.xml");
        //获取bean对象的调用方法
        userService userService1= applicationContext.getBean("userService",userService.class);
        userService1.queryUserInfo();
    }
}

