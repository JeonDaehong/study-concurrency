package com.example.studyconcurrency.repository;

import com.example.studyconcurrency.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 스레드간의 충돌이 빈번하게 일어나면 낙관적 락보다는, 비관적 락이 좋을 수 있다.
     * ( 동시에 수정을 Update 를 하는 일이 빈번하게 일어나는 가 ? )
     * 반대로 낙관적 락은 충돌이 거의 발생하지 않지만, 발생할 경우를 대비하는 방식이라고 생각할 수 있다.
     * 모든 것에 비관적 락을 사용한다면 성능저하가 생길 수 있는데,
     * 이렇게 상황에 따라 다르게 락을 걸면 성능저하를 막을 수 있다.
     * 낙관적 락의 단점은, 버전에 따른 재시도 로직을 직접 구현해줘야 한다는 것이고,
     * 재시도 로직 때문에 너무 빈번한 충돌이 발생하는 로직에 사용하면 오히려 성능이 저하될 수 있다.
     */

    // 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);
    
    // 낙관적 락
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);

}
