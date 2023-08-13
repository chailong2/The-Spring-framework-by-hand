package springframework.beans.context.event;

import springframework.beans.context.ApplicationEvent;
import springframework.beans.factory.config.ConfigurableBeanFactory;

public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {
    public SimpleApplicationEventMulticaster(ConfigurableBeanFactory beanFactory) {

    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
    }
}
