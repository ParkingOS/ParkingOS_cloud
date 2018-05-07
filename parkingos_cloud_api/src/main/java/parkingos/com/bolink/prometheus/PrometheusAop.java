package parkingos.com.bolink.prometheus;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 拦截controller
 * Created by waynelu on 2018/3/29.
 */
@Aspect
@Order(1)
@Component

public class PrometheusAop {
    private static final Counter requestTotal = Counter.build()
            .name("http_requests_total")
            .labelNames("method", "handler")
            .help("http_requests_total").register();

    private static final Summary responseTimeInMs = Summary
            .build()
            .name("http_response_time_milliseconds")
            .labelNames("method", "handler")
            .help("Request completed time in milliseconds")
            .register();

    //Controller aop
    @Pointcut("execution(public * parkingos.com.bolink.actions.*.*(..))")
    public void timing() {
    }

    @Around("timing()")
    public Object aroundTiming(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName(); //获取方法名称
        requestTotal.labels(methodName, "").inc();
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long tTime = System.currentTimeMillis() - startTime;
            responseTimeInMs.labels(methodName, "").observe(tTime);
        }

    }

}
