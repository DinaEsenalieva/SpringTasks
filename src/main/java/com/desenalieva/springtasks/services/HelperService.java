package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Вспомогательный сервис.
 */
@Service
@RequiredArgsConstructor
public class HelperService {
    private final SerialRepository serialRepository;

    @Transactional
    public void helpMethod1() {
        throw new RecoverableDataAccessException("Message");
    }

    @Transactional(noRollbackFor = RecoverableDataAccessException.class)
    public void helpMethod2() {
        throw new RecoverableDataAccessException("Message");
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void helpMethod3() {
        throw new RecoverableDataAccessException("Message");
    }

    /**
     * Сохраняет сущность в новой транзакции.
     */
    @Transactional(propagation = REQUIRES_NEW)
    public void saveNewSerialInNewTransaction() {
        serialRepository.save(new Serial(1L));
    }
}


