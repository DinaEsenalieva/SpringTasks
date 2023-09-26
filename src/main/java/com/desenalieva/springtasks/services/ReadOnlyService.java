package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Сервис для тестирования метода аннотированного @Transactional(readOnly = true)
 */
@Service
@RequiredArgsConstructor
public class ReadOnlyService {
    /**
     * Репозиторий сериалов.
     */
    private final SerialRepository serialRepository;

    /**
     * EntityManager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Метод аннотированный @Transactional(readOnly = true) внутри которого вызываем flush()
     */
    @Transactional(readOnly = true)
    public void readOnlyMethodWithFlush() {
        Serial serial = new Serial(60L, "Read Only", 9);
        serialRepository.save(serial);
        entityManager.flush();
    }
}