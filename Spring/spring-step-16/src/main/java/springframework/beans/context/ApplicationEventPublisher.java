package springframework.beans.context;

public interface ApplicationEventPublisher {
    void publishEvent(ApplicationEvent event);
}
