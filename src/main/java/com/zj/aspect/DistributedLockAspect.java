package com.zj.aspect;

import com.zj.annotation.DistributedLock;
import com.zj.distributedLock.IDistributedLock;
import com.zj.distributedLock.ILock;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/11/1 14:58
 */
@Aspect
@Slf4j
public class DistributedLockAspect {

    @Resource
    private IDistributedLock distributedLock;

    /**
     * SpEL表达式解析
     */
    private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    /**
     * 用于获取方法参数名字
     */
    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(com.zj.annotation.DistributedLock)")
    public void distributorLock() {
    }

    @Around("distributorLock()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 获取DistributedLock
        DistributedLock distributedLock = this.getDistributedLock(pjp);
        // 获取 lockKey
        String lockKey = this.getLockKey(pjp, distributedLock);
        ILock lockObj = null;
        try {
            // 加锁，tryLok = true,并且tryTime > 0时，尝试获取锁，获取不到超时异常
            if (distributedLock.tryLok()) {
                if(distributedLock.tryTime() <= 0){
                    throw new Exception("tryTime must be greater than 0");
                }
                lockObj = this.distributedLock.tryLock(lockKey, distributedLock.tryTime(), distributedLock.lockTime(), distributedLock.unit(), distributedLock.fair());
            } else {
                lockObj = this.distributedLock.lock(lockKey, distributedLock.lockTime(), distributedLock.unit(), distributedLock.fair());
            }

            if (Objects.isNull(lockObj)) {
                throw new Exception("Duplicate request for method still in process");
            }

            return pjp.proceed();
        } finally {
            // 解锁
            this.unLock(lockObj);
        }
    }

    /**
     * @param pjp
     * @return
     * @throws NoSuchMethodException
     */
    private DistributedLock getDistributedLock(ProceedingJoinPoint pjp) throws NoSuchMethodException {
        String methodName = pjp.getSignature().getName();
        Class<?> clazz = pjp.getTarget().getClass();
        Class<?>[] par = ((MethodSignature) pjp.getSignature()).getParameterTypes();
        Method lockMethod = clazz.getMethod(methodName, par);
        return lockMethod.getAnnotation(DistributedLock.class);
    }

    /**
     * 解锁
     *
     * @param lockObj
     */
    private void unLock(ILock lockObj) {
        if (Objects.isNull(lockObj)) {
            return;
        }

        try {
            this.distributedLock.unLock(lockObj);
        } catch (Exception e) {
            log.error("分布式锁解锁异常", e);
        }
    }

    /**
     * 获取 lockKey
     *
     * @param pjp
     * @param distributedLock
     * @return
     */
    private String getLockKey(ProceedingJoinPoint pjp, DistributedLock distributedLock) throws Exception {
        String lockKey = distributedLock.key();
        String keyPrefix = distributedLock.keyPrefix();
        if (StringUtils.isBlank(lockKey)) {
            throw new Exception("Lok key cannot be empty");
        }
        if (lockKey.contains("#")) {
            this.checkSpEL(lockKey);
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            // 获取方法参数值
            Object[] args = pjp.getArgs();
            lockKey = getValBySpEL(lockKey, methodSignature, args);
        }
        lockKey = StringUtils.isBlank(keyPrefix) ? lockKey : keyPrefix + lockKey;
        return lockKey;
    }

    /**
     * 解析spEL表达式
     *
     * @param spEL
     * @param methodSignature
     * @param args
     * @return
     */
    private String getValBySpEL(String spEL, MethodSignature methodSignature, Object[] args) throws Exception {
        // 获取方法形参名数组
        String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
        if (paramNames == null || paramNames.length < 1) {
            throw new Exception("Lok key cannot be empty");
        }
        Expression expression = spelExpressionParser.parseExpression(spEL);
        // spring的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 给上下文赋值
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return expression.getValue(context).toString();
    }

    /**
     * SpEL 表达式校验
     *
     * @param spEL
     * @return
     */
    private void checkSpEL(String spEL) throws Exception {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            parser.parseExpression(spEL, new TemplateParserContext());
        } catch (Exception e) {
            log.error("spEL表达式解析异常", e);
            throw new Exception("Invalid SpEL expression [" + spEL + "]");
        }
    }

}