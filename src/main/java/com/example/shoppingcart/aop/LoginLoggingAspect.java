package com.example.shoppingcart.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoginLoggingAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.example.ShoppingCart.Controllers.UserController.login(..))")
    public Object logBeforeLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint, "Controllers");
    }
;
    private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();

        log.info("[{}] {}.{} - Started - Arguments: {}",
                layer, className, methodName, Arrays.toString(arguments));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("[{}] {}.{} - Completed - Duration: {}ms - Result: {}",
                    layer, className, methodName, (endTime - startTime), result);
            return result;
        } catch (Exception e) {
            log.error("[{}] {}.{} - Failed - Error: {}",
                    layer, className, methodName, e.getMessage(), e);
            throw e;
        }
    }
}