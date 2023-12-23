package com.example.studyconcurrency.service;

import com.example.studyconcurrency.domain.Stock;
import com.example.studyconcurrency.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * 이렇게 synchronized 를 활용해도, 동시성 문제가 해결되지 않았다.
     * 그 이유는 service 에 있는 @Transactional 의 동작 방식 때문이다.
     * Spring 에서는 @Transactional 어노테이션을 사용하면
     * 우리가 실행한 클래스를 Mapping 한 클래스를 새로 만들어서 실행한다.
     *      - 쉽게 이야기하면, StockService 안에 @Transactional 이 있으므로,
     *        private StockService stockService; 라는 필드를 보유한
     *        TransactionStockService 라는 걸 내부적으로 만들어서 얘를 실행시킨다.
     *      - 그리고 @Transactional 이 직접적으로 붙어 있는 메서드의
     *        시작 부분에 startTransaction();
     *        끝 부분에 endTransaction(); 이라는 메서드를 붙어 실행한다.
     *      - 여기서 문제가 발생한다.
     *         - Stock 에 있는 decrease 는 synchronized 덕분에 1개의 Thread 씩 접근을 하지만
     *           끝 부분에 있는 endTransaction(); 은 synchronized 범위 밖이므로, 얘가 실행되기 전에
     *           새로운 Thread 가 접근할 수 있다.
     *           근데 endTransaction(); 의 역할이, 문제가 없을 경우 실제 DB에 반영.. commit 해주는 역할이므로,
     *           여기에 겹쳐져서 동시성 문제가 발생하게 된다. ( 아직 commit 되기 전의 수량을 다른 Thread 에서 가져가 버림 )
     *      - 해결 하기 위해서는 @Transactional 을 주석처리하면 된다.
     *        그러나 이 방법은 사용을 권장하지 않는다.
     *
     * 또한 synchronized 는 하나의 프로세스에 대한 Thread 만 순차적으로 실행시켜주므로,
     * 여러대의 서버를 이용할 경우 동시성을 보장해주지 않는다.
     * 그래서 실질적인 서버 운영 단계에서는 synchronized 는 잘 사용하지 않는다.
     *
     */
    // @Transactional synchronized 를 사용하려면 @Transactional 어노테이션을 삭제해야 한다.
    @Transactional
    public synchronized void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

    /**
     * 부모의 Transactional 과 별도로 실행되어야 하기 때문에 Propagation 을 설정해 줌.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseNamedLock(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}
