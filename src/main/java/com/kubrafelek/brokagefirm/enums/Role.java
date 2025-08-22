package com.kubrafelek.brokagefirm.enums;

public enum Role {
    CUSTOMER("Customer"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
