package com.zj.aspect;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.util.StringUtil;
import com.zj.entity.User;
import com.zj.annotation.AntiReplay;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 16:33
 */

@Aspect
@Slf4j
@Component
public class AntiReplayAspect {
    public static final String RESULT = JSON.toJSONString("请勿重复点击按钮");
    public static final String XREALIP = "X-Real-IP";
    private static final String OFF = "off";

    @Value("${AntiReplaySwitch: on}")
    String aSwitch;

    @Autowired
    private RedisLockRegistry redisLockRegistry;

    @Pointcut("@within(antiReplay)||@annotation(antiReplay)")
    public void pointcut(AntiReplay antiReplay) {
    }

    @Around(value = "pointcut(antiReplay)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, AntiReplay antiReplay) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        int tryLockTime = antiReplay.tryLockTime();
        String path = request.getRequestURI();
        HttpServletResponse response = attributes.getResponse();
        Object result = null;
        String name = antiReplay.key();
        //User user = ContextHolder.getContext().getUser();
        //实际项目中通过 Secure工具类获取或者请求头
        User user = new User();
        user.setId("1111111");
        user.setName("zj");
        user.setEmail("123456");
        if(StringUtil.isEmpty(name)){
            if (null != user && StringUtil.isNotEmpty(user.getEmail())) {
                name = user.getName()+user.getId();
            } else {
                //获取nginx转发之后的真实ip
                name = request.getHeader(XREALIP);
            }
        }
        String key = name + '-' + path;
        // 开关打开
        if(!OFF.equalsIgnoreCase(aSwitch)){
            //获取分布式锁
            Lock lock = redisLockRegistry.obtain(key);
            long s = System.currentTimeMillis();
            // tryLockTime 默认为0 不会等待锁变为可用，如果锁不可用，它将立即返回false，一个非阻塞的锁获取方式
            boolean isSuccess = lock.tryLock(tryLockTime, TimeUnit.MILLISECONDS);
            log.info("获取锁时间：[{}]", System.currentTimeMillis() - s);
            if (isSuccess) {
                log.info("获取锁 success, key = [{}ms]", key);
                // 获取锁成功, 执行进程
                try {
                    result = proceedingJoinPoint.proceed();
                } finally {
                    // 解锁
                    lock.unlock();
                    log.info("释放锁 success, key = [{}]", key);
                }
            } else {
                // 获取锁失败，认为是重复提交的请求
                log.info("获取锁失败 fail, key = [{}]", key);
                //直接返回前端数据
                sendResponse(response);
            }
        }else{
            result = proceedingJoinPoint.proceed();
        }
        return result;

    }

    private void sendResponse(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.append(RESULT);
        } catch (IOException e) {
            //
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


}
