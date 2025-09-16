package com.app.coworking.aspect;

import com.app.coworking.exception.ControllerInvocationException;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.app.coworking.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // Логируем только вызов метода и его аргументы
        if (org.slf4j.LoggerFactory.getLogger(className).isInfoEnabled()) {
            org.slf4j.LoggerFactory.getLogger(className)
                    .info("Controller method {}.{}() called with args: {}",
                            className, methodName, Arrays.toString(args));
        }

        try {
            Object result = joinPoint.proceed();

            // Логируем успешное завершение метода
            org.slf4j.LoggerFactory.getLogger(className)
                    .info("Controller method {}.{}() completed successfully",
                            className, methodName);

            return result;

        } catch (Exception e) {
            // Не логируем здесь, а просто пробрасываем кастомное исключение
            throw new ControllerInvocationException(
                    String.format("Error in controller method %s.%s()", className, methodName),
                    e
            );
        }
    }
}
