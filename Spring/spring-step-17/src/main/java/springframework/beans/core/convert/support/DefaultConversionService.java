package springframework.beans.core.convert.support;

import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.GenericConversionService;

import springframework.beans.core.convert.converter.GenericConverter;

public class DefaultConversionService extends GenericConversionService {
    public DefaultConversionService(){
        addDeaultConverters(this);
    }
    public static void addDeaultConverters(ConverterRegistry converterRegistry){
        //添加各类的类型转换工厂
        converterRegistry.addConverterFactory((ConverterFactory<?, ?>) new StringToNumberConverterFactory());
    }
}
