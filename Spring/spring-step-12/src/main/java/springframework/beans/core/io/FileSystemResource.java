package springframework.beans.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
public class FileSystemResource implements Resource {
    //定义一个文件对象
    private final File file;
    //定义文件路径
    private final String path;

    public FileSystemResource(File file){
        this.file=file;
        this.path=file.getPath();
    }
    public FileSystemResource(String path){
        this.file=new File(path);
        this.path=path;
    }
    public final String getPath(){
        return this.path;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }
}
