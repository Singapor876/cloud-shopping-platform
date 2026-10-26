package com.cloud.oms.enums;

public enum PayType {
    WX("1", "WeChat Pay"),
    ZFB("2", "Alipay");

    private final String value;
    private final String label;

    PayType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    // Find enum by dictionary value
    public static PayType fromValue(String value) {
        for (PayType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
