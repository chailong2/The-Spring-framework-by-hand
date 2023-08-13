package springframework.beans.context.support;

import springframework.BeansException;
import springframework.beans.context.ApplicationContext;
import springframework.beans.context.ApplicationContextAware;
import springframework.beans.factory.config.BeanPostProcessor;

public class ApplicationContextAwareProcessor implements BeanPostProcessor {
    private final ApplicationContext applicationContext;
    //构造函数获得当前的容器的对象
    public  ApplicationContextAwareProcessor (ApplicationContext applicationContext){
        this.applicationContext=applicationContext;
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ApplicationContextAware){
            //bean可以获取当前的容器对象，这样该bean就可以操作Bean容器对象了
            ((ApplicationContextAware)bean).setApplicationContext(applicationContext);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        return bean;
    }
}
