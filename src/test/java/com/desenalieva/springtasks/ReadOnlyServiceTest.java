package com.desenalieva.springtasks;

import com.desenalieva.springtasks.entities.Book;
import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.BookRepository;
import com.desenalieva.springtasks.repositories.SerialRepository;
import com.desenalieva.springtasks.services.ReadOnlyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReadOnlyServiceTest {
    @Autowired
    private ReadOnlyService readOnlyService;

    @Autowired
    private SerialRepository serialRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setup() {
        serialRepository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        serialRepository.deleteAll();
    }

    /**
     * Проверяем, что вызов readOnlyService.readOnlyMethodWithFlush()
     * (данный метод аннотирован @Transactional(readOnly = true), в нем создается и сохраняется новый объект Serial,
     * а затем происходит вызов flush())
     * не вызывает исключения и новый объект сохраняется в бд
     */
    @Test
    void testReadOnlyMethod() {
        assertEquals(0, serialRepository.count()); // перед вызовом метода в бд в таблице serial нет объектов
        assertDoesNotThrow(() -> readOnlyService.readOnlyMethodWithFlush()); // вызов метода не вызывает исключения
        assertEquals(1, serialRepository.count()); // после вызова метода в бд в таблице serial появился объект
        assertNotNull(serialRepository.findById(60L));
    }

    /**
     * Проверка кейса:
     * вызываем changeSerialAndBookName1() аннотированный @Transactional(readOnly = true)
     * в нем изменяется название сериала и вызывается changeBookNameWithReadOnlyFalse()
     * аннотированный @Transactional(propagation = REQUIRES_NEW), в котором происходит изменение названия книги
     * Итог:
     * - изменение названия сериала НЕ сохранилось в бд
     * - изменение названия книги сохранилось в бд
     */
    @Test
    void testMyMethod3() {
        createSerialAndBook();

        readOnlyService.changeSerialAndBookName1(1L, "NewSerialName", 1L, "NewBookName");

        Optional<Serial> optSerial = serialRepository.findById(1L);
        assertTrue(optSerial.isPresent());
        Serial serial = optSerial.get();
        assertEquals("OldSerialName", serial.getName()); // изменение названия сериала НЕ сохранилось в бд

        Optional<Book> optBook = bookRepository.findById(1L);
        assertTrue(optBook.isPresent());
        Book book = optBook.get();
        assertEquals("NewBookName", book.getName()); // изменение названия книги сохранилось в бд
    }

    /**
     * Проверка кейса:
     * вызываем changeSerialAndBookName2() аннотированный @Transactional()
     * в нем изменяется название сериала и вызывается changeBookNameWithReadOnlyTrue()
     * аннотированный @Transactional(readOnly = true, propagation = REQUIRES_NEW),
     * в котором происходит изменение названия книги
     * Итог:
     * - изменение названия сериала сохранилось в бд
     * - изменение названия книги НЕ сохранилось в бд
     */
    @Test
    void testMyMethod4() {
        createSerialAndBook();

        readOnlyService.changeSerialAndBookName2(1L, "NewSerialName", 1L, "NewBookName");

        Optional<Serial> optSerial = serialRepository.findById(1L);
        assertTrue(optSerial.isPresent());
        Serial serial = optSerial.get();
        assertEquals("NewSerialName", serial.getName()); // изменения сериала сохранились в бд

        Optional<Book> optBook = bookRepository.findById(1L);
        assertTrue(optBook.isPresent());
        Book book = optBook.get();
        assertEquals("OldBookName", book.getName()); // изменения книги НЕ сохранились в бд
    }

    private void createSerialAndBook() {
        transactionTemplate.executeWithoutResult((status) -> {
            serialRepository.save(new Serial(1L, "OldSerialName", 5));
            bookRepository.save(new Book(1L, "OldBookName", "Author"));
        });
    }

    /**
     * Проверка кейса:
     * вызываем changeSerialNameAndRating1() аннотированный @Transactional(readOnly = true)
     * в нем изменяется название сериала и вызывается changeSerialRatingWithReadOnlyFalse()
     * аннотированный @Transactional(propagation = REQUIRES_NEW), в котором происходит изменение рейтинга сериала
     * Итог:
     * - изменение названия сериала НЕ сохранилось в бд
     * - изменение рейтинга сериала сохранилось в бд
     */
    @Test
    void testMyMethod5() {
        transactionTemplate.executeWithoutResult(status -> serialRepository.save(new Serial(1L, "OldSerialName", 5)));

        readOnlyService.changeSerialNameAndRating1(1L, "NewSerialName", 10);

        Optional<Serial> optSerial = serialRepository.findById(1L);
        assertTrue(optSerial.isPresent());
        Serial serial = optSerial.get();
        assertEquals("OldSerialName", serial.getName()); // изменение названия сериала НЕ сохранилось в бд
        assertEquals(10, serial.getRating()); // изменение рейтинга сериала сохранилось в бд

    }

    /**
     * Проверка кейса:
     * вызываем changeSerialNameAndRating2() аннотированный @Transactional()
     * в нем изменяется название сериала и вызывается changeSerialRatingWithReadOnlyTrue()
     * аннотированный @Transactional(readOnly = true, propagation = REQUIRES_NEW),
     * в котором происходит изменение рейтинга сериала
     * Итог:
     * - изменение названия сериала сохранилось в бд
     * - изменение рейтинга сериала НЕ сохранилось в бд
     */
    @Test
    void testMyMethod6() {
        transactionTemplate.executeWithoutResult(status -> serialRepository.save(new Serial(1L, "OldSerialName", 5)));

        readOnlyService.changeSerialNameAndRating2(1L, "NewSerialName", 10);

        Optional<Serial> optSerial = serialRepository.findById(1L);
        assertTrue(optSerial.isPresent());
        Serial serial = optSerial.get();
        assertEquals("NewSerialName", serial.getName()); // изменение названия сериала сохранилось в бд
        assertEquals(5, serial.getRating()); // изменение рейтинга сериала НЕ сохранилось в бд
    }
}