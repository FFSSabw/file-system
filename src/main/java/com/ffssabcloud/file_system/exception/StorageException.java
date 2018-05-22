package com.ffssabcloud.file_system.exception;

public class StorageException extends RuntimeException{
    
    public StorageException(String msg) {
        super(msg);
    }
    
    public StorageException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
