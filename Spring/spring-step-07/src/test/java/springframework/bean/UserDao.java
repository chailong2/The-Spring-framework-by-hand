package springframework.bean;

import java.util.HashMap;
import java.util.Map;

public class UserDao {
    private static Map<String,String> hashmap=new HashMap<>();

    public void initDataMethod(){
        System.out.println("执行：init-method");
        hashmap.put("10001","张三");
        hashmap.put("10002","李四");
        hashmap.put("10003","王五");
    }

    public void destroyDataMethod(){
        System.out.println("执行：destroy-method");
        hashmap.clear();
    }
    public String queryUserName(String uId){
        return hashmap.get(uId);
    }
}
