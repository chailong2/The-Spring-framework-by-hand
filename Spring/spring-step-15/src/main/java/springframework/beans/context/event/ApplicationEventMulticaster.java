package springframework.beans.context.event;

import springframework.beans.context.ApplicationContextAware;
import springframework.beans.context.ApplicationEvent;
import springframework.beans.context.ApplicationListener;

public interface ApplicationEventMulticaster {
    //添加事件监听器
    void addApplicationListener(ApplicationListener<?> listener);

    //移除事件监听器
    void removeApplicationListener(ApplicationListener<?> listener);

    void multicastEvent(ApplicationEvent event);
}
