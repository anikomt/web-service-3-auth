package ru.ifmo.web.service.util;

import lombok.Getter;

import javax.xml.ws.WebFault;

@WebFault(faultBean = "ru.ifmo.web.service.util.UserServiceFault")
public class UserServiceException extends Exception {
    @Getter
    private final UserServiceFault faultInfo;

    public UserServiceException(String message, UserServiceFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public UserServiceException(String message, Throwable cause, UserServiceFault faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }
}
