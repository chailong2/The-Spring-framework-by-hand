package springframework.beans.core.convert.converter;

public interface ConverterRegistry {
    //注册一个普通的类型转换器
    void addConverter(Converter<?,?> converter);
    //注册一个泛型类型转换器
    void addConverter(GenericConverter converter);
}
