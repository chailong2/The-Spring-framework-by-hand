package springframework.beans.factory.support;

import cn.hutool.core.bean.BeanException;
import cn.hutool.core.util.StrUtil;
import springframework.beans.factory.DisposeableBean;
import springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;

public class DisposableBeanAdapter implements DisposeableBean {
    private final Object bean;
    private final String beanName;
    private String destroyMethodName;

    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
        this.bean = bean;
        this.beanName = beanName;
        this.destroyMethodName = beanDefinition.getDestroyMethodName();
    }

    @Override
    public void destroy() throws Exception {
        //实现DisposeableBean接口
        if( bean instanceof DisposeableBean){
            //调用Bean对象的销毁方法
            ((DisposeableBean)bean).destroy();
        }
        //配置信息destroy-method
        if(StrUtil.isNotEmpty(destroyMethodName) && !(bean instanceof DisposeableBean && "destroy".equals(this.destroyMethodName))){
            //放射获得用户配置文件中配置的方法
            Method destroyMethod=bean.getClass().getMethod(destroyMethodName);
            if(null==destroyMethod){
                //没有找到销毁方法就报错
                throw new BeanException("Could`t find a destroy method named '"+
                        destroyMethodName+" ' on bean with name '"+beanName+"'" );
            }
            destroyMethod.invoke(bean);
        }
    }
}
