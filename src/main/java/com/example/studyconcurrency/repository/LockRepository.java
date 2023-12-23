package com.example.studyconcurrency.repository;

import com.example.studyconcurrency.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Named Lock 을 할 건데,
 * 실제 현업에서는 JpaRepository<?, ?>  이 부분에 별도의 JDBC 를 넣지, 이렇게 Entity 를 바로 넣지 않는다.
 * 그리고 현업에서는 Named Lock 을 할 때 커넥션 풀 때문에 데이터 소스를 분리하여 사용한다.
 */
public interface LockRepository extends JpaRepository<Stock, Long> {

    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);

}
