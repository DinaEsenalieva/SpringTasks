package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для тестирования аннотации @Transactional из пакета org.springframework.transaction.annotation.
 */
@Service
@RequiredArgsConstructor
public class SpringTransactionService {

    /**
     * Репозиторий сущности Serial.
     */
    private final SerialRepository serialRepository;

    /**
     * Вспомогательный сервис.
     */
    private final HelperService helperService;

    /**
     * Транзакционный метод с isolation = SERIALIZABLE.
     * @return кол-во записей в таблице serial
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long transactionWithSerializableIsolation() {
        long countBefore = serialRepository.count(); // 0
        helperService.saveNewSerialInNewTransaction();
        long countAfter = serialRepository.count(); // Все еще 0, так как SERIALIZABLE
        return countAfter;
    }

    /**
     * Транзакционный метод c readOnly = true.
     */
    @Transactional(readOnly = true)
    public void transactionWithReadOnly() {
        serialRepository.save(new Serial(1L)); // Изменения не попадут в бд так как readOnly = true
    }

    /**
     * Транзакционный метод c timeout = 0.
     * Будет всегда выбрасывать TransactionTimedOutException.
     */
    @Transactional(timeout = 0)
    public void transactionWithTimeout() {
        serialRepository.count();
    }
}
