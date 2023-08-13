package springframework.bean;

import springframework.beans.context.stereotype.Component;
import springframework.beans.factory.annotation.Autowired;
import springframework.beans.factory.annotation.Value;
import java.util.Random;
public class UserService implements IUserService{
    private String token;

    @Override
    public String queryUserInfo() {
        try{
            Thread.sleep(new Random(1).nextInt(100));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "张三,100001,深圳,"+token;
    }
}
