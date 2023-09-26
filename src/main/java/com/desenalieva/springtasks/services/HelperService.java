package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.entities.Book;
import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.BookRepository;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Вспомогательный сервис.
 */
@Service
@RequiredArgsConstructor
public class HelperService {
    private final SerialRepository serialRepository;

    private final BookRepository bookRepository;

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

    @Transactional(propagation = REQUIRES_NEW)
    public void changeBookNameWithReadOnlyFalse(Long id, String nameNew) {
        changeBookName(id, nameNew);
    }

    @Transactional(readOnly = true, propagation = REQUIRES_NEW)
    public void changeBookNameWithReadOnlyTrue(Long id, String newName) {
        changeBookName(id, newName);
    }

    private void changeBookName(Long id, String newName) {
        Optional<Book> optBook = bookRepository.findById(id);
        optBook.ifPresent(book -> book.setName(newName));
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void changeSerialRatingWithReadOnlyFalse(Long id, Integer newRating) {
        changeSerialRating(id, newRating);
    }

    @Transactional(readOnly = true, propagation = REQUIRES_NEW)
    public void changeSerialRatingWithReadOnlyTrue(Long id, Integer newRating) {
        changeSerialRating(id, newRating);
    }

    private void changeSerialRating(Long id, Integer newRating) {
        Optional<Serial> optSerial = serialRepository.findById(id);
        optSerial.ifPresent(serial -> serial.setRating(newRating));
    }
}


