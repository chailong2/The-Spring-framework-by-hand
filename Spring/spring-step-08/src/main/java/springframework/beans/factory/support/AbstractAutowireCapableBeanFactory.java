package springframework.beans.factory.support;

import cn.hutool.core.bean.BeanException;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import springframework.BeansException;
import springframework.PropertyValue;
import springframework.PropertyValues;
import springframework.beans.factory.*;
import springframework.beans.factory.config.AutowireCapableBeanFactory;
import springframework.beans.factory.config.BeanDefinition;
import springframework.beans.factory.config.BeanPostProcessor;
import springframework.beans.factory.config.BeanReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {
    //这里使用cglib的代理方法创建bean实例
    private InstantiationStrategy instantiationStrategy=new CglibSubclassingInstantiationStrategy();

    //用来床架bean的函数
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean=null;
        try{
            bean=ctreatBeanInstance(beanDefinition,beanName,args);
            //给bean对象填充属性
            applyPropertyValues(beanName,bean,beanDefinition);
            //执行Beand对象的初始化方法和BeanPostProcessor接口的前置方法和后置处理方法
            bean=initializeBean(beanName,bean,beanDefinition);
        }catch (Exception e){
            throw new BeansException("Instantiation of bean failed",e);
        }
        //注册实现了DisposeableBean接口的Bean对象
        registerDisposableBeanIfNeccessary(beanName,bean,beanDefinition);
        //判断SCOPE_SINGLE，SCOPE_PROTOTYPE
        registerSinleton(beanName,bean);
        //
        return bean;
    }
    protected void registerDisposableBeanIfNeccessary(String beanName,Object bean, BeanDefinition beanDefinition){
        if(bean instanceof DisposeableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())){
            registerDisposableBean(beanName,new DisposableBeanAdapter(bean,beanName,beanDefinition));
        }
    }
    private Object initializeBean(String beanName,Object bean, BeanDefinition beanDefinition) throws BeansException {
        //如果该bean标记类Aware的接口
        if(bean instanceof Aware){
            if(bean instanceof BeanFactoryAware){
                ((BeanFactoryAware)bean).setBeanFactory(this);
            }
            if(bean instanceof BeanClassLoaderAware){
                ((BeanClassLoaderAware)bean).setBeanClassLoader(getBeanClassLoader());
            }
            if(bean instanceof BeanNameAware){
                ((BeanNameAware)bean).setBeanName(beanName);
            }
        }
        //1. 执行BeanPostProcessor Before前置处理
        Object wrappedBean=applyBeanPostProcessorsBeforeInitialization(bean,beanName);
        //2. 待完成的内容
        try{
            invokeInitMethods(beanName,wrappedBean,beanDefinition);
        }catch (Exception e){
            throw  new BeansException("Invocation of init method of bean ["+beanName+"] failed", e);
        }

        //3. 执行BeanPostProcessor After后置处理
        wrappedBean=applyBeanPostProcessorsAfterInitialization(bean,beanName);
        return wrappedBean;
    }
    private void invokeInitMethods(String beanName,Object bean, BeanDefinition beanDefinition) throws Exception, BeansException {
        //1. 实现InitializingBean接口
       if(bean instanceof InitializingBean){
           ((InitializingBean) bean).afterPropertiesSet();
       }
       //2. 配置信息init-method{判断是为了避免二次销毁}
        String initMethodName=beanDefinition.getInitMethodName();
       if(StrUtil.isNotEmpty(initMethodName)){
           Method iniMethod=beanDefinition.getBeanClass().getMethod(initMethodName);
           if(null==iniMethod){
               throw  new BeansException("Cou;d not find an init method named '"+initMethodName+"' on bean with name '"+beanName+"'");
           }
           iniMethod.invoke(bean);
       }

    }
    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        Object result=existingBean;
        for(BeanPostProcessor processor :getBeanPostProcessors()){
            Object current=processor.postProcessBeforeInitialization(result,beanName);
            if(null==current) return result;
            result=current;
        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {
        Object result=existingBean;
        for(BeanPostProcessor processor :getBeanPostProcessors()){
            Object current=processor.postProcessAfterInitialization(result,beanName);
            if(null==current) return result;
            result=current;
        }
        return result;
    }

    protected Object ctreatBeanInstance(BeanDefinition beanDefinition, String beanName, Object[] args) throws BeansException {
        Constructor constructorToUser=null;
        Class<?> beanClass=beanDefinition.getBeanClass();
        Constructor<?>[] declaredConstructors=beanClass.getDeclaredConstructors();
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            if(args!=null && declaredConstructor.getParameterTypes().length==args.length)
            {
                constructorToUser=declaredConstructor;
                break;
            }
        }
        return  getInstantiationStrategy().instatiate(beanDefinition,beanName,constructorToUser,args);
    }
    //该方法用于给Bean对象填充属性
    protected  void applyPropertyValues(String beanName, Object bean , BeanDefinition beanDefinition){
        try{
            //或的bean的属性集合
            PropertyValues propertyValues = beanDefinition.getPropertyValues();
            for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
                //取出属性名和属性值
                String name=propertyValue.getName();
                Object value=propertyValue.getValue();
                if(value instanceof BeanReference){
                    //例如A依赖B，获取B的实例化对象
                    BeanReference beanReference=(BeanReference) value;
                    value=getBean(beanReference.getBeanName());
                }
                //属性填充
                BeanUtil.setFieldValue(bean,name,value);
            }
        } catch (Exception | BeansException e) {
            throw new BeanException("Error setting property values："+beanName);
        }
    }
    public InstantiationStrategy getInstantiationStrategy(){
        return instantiationStrategy;
    }
    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy){
        this.instantiationStrategy=instantiationStrategy;
    }
}
