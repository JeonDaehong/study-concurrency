package com.example.studyconcurrency.facade;

import com.example.studyconcurrency.repository.LockRepository;
import com.example.studyconcurrency.repository.StockRepository;
import com.example.studyconcurrency.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 네임드 락은 분산락을 구현할 때 사용한다. ( Redis가 아닌 순수 mySql의 분산락 )
 * 그리고 네임드 락은 비관적 락과는 다르게 TimeOut을 손쉽게 구현할 수 있다.
 * 데이터 삽입시 정합성을 맞춰야 할 때에도 네임드 락을 많이 사용한다. ( 포인트 전환 로직 )
 * 하지만 트랜잭션 종료 시 락 해제, 세션 관리를 잘 해줘야 하기 때문에 잘 사용해줘야 하고,
 * 실제 현업에서 구현시 로직이 복잡해 질 수 있음.
 *
 * 그래서 분산 락을 사용할 때는 Redis의 Lettuce를 활용하여 많이 구현 함.
 * Redis를 사용할 때는 세션관리를 신경쓰지 않아도 됨.
 */
@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decreaseNamedLock(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }

}
