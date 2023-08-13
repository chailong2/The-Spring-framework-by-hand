package springframework.bean;

import springframework.beans.context.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserDao {
    private static Map<String ,String > hashmap=new HashMap<>();
    static {
        hashmap.put("10001","张三,北京,东城");
        hashmap.put("10002","张四,北京,东城");
        hashmap.put("10003","张五,北京,东城");

    }
    public String queryUserName(String uid){
        return hashmap.get(uid);
    }
}
