package com.easypost.easyvcr.requestelements;

public class Status {
    private int code;

    private String message;

    public Status(int code, String message) {
        setCode(code);
        setMessage(message);
    }

    public Status() {}

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
