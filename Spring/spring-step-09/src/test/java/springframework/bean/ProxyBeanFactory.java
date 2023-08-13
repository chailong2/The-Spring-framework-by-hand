package springframework.bean;


import springframework.beans.factory.FactoryBean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ProxyBeanFactory implements FactoryBean<IUserDao> {
    @Override
    public IUserDao getObject()  {
        InvocationHandler handler=(proxy,method,args)->{
            Map<String,String> hashmap=new HashMap<>();
            hashmap.put("10001","张三");
            hashmap.put("10002","李四");
            hashmap.put("10003","王五");
            return "你被代理了"+method.getName()+"："+hashmap.get(args[0].toString());
        };
        return (IUserDao) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{IUserDao.class},handler);
    }

    @Override
    public Class<?> getObjectType() {
        return IUserDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
