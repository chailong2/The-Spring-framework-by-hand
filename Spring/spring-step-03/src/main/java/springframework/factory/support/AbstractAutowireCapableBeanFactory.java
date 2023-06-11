package springframework.factory.support;

import springframework.BeansException;
import springframework.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    //这里使用cglib的代理方法创建bean实例
    private InstantiationStrategy instantiationStrategy=new CglibSubclassingInstantiationStrategy();
    //用来床架bean的函数
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition,Object[] args) throws BeansException {
        Object bean=null;
        try{
            bean=ctreatBeanInstance(beanDefinition,beanName,args);
        }catch (Exception e){
            throw new BeansException("Instantiation of bean failed",e);
        }
        registerSinleton(beanName,bean);
        return bean;
    }

    protected Object ctreatBeanInstance(BeanDefinition beanDefinition,String beanName,Object[] args) throws BeansException {
        //创建构造函数对象
        Constructor constructorTOuser=null;
        //获得BeanDefinition的类型
        Class<?> beanClass=beanDefinition.getBeanClass();
        //获得Bean的所有构造函数
        Constructor<?>[] declaredConstructors=beanClass.getDeclaredConstructors();
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            //按照提供的参数类型获得指定的构造函数
            if(args!=null && declaredConstructor.getParameterTypes().length==args.length){
                constructorTOuser=declaredConstructor;
                break;
            }
        }
        return instantiationStrategy.instatiate(beanDefinition,beanName,constructorTOuser,args);
    }
}
