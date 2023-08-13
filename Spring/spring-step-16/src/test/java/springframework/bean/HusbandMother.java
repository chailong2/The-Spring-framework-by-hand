package springframework.bean;

import springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class HusbandMother implements FactoryBean<IMother> {

    @Override
    public IMother getObject() throws Exception {
        return (IMother) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[]{IMother.class},
                (Proxy,method,args)->"婚后媳妇的妈妈的职责被婆婆代理了！"+method.getName());
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
