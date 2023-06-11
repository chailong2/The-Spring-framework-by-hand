package springframework.factory.support;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import springframework.BeansException;
import springframework.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;

public class CglibSubclassingInstantiationStrategy implements InstantiationStrategy {

    @Override
    public Object instatiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        //定义增强器
        Enhancer enhancer=new Enhancer();
        //定义代理类所继承的父类
        enhancer.setSuperclass(beanDefinition.getBeanClass());
        //这段代码的作用是创建一个名为"enhancer"的对象，并为其设置一个回调函数（callback），
        // 该回调函数被命名为"NoOp"。在这个回调函数中，重写了hashCode()方法，但是没有实际的实现。
        // 这个回调函数的作用是在enhancer对象执行某些操作时被调用，但是它并不会对操作产生任何影响。
        enhancer.setCallback(new NoOp() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        });
        if(null==ctor){
            //创建目标类的增加实例
            return enhancer.create();
        }
        //有参构造
        return enhancer.create(ctor.getParameterTypes(),args);
    }
}
