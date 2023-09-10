package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Сервис для тестирования аннотации @Transactional из пакета javax.transaction.
 */
@Service
@RequiredArgsConstructor
public class JavaTransactionService {

    /**
     * Репозиторий сериалов.
     */
    private final SerialRepository serialRepository;

    /**
     * Вспомогательный сервис.
     */
    private final HelperService helperService;

    /**
     * Транзакционный метод без указания isolation (т.к. у java @Transactional такого параметра нет).
     * Java @Transactional всегда использует isolation = Default (обычно это READ_COMMITTED).
     * @return кол-во записей в таблице serial
     */
    @Transactional
    public long transactionWithDefaultIsolation() {
        long countBefore = serialRepository.count(); // 0
        helperService.saveNewSerialInNewTransaction();
        long countAfter = serialRepository.count(); // 1,
        return countAfter;
    }

    /**
     * Транзакционный метод без указания readOnly (т.к. у java @Transactional такого параметра нет).
     */
    @Transactional
    public void transactionWithoutReadOnly() {
        serialRepository.save(new Serial(1L));
    }

    /**
     * Транзакционный метод без указания timeout (т.к. у java @Transactional такого параметра нет).
     */
    @Transactional
    public void transactionWithoutTimeout() {
        serialRepository.count();
    }
}
