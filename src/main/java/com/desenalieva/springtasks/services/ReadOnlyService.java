package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

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
     * Вспомогательный сервис.
     */
    private final HelperService helperService;

    /**
     * Метод аннотированный @Transactional(readOnly = true) внутри которого вызываем flush()
     */
    @Transactional(readOnly = true)
    public void readOnlyMethodWithFlush() {
        Serial serial = new Serial(60L, "Read Only", 9);
        serialRepository.save(serial);
        entityManager.flush();
    }

    @Transactional(readOnly = true)
    public void changeSerialAndBookName1(Long serialId, String newSerialName, Long bookId, String newBookName) {
        changeSerialName(serialId, newSerialName);
        helperService.changeBookNameWithReadOnlyFalse(bookId, newBookName);
    }

    @Transactional()
    public void changeSerialAndBookName2(Long serialId, String newSerialName, Long bookId, String newBookName) {
        changeSerialName(serialId, newSerialName);
        helperService.changeBookNameWithReadOnlyTrue(bookId, newBookName);
    }

    @Transactional(readOnly = true)
    public void changeSerialNameAndRating1(Long id, String newName, Integer newRating) {
        changeSerialName(id, newName);
        helperService.changeSerialRatingWithReadOnlyFalse(id, newRating);
    }

    @Transactional()
    public void changeSerialNameAndRating2(Long id, String nameNew, Integer newRating) {
        changeSerialName(id, nameNew);
        helperService.changeSerialRatingWithReadOnlyTrue(id, newRating);
    }

    private void changeSerialName(Long id, String nameNew) {
        Optional<Serial> optSerial = serialRepository.findById(id);
        optSerial.ifPresent(serial -> serial.setName(nameNew));
    }
}