package springframework.factory.support;

import cn.hutool.core.bean.BeanException;
import cn.hutool.core.bean.BeanUtil;
import springframework.BeansException;
import springframework.PropertyValue;
import springframework.PropertyValues;
import springframework.factory.config.BeanDefinition;
import springframework.factory.config.BeanReference;

import java.lang.reflect.Constructor;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory{
    //这里使用cglib的代理方法创建bean实例
    private InstantiationStrategy instantiationStrategy=new CglibSubclassingInstantiationStrategy();
    private PropertyValues propertyValues;

    //用来床架bean的函数
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition,Object[] args) throws BeansException {
        Object bean=null;
        try{
            bean=ctreatBeanInstance(beanDefinition,beanName,args);
            //给bean对象填充属性
            applyPropertyValues(beanName,bean,beanDefinition);
        }catch (Exception e){
            throw new BeansException("Instantiation of bean failed",e);
        }
        registerSinleton(beanName,bean);
        return bean;
    }

    protected Object ctreatBeanInstance(BeanDefinition beanDefinition,String beanName,Object[] args) throws BeansException {
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
