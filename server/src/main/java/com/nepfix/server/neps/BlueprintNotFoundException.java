package com.nepfix.server.neps;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BlueprintNotFoundException extends RuntimeException {
}
