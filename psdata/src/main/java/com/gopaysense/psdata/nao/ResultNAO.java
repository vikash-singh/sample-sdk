package com.gopaysense.psdata.nao;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class ResultNAO<E> {

    private ResponseCode code = ResponseCode.ERROR;
    private String message;
    private E data;

    public ResultNAO() {

    }

    public ResultNAO(ResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultNAO(ResponseCode code, String message, E data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public E getData() {
        return this.data;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
