package springframework.factory.config;

public interface SingletonBeanRegistry {
    //根据名称获得单例bean
    Object getSingleton(String Name);
    //注册单例bean的方法
    void registerSinleton(String beanName , Object singletonObject);

}
