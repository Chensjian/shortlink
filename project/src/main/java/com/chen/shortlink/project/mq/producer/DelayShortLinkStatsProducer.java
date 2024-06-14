package com.chen.shortlink.project.mq.producer;

import com.chen.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.chen.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟消费短链接统计发送者
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;

    /**
     * 发送延迟消费短链统计
     */
    public void send(ShortLinkStatsRecordDTO statsRecord){
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(statsRecord,5, TimeUnit.SECONDS);
    }
}
