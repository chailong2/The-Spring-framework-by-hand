package springframework.beans.context.support;

import springframework.BeansException;
import springframework.beans.factory.ConfigurableListableBeanFactory;
import springframework.beans.factory.support.DefaultListableBeanFactory;

public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {
    private DefaultListableBeanFactory beanFactory;


    @Override
    protected  void refreshBeanFactory() throws BeansException{
        //创建工厂
        DefaultListableBeanFactory beanFactory=createBeanFactory();
        //加载BeanDefinition
        loadBeanDefinitions(beanFactory);
        this.beanFactory=beanFactory;
    }
    private DefaultListableBeanFactory createBeanFactory(){
        return new DefaultListableBeanFactory();
    }
    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException;
    @Override
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return (ConfigurableListableBeanFactory) beanFactory;
    }
}
