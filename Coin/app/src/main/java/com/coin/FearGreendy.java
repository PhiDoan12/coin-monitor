package com.coin;

import java.math.BigDecimal;

public class FearGreendy {
    private String status = "UNKNOWN";
    private BigDecimal value = new BigDecimal("0");

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
