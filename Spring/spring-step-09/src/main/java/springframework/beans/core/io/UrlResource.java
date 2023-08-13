package springframework.beans.core.io;

import cn.hutool.core.lang.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class UrlResource implements Resource{
    private final URL url;

    public UrlResource(URL url){
        Assert.notNull(url,"URL must bot be null");
        this.url=url;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        //与指定的URL资源建立TCP连接
        URLConnection con=this.url.openConnection();
        try{
            //打开流，获取文件内容
            return con.getInputStream();
        }catch (IOException ex){
            if(con instanceof HttpURLConnection){
                //断开连接
                ((HttpURLConnection)con).disconnect();
            }
            throw ex;
        }
    }
}
