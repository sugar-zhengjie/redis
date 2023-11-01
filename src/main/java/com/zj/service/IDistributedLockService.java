package com.zj.service;

import com.zj.annotation.DistributedLock;
import com.zj.distributedLock.IDistributedLock;
import com.zj.distributedLock.ILock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/11/1 14:42
 */
@Slf4j
@Service
public class IDistributedLockService {

    @Autowired
    private IDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public Boolean test(){
        ILock lock = null;
        try{
            lock = distributedLock.lock("your key or id",10L, TimeUnit.SECONDS,false);
            // 执行业务代码
        }catch (Exception e){
            log.error("保存异常", e);
        }finally {
            if(Objects.nonNull(lock)){
                distributedLock.unLock(lock);
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean test2(){
        // try-with-resources 语法糖自动释放锁
        try(ILock lock = distributedLock.lock("key",10L, TimeUnit.SECONDS, false)) {
            if(Objects.isNull(lock)){
                throw new Exception("Duplicate request for method still in process");
            }
            // 业务代码

        } catch (Exception e) {
            log.error("异常", e);
        }
        return Boolean.TRUE;

    }

    //@DistributedLock(key = "#dto.sku + '-' + #dto.skuId", lockTime = 10L, keyPrefix = "sku-")
    @DistributedLock(key = "#dto.sku + '-' + #dto.key", lockTime = 10L, keyPrefix = "test-")
    @Transactional(rollbackFor = Exception.class)
    public Boolean test3(String key){
        return true;
    }
}
