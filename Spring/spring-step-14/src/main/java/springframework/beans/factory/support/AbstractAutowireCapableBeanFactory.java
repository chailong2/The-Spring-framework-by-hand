package springframework.beans.factory.support;

import cn.hutool.core.bean.BeanException;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.sun.istack.internal.Nullable;
import springframework.BeansException;
import springframework.PropertyValue;
import springframework.PropertyValues;
import springframework.beans.factory.*;
import springframework.beans.factory.config.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {
    //这里使用cglib的代理方法创建bean实例
    private InstantiationStrategy instantiationStrategy=new CglibSubclassingInstantiationStrategy();
    private PropertyValues propertyValues;

    //用来床架bean的函数
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean=null;
        try{
            bean = resolveBeforeInstantiation(beanName, beanDefinition);
            if (null != bean) {
                return bean;
            }
            bean=ctreatBeanInstance(beanDefinition,beanName,args);
            //在设置Bean对象的属性之前，运行BeanPostProcesppr接口修改属性值
            applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName,bean,beanDefinition);
            //给bean对象填充属性
            applyPropertyValues(beanName,bean,beanDefinition);
            //执行Beand对象的初始化方法和BeanPostProcessor接口的前置方法和后置处理方法
            bean=initializeBean(beanName,bean,beanDefinition);
        }catch (Exception e){
            throw new BeansException("Instantiation of bean failed",e);
        }
        registerDisposableBeanIfNeccessary(beanName,bean,beanDefinition);
        //注册实现了DisposeableBean接口的Bean对象
        if(beanDefinition.isSingleton()){
            registerSinleton(beanName,bean);
        }
        return bean;
    }
    /**
     * 执行Bean实例化前的操作
     */
    protected Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) throws BeansException {
        Object bean = applyBeanPostProcessorsBeforeInstantiation(beanDefinition.getBeanClass(), beanName);
        if (null != bean) {
            //  应用Bean的后置增强器
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        }
        return bean;
    }
    /**
     * 执行Bean实例化前的操作
     */
    protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                Object result = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessBeforeInstantiation(beanClass, beanName);
                if (null != result) {
                    return result;
                }
            }
        }
        return null;
    }


    protected void registerDisposableBeanIfNeccessary(String beanName,Object bean, BeanDefinition beanDefinition){
        //非singleton类型的bean对象不必执行销毁方法
        if(!beanDefinition.isSingleton())return;;
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

    protected Object ctreatBeanInstance(BeanDefinition beanDefinition, String beanName, @Nullable Object[] args) throws BeansException {
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
    protected void applyBeanPostProcessorsBeforeApplyingPropertyValues(String beanName,Object bean ,BeanDefinition beanDefinition) throws BeansException {
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if(beanPostProcessor instanceof  InstantiationAwareBeanPostProcessor){
                PropertyValues pvs=((InstantiationAwareBeanPostProcessor)beanPostProcessor).postProcessPropertyValues(beanDefinition.getPropertyValues(),bean,beanName);
                if (pvs!=null) {
                    for (PropertyValue propertyValue : pvs.getPropertyValues()) {
                        beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
                    }
                }
            }

        }
    }
}
