package com.just.example.service;

import com.just.example.exception.ErrorResponse;
import com.just.example.exception.ServiceException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceExceptionMapper.class);

    public ServiceExceptionMapper() {
    }

    public Response toResponse(final ServiceException serviceException) {

        if (logger.isDebugEnabled()) {
            logger.debug("Mapping exception to Response....");
        }
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(serviceException.getMessage());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

}
