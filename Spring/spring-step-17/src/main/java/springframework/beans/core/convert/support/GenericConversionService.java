package springframework.beans.core.convert.support;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import springframework.beans.core.convert.converter.Converter;
import springframework.beans.core.convert.converter.ConverterRegistry;
import springframework.beans.core.convert.converter.GenericConverter;


public class GenericConversionService implements ConversionService, ConverterRegistry {

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return false;
    }

    @Override
    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return false;
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        return null;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return null;
    }
    @Override
    public void addConverter(GenericConverter converter) {

    }

    @Override
    public void addConverter(Converter<?, ?> converter) {

    }
}
