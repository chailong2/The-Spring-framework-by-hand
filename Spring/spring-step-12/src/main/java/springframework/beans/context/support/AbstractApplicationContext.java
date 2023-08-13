package springframework.beans.context.support;

import springframework.BeansException;
import springframework.beans.context.ApplicationEvent;
import springframework.beans.context.ApplicationListener;
import springframework.beans.context.ConfigurableApplicationContext;
import springframework.beans.context.event.ApplicationEventMulticaster;
import springframework.beans.context.event.ContextClosedEvent;
import springframework.beans.context.event.ContextRefreshedEvent;
import springframework.beans.context.event.SimpleApplicationEventMulticaster;
import springframework.beans.core.io.DefaultResourceLoader;
import springframework.beans.factory.ConfigurableListableBeanFactory;
import springframework.beans.factory.config.BeanFactoryPostProcessor;
import springframework.beans.factory.config.BeanPostProcessor;
import springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    public static final  String APPLICATION_EVENT_MULTICASTER_BEAN_NAME="applicationEventMulticaster";
    private ApplicationEventMulticaster applicationEventMulticaster;
    @Override
    public void refresh() throws BeansException {
        //1. 创建BeanFactory，并加载BeanDefinition
        refreshBeanFactory();
        //2. 获取BeanFactory
        ConfigurableListableBeanFactory beanFactory=getBeanFactory();
        //3. 在将Bean对象实例化执行之前，执行BeanFactoryPostProcessor操作
        invokeBeanFactoryPostProcessor(beanFactory);
        //4. BeanPostProcessor需要将Bean对象实例化之前注册
        registerBeanPostProcessor(beanFactory);
        //5. 提前实例化单例Bean对象
        beanFactory.preInstantiateSingletons();
    }
    private void initApplicationEventMulticaster(){
        ConfigurableListableBeanFactory beanFactory=getBeanFactory();
        applicationEventMulticaster=new SimpleApplicationEventMulticaster(beanFactory);
        //将事件广播器注册为一个Bean
        beanFactory.registerSinleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME,applicationEventMulticaster);

    }
    private void registerListeners() throws BeansException {
        Collection<ApplicationListener> applicationListeners=getBeansOfType(ApplicationListener.class).values();
        for(ApplicationListener listener:applicationListeners){
            applicationEventMulticaster.addApplicationListener(listener);
        }
    }
    public void finishRefresh(){
        publishEvent(new ContextRefreshedEvent(this));
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
       applicationEventMulticaster.multicastEvent(event);
    }


    protected abstract void refreshBeanFactory() throws BeansException;
    protected abstract ConfigurableListableBeanFactory getBeanFactory();

    private void invokeBeanFactoryPostProcessor(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, BeanFactoryPostProcessor> beanFactoryPostProcessorMap=beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);
        for(BeanFactoryPostProcessor beanFactoryPostProcessor:beanFactoryPostProcessorMap.values()){
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    private void registerBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, BeanPostProcessor> beanPostProcessorMap=beanFactory.getBeansOfType(BeanPostProcessor.class);
        for(BeanPostProcessor beanPostProcessor: beanPostProcessorMap.values()){
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
    }

    @Override
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public void close() {
        try {
            publishEvent(new ContextClosedEvent(this));
            getBeanFactory().destroySingletons();
        }catch (Exception e){}

    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> var1) throws BeansException {
        return getBeanFactory().getBeansOfType(var1);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return getBeanFactory().getBean(name,args);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return getBeanFactory().getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return getBeanFactory().getBean(name,requiredType);
    }
    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }

}
