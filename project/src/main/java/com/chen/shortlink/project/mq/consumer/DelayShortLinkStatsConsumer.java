package com.chen.shortlink.project.mq.consumer;

import com.chen.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.chen.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

import static com.chen.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 *  延迟记录短链接统计组件
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;

    @Override
    public void afterPropertiesSet() throws Exception {
        Executors.newSingleThreadExecutor(runnable -> {
            Thread thread=new Thread(runnable);
            thread.setName("delay_short-link_stats_consumer");
            thread.setDaemon(Boolean.TRUE);
            return thread;
        }).execute(()->{
            RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
            RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            for(;;){
                ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                if(statsRecord!=null){
                    shortLinkService.shortLinkStats(null,statsRecord);
                }
            }
        });
    }
}
