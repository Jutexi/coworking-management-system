package com.app.coworking.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import com.app.coworking.service.VisitCounterService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VisitAspect {

    private final VisitCounterService visitCounterService;

    public VisitAspect(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Before("execution(* com.app.coworking.controller..*(..))")
    public void registerVisit() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String uri = request.getRequestURI();
        visitCounterService.registerVisit(uri);
    }
}