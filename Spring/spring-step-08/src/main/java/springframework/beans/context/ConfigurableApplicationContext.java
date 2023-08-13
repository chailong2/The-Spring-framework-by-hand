package springframework.beans.context;

import springframework.BeansException;

public interface ConfigurableApplicationContext extends ApplicationContext{
    //刷新容器
    void refresh() throws BeansException;
    //注册虚拟机钩子
    void registerShutdownHook();
    //关闭容器
    void close();
}
