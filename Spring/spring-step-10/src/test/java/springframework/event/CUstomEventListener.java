package springframework.event;

import springframework.beans.context.ApplicationListener;

import java.util.Date;

public class CUstomEventListener implements ApplicationListener<CUstomEvent> {


    @Override
    public void onApplicationEvent(CUstomEvent event) {
        System.out.println("收到："+event.getSource()+"消息；时间："+new Date());
        System.out.println("消息："+event.getId()+":"+event.getMessage());
    }

}
