package com.ecommerce.inventoryservice.exception;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResourceNotFoundException extends RuntimeException {
    private final String resurceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resurceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resurceName, fieldName, fieldValue));
        this.resurceName = resurceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
