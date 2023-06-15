package springframework.beans.context;

import springframework.BeansException;

public interface ConfigurableApplicationContext extends ApplicationContext{
    //刷新容器
    void refresh() throws BeansException;
}
