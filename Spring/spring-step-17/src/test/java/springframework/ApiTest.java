package springframework;

import org.junit.Test;
import springframework.bean.Husband;
import springframework.bean.Wife;
import springframework.beans.context.support.ClassPathXmlApplicationContext;
public class ApiTest {
    @Test
    public void test() throws BeansException {
        ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        Husband husband= applicationContext.getBean("husband",Husband.class);
        Wife wife=applicationContext.getBean("wife" ,Wife.class);
        System.out.println("老公的媳妇："+husband.queryWife());
        System.out.println("媳妇的老公："+wife.queryHusband());
    }
}

