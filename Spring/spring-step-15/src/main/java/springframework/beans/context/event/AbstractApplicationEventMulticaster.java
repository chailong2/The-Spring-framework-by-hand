package springframework.beans.context.event;

import springframework.BeansException;
import springframework.beans.context.ApplicationEvent;
import springframework.beans.context.ApplicationListener;
import springframework.beans.factory.BeanFactory;
import springframework.beans.factory.BeanFactoryAware;
import springframework.beans.utils.ClassUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {
    //事件监听器的集合，每个事件监听器绑定一个事件类型
    private final Set<ApplicationListener<ApplicationEvent>> applicationListeners=new LinkedHashSet<>();
    //定义一个Bean工厂
    private BeanFactory beanFactory;
    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
           applicationListeners.add((ApplicationListener<ApplicationEvent>) listener);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
           applicationListeners.remove(listener);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
          this.beanFactory=beanFactory;
    }
    //获取所有的事件监听器
    protected Collection<ApplicationListener> t(ApplicationEvent event) throws BeansException {
        //定义一个链表来封装所有的事件监听器
        LinkedList<ApplicationListener> allListeners=new LinkedList<ApplicationListener>();
        for(ApplicationListener<ApplicationEvent> listener: applicationListeners){
            if(supportsEvent(listener,event) ){
                allListeners.add(listener);
            }
        }
        return allListeners;
    }
    //监听器是否对事件感兴趣
    protected boolean supportsEvent(ApplicationListener<ApplicationEvent> applicationListener,ApplicationEvent event) throws BeansException {
        Class<? extends ApplicationListener> listenerClass=applicationListener.getClass();
        //按照cglibSubClassInstantiationStratege和SimpleInstantiationStrategy不同的实例化类型，需要判断后获取目标的class
        /**
         * 这段代码的作用是判断listenerClass是否为CGLIB代理类，如果是，则获取其父类作为目标类（targetClass），如果不是，则直接将listenerClass作为目标类。
         * 在Spring框架中，CGLIB是一种用于生成代理类的库。当使用CGLIB生成代理类时，代理类会继承目标类，而不是实现目标类的接口。因此，如果listenerClass
         * 是CGLIB代理类，我们需要获取它的父类作为目标类，以便后续使用。这段代码中的ClassUtils.isCglibProxyClass(listenerClass)方法用于判断listenerClass
         * 是否为CGLIB代理类。如果是，则通过listenerClass.getSuperclass()方法获取其父类作为目标类；如果不是，则直接将listenerClass作为目标类。
         * 通过这种方式，我们可以确保在处理CGLIB代理类时，获取到正确的目标类。
         */
        Class<?> targetClass= ClassUtils.isCglibProxyClass(listenerClass)? listenerClass.getSuperclass() :listenerClass;
        /**
         * 获取targetClass的泛型接口。
         * 在Java中，泛型接口是一个使用一个或多个类型参数进行参数化的接口。它允许在运行时使用不同类型的接口。
         * 在这段代码中，targetClass.getGenericInterfaces()返回一个由targetClass实现的泛型接口数组。[0]用于访问这个数组的第一个元素。
         * 通过将genericInterface赋值为targetClass.getGenericInterfaces()[0]，代码获取了targetClass实现的第一个泛型接口。
         * 这样可以进一步操作或分析泛型接口，比如提取其类型参数或检查其属性。
         */
        Type genericInterface=targetClass.getGenericInterfaces()[0];
        Type actualTypeArgument=((ParameterizedType)genericInterface).getActualTypeArguments()[0];
        String className=actualTypeArgument.getTypeName();
        Class<?> eventClassName;
        try{
            eventClassName =Class.forName(className);
        }catch (ClassNotFoundException e){
            throw new BeansException("Wrong event class name:"+className);
        }
        //判断此eventClassName对象表示的类或接口与指定的event.getClass参数所表示的类或接口是否相同，或者是否是其超类或超接口，isAssignableFrom
        //用来判断子类和父类的关系，或者接口的实现类和接口的关闭，默认所有类的终极父类都是Object，如果A.isAssignableFrom(B)的值为true，则证明B
        //可以转换成A，也就是说，A可以由B转换而来
        return eventClassName.isAssignableFrom(event.getClass());
    }
}
