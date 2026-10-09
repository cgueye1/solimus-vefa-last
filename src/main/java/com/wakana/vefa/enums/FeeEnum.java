
package com.wakana.vefa.enums;

public enum FeeEnum {
    LOTFEE("LOTFEE");
    

    private final String value;

    FeeEnum (String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
