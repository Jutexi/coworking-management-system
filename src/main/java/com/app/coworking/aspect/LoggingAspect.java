package com.app.coworking.aspect;

import com.app.coworking.exception.ControllerInvocationException;
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

        try {
            Object result = joinPoint.proceed();

            // Логируем успешное завершение метода
            logger.info("Controller method {}.{}() completed successfully", className, methodName);

            return result;

        } catch (Exception e) {
            // Логируем исключение вместе со стеком
            logger.error("Error in {}.{}()", className, methodName, e);

            // Пробрасываем кастомное исключение с контекстом и оригинальным cause
            throw new ControllerInvocationException(
                    String.format("Error in controller method %s.%s()", className, methodName),
                    e
            );
        }
    }
}

