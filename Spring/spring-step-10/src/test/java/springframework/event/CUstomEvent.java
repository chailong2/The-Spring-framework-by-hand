package springframework.event;

import springframework.beans.context.event.ApplicationContextEvent;

public class CUstomEvent extends ApplicationContextEvent {
    private long id;
    private String message;

    public CUstomEvent(Object source,Long id,String message) {
        super(source);
        this.id=id;
        this.message=message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
