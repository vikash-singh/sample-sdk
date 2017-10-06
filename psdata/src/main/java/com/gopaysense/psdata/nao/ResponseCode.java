package com.gopaysense.psdata.nao;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public enum ResponseCode {

    CONNECTION_ISSUE(-1), ERROR(0), SUCCESS(1), USER_EXIT(2);

    private int code;

    private ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() { return code; }

    public static ResponseCode get(int code) {
        for(ResponseCode s : values()) {
            if(s.code == code) return s;
        }
        return null;
    }

}
