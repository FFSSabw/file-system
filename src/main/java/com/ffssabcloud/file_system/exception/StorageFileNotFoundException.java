package com.ffssabcloud.file_system.exception;

public class StorageFileNotFoundException extends StorageException{

    public StorageFileNotFoundException(String msg) {
        super(msg);
    }
    
    public StorageFileNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
