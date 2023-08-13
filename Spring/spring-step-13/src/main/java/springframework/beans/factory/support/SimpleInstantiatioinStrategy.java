package springframework.beans.factory.support;

import springframework.BeansException;
import springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SimpleInstantiatioinStrategy implements InstantiationStrategy{

    @Override
    public Object instatiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        Class clazz=beanDefinition.getBeanClass();
        try{
            //如果构造函数不为空
            if(ctor !=null){
                //直接使用反射创建代理对象
                return clazz.getDeclaredConstructor(ctor.getParameterTypes()).newInstance(args);
            }else {
                //构造函数为空则使用默认无参构造函数创建bean实例
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new BeansException("Failed to instantiate  ["+ clazz.getName()+"]", e);
        }
    }
}
