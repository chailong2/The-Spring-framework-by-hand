package springframework.bean;

import springframework.beans.context.stereotype.Component;
import springframework.beans.factory.annotation.Autowired;
import springframework.beans.factory.annotation.Value;
import java.util.Random;
@Component("userService")
public class UserService implements IUserService{
    @Value("${token}")
    private String token;
    @Autowired
    private UserDao userDao;

    @Override
    public String queryUserInfo() {
        try{
            Thread.sleep(new Random(1).nextInt(100));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return userDao.queryUserName("10001")+","+token;
    }
}
