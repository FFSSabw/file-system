package com.ffssabcloud.file_system.exception;

public class MkdirExistedException extends MkdirException{

    public MkdirExistedException(String msg) {
        super(msg);
    }
    
    public MkdirExistedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
