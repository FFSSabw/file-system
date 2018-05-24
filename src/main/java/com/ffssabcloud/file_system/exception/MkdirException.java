package com.ffssabcloud.file_system.exception;

public class MkdirException extends RuntimeException{
    
    public MkdirException(String msg) {
        super(msg);
    }
    
    public MkdirException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
