package com.chen.shortlink.admin.aspenct;

import cn.hutool.http.server.HttpServerRequest;
import com.alibaba.fastjson2.JSON;
import com.chen.shortlink.admin.annotation.MyLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 切面处理类，打印操作日志
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    //为了记录方法的执行时间
    ThreadLocal<Long> startTime=new ThreadLocal<>();

//    @Pointcut("@annotation(MyLog)")
//    @Pointcut("execution(public * com.chen.shortlink.admin.annotation..*.*(..))")//从controller切入
    public void logPointCut(){

    }
//    @Before("logPointCut()")
    public void beforeMethod(JoinPoint joinPoint){
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 设置操作异常切入点记录异常日志，扫描所有controller包下的操作
     */
    @Pointcut(value = "execution(* com.chen.shortlink.admin.controller..*.*(..))")
    public void exceptionPointCut(){

    }

    @Around(value = "@annotation(myLog)")
    public Object around(ProceedingJoinPoint joinPoint,MyLog myLog){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        log.info("开始执行"+methodName+"方法");
        try {
            Object proceed = joinPoint.proceed();
            log.info(methodName+"方法执行完成");
            return proceed;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
//    /**
//     * 正常返回通知，拦截用户操作日志，连接点正常执行完成之后执行，如果连接点抛出异常，则不会执行
//     * @param joinPoint 切入点
//     * @param result 返回值
//     */
//    @AfterReturning(value = "@@annotation(MyLog)",returning = "result")
//    public void printLog(JoinPoint joinPoint,Object result){
//        //获取requestAttributes
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        //从requestAttributes中获取HttpServletRequest的信息
//        HttpServerRequest httpServerRequest = (HttpServerRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
//        try{
//            //从切面切入点处通过反射获取切入点的方法
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            //获取切入点所在方法
//            Method method = signature.getMethod();
//            //获取操作
//            MyLog myLog = method.getAnnotation(MyLog.class);
//
//            if(myLog==null){
//                return;
//            }
//            String className = joinPoint.getTarget().getClass().getName();
//            String methodName = className+"."+method.getName()+"()";
//            //将参数转换为字符串
//            String params = parametersToString(joinPoint.getArgs());
//            //打印
//            log.info("url:"+httpServerRequest.getURI()+"请求方式:"+httpServerRequest.getMethod()+"调用了"+methodName+"方法"+"请求参数"+params);
//
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
    private String parametersToString(Object[] paramArray){
        String params="";
        if(paramArray==null){
            return params;
        }
        for(Object o:paramArray){
            if(o!=null){
                Object json = JSON.toJSON(o);
                params+=json.toString()+"";
            }
        }
        return params.trim();
    }


}
