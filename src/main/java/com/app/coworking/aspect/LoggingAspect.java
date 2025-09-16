package com.app.coworking.aspect;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.app.coworking.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        if (logger.isInfoEnabled()) {
            logger.info("Controller method {}.{}() called with args: {}",
                    className, methodName, Arrays.toString(args));
        }

        Object result = joinPoint.proceed();
        logger.info("Controller method {}.{}() completed successfully",
                className, methodName);
        return result;
    }
}
