package com.zj.aspect;

import com.zj.annotation.CustomCacheEvict;
import com.zj.annotation.CustomCacheable;
import com.zj.util.JsonUtil;
import com.zj.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 11:19
 */
@Slf4j
@Aspect
@Component
public class CustomCacheAspect {

    @Autowired
    private RedisUtil redisUtil;

    // 通过配置文件控制是否开启redis缓存
    @Value("${customCache.enable:true}")
    private boolean cacheEnable;

    @Pointcut("@annotation(com.zj.annotation.CustomCacheable)")
    public void cacheableAspect(){
    }

    @Pointcut("@annotation(com.zj.annotation.CustomCacheEvict)")
    public void cacheEvictAspect(){
    }

    @Around("cacheableAspect()")
    public Object cache(JoinPoint point) throws Throwable{
        // 获取方法的签名信息
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        // 方法连接点转换为可执行连接点
        ProceedingJoinPoint joinPoint = (ProceedingJoinPoint) point;
        // 得到被切面修饰的方法的参数列表
        Object[] args = joinPoint.getArgs();
        // 定义最终返回结果
        Object result = null;
        // 获取被代理方法
        Method method = methodSignature.getMethod();
        // 获取被代理方法的返回值类型
        Class<?> returnType = methodSignature.getReturnType();
        // 获取被代理方法上的注解
        CustomCacheable customCacheable = method.getAnnotation(CustomCacheable.class);
        Class<?> elementClass = customCacheable.type();
        String methodKey = customCacheable.methodKey();
        String fieldKey = customCacheable.fieldKey();
        // methodKey规则与注解规则一致 类名.方法名
        if(StringUtils.isEmpty(methodKey)){
            methodKey = point.getTarget().getClass().getSimpleName() + "." + point.getSignature().getName();
        }
        TimeUnit timeUnit = customCacheable.timeUnit();
        long expire = customCacheable.expire();
        // 得到redis的唯一key
        String redisKey = this.buildRedisKey(point, fieldKey, methodKey);
        // 从redis中获取数据
        String cacheValue = (String) redisUtil.get(redisKey);

        if(StringUtils.isEmpty(cacheValue)){
            // redis中没有数据，直接业务方法获取数据
            result = joinPoint.proceed(args);
            // 将结果序列化后存入redis
            if(result != null){
                String resultStr = JsonUtil.serialize(result);
                redisUtil.setIfAbsent(redisKey,resultStr,expire,timeUnit);
            }else{
                // 当结果为null时默认值，防止缓存被击穿
                if (elementClass == Exception.class) {
                    redisUtil.setIfAbsent(redisKey, "{}", expire, timeUnit);
                } else {
                    redisUtil.setIfAbsent(redisKey, "[]", expire, timeUnit);
                }
                log.info("将结果序列化后放入redis，redisKey={}", redisKey);
            }
        }else{
            log.info("命中缓存,redisKey={}",redisKey);
            // 如果redis中可以取到数据,将redis中获取到的数据反序列化后返回
            if (elementClass == Exception.class) {
                result = JsonUtil.deserialize(cacheValue, returnType);
            } else {
                result = JsonUtil.deserialize(cacheValue, returnType, elementClass);
            }
        }
        return result;
    }

    /**
     * 在方法调用前清除缓存，然后调用业务方法
     *
     * @param point
     * @throws Throwable
     */
    @Around("cacheEvictAspect()")
    public Object evictCache(JoinPoint point) throws Throwable {
        ProceedingJoinPoint joinPoint = (ProceedingJoinPoint) point;
        // 得到被切面修饰的方法的参数列表
        Object[] args = joinPoint.getArgs();
        // 得到被代理的方法上的注解
        MethodSignature methodSignature = ((MethodSignature) point.getSignature());
        // 得到被代理的方法
        Method method = methodSignature.getMethod();

        CustomCacheEvict ev = method.getAnnotation(CustomCacheEvict.class);
        String key = ev.keyField();
        // 从注解中获取缓存名称
        String value = ev.cachePutValue();

        String[] vs = value.split(",");
        for (String item : vs) {
            // 获取Redis中的key
            String redisKey = this.buildRedisKey(point, key, item);
            // 清除对应缓存
            boolean flag = redisUtil.delKey(redisKey);
            log.info("删除缓存,redisKey={},flag={}", redisKey, flag);
        }

        return joinPoint.proceed(args);
    }


    /**
     * 构造redis key
     */
    private String buildRedisKey(JoinPoint joinPoint,String fieldKey,String methodKey){
        MethodSignature methodSignature = ((MethodSignature) joinPoint.getSignature());
        ProceedingJoinPoint proceedingJoinPoint = (ProceedingJoinPoint) joinPoint;
        // 得到被切面修饰的方法的参数列表
        Object[] args = proceedingJoinPoint.getArgs();
        // 得到被代理的方法
        Method method = methodSignature.getMethod();

        // 获得经过el解析后的key值 使用参数来构建，参数中包含客户id则区分了不同客户
        String elKey = this.parseRedisKey(fieldKey, method, args);
        // redis 中的key值,确保唯一
        return "serviceName:cache:" + methodKey + ":" + elKey;
    }

    /**
     * 获取缓存的key
     * key 定义在注解上，支持SPEL表达式
     */
    private String parseRedisKey(String key,Method method,Object[] args){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        // 获取被拦截方法参数名列表（使用Spring支持类库）
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNameArr = u.getParameterNames(method);

        // 使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        // SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 将参数放入SPEL上下文中
        if (paramNameArr != null) {
            for (int i = 0; i < paramNameArr.length; i++) {
                context.setVariable(paramNameArr[i], args[i]);
            }
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }
}
