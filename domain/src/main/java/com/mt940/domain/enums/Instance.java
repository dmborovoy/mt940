package com.mt940.domain.enums;

public enum Instance {
    UNKNOWN(-1, "UNKNOWN", false),
    RUSSIA(0, "RUSSIA", true),
    EUROPE(1, "EUROPE", true),
    ASIA(2, "ASIA", false),
    AMERICA(3, "AMERICA", true);

    final int code;
    final String stringCode;
    final boolean isAllowed;

    private Instance(int code, String stringCode, boolean isAllowed) {
        this.code = code;
        this.stringCode = stringCode;
        this.isAllowed = isAllowed;
    }

    public static Instance findByCode(int code) {
        Instance[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Instance value = var1[var3];
            if (value.getCode() == code) {
                return value;
            }
        }

        return UNKNOWN;
    }

    public static Instance findByStringCode(String code) {
        Instance[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Instance value = var1[var3];
            if (value.getStringCode().equals(code.toUpperCase())) {
                return value;
            }
        }

        return UNKNOWN;
    }

    public int getCode() {
        return this.code;
    }

    public String getStringCode() {
        return this.stringCode;
    }

    public boolean isAllowed() {
        return this.isAllowed;
    }
}
