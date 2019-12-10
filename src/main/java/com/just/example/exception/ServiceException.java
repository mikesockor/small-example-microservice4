package com.just.example.exception;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -5391044006736835105L;

    public ServiceException(final String msg) {
        super(msg);
    }

    public ServiceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
