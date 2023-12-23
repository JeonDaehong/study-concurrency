package com.example.studyconcurrency.facade;

import com.example.studyconcurrency.repository.RedisLockRepository;
import com.example.studyconcurrency.service.StockService;
import org.springframework.stereotype.Component;

/**
 * Lettuce 를 활용하여 분산 락을 구현하는 장점은, DB의 Named Lock에 비해 구현이 쉽다가 있다.
 * 그러나 단점은 스핀 락 방식이므로, 레디스에 부하를 줄 수 있다.
 * 그래서 Thread sleep 을 통해 요청 텀을 두어야 한다.
 *  - 스핀 락 방식 : 특정 조건이 충족 될 때까지 계속해서 루프를 돌며 기다리는 방식
 */
@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while ( !redisLockRepository.lock(id) ) {
            Thread.sleep(100);
        }
        try {
            stockService.decrease(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}
