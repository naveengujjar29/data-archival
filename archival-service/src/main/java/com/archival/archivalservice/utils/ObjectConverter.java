package com.archival.archivalservice.utils;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ObjectConverter {

    private ModelMapper modelMapper = new ModelMapper();

    public Object convert(final Object source, final Class<?> destination) {
        try {
            return this.modelMapper.map(source, destination);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to map");
        }
    }

}
