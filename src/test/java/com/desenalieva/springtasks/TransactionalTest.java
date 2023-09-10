package com.desenalieva.springtasks;

import com.desenalieva.springtasks.repositories.SerialRepository;
import com.desenalieva.springtasks.services.JavaTransactionService;
import com.desenalieva.springtasks.services.SpringTransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionTimedOutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransactionalTest {
    @Autowired
    private SerialRepository serialRepository;

    @Autowired
    private JavaTransactionService javaTransactionService;

    @Autowired
    private SpringTransactionService springTransactionService;

    @AfterEach
    void cleanup() {
        serialRepository.deleteAll();
    }

    /**
     * У spring @Transactional есть возможность указать параметр isolation, который отвечает за изоляцию транзакций.
     * В данном тесте рассмотрен следующий кейс:
     * 1) Первая транзакция запрашивает кол-во элементов в таблице (получает ноль);
     * 2) Вторая транзакция сохраняет новый элемент;
     * 3) Первая транзакция снова запрашивает кол-во элементов в таблице и возвращает полученное значение.
     * Результат следующий:
     * - У spring @Transactional указан уровень изоляции Serializable - следовательно первая транзакция не видит новые объекты
     * в таблице (и мы ожидаем что вернется ноль).
     * - У java @Transactional нет возможности изменить уровень изоляции и используется уровень
     * DEFAULT (обычно это READ_COMMITED) - следовательно первая транзакция видит изменения сделанные второй (ожидаем что вернется 1).
     */
    @Test
    void testIsolationParameter() {
        assertEquals(0, springTransactionService.transactionWithSerializableIsolation());
        serialRepository.deleteAll();
        assertEquals(1, javaTransactionService.transactionWithDefaultIsolation());
    }

    /**
     * У spring @Transactional есть возможность указать параметр readOnly.
     * Если его установить в true, то flush() происходить не будет.
     * В данном тесте рассмотрен следующий кейс:
     * 1) В методе с @Transactional происходит сохранение новой сущности Serial.
     * 2) Проверяем кол-во записей в бд после окончания транзакции.
     * Результат следующий:
     * - У spring @Transactional параметр readOnly = true - следовательно изменения сделанные в транзакции не попали в бд,
     * и кол-во элементов в таблице не изменилось.
     * - У java @Transactional нет параметра readOnly - следовательно изменения сделанные в транзакции попали в бд,
     * и кол-во элементов в таблице увеличилось.
     */
    @Test
    void testReadOnlyParameter() {
        assertEquals(0, serialRepository.count());
        springTransactionService.transactionWithReadOnly();
        assertEquals(0, serialRepository.count());
        javaTransactionService.transactionWithoutReadOnly();
        assertEquals(1, serialRepository.count());
    }

    /**
     * У spring @Transactional есть возможность указать параметр timeout/timeoutString.
     * Этот параметр отвечает за ограничение времени работы транзакции.
     * Принимает int (или String для timeoutString), и если транзакция будет длиться дольше заданного времени (в секундах),
     * то выбросится TransactionTimedOutException.
     * В данном тесте рассмотрен следующий кейс:
     * Выполняется транзакционный метод.
     * - У spring @Transactional установлен timeout = 0, и вызов данного метода
     * будет выдавать ошибку.
     * - У java @Transactional такой параметр установить нельзя, следовательно, и время выполнения транзакций
     * контролировать нельзя.
     */
    @Test
    void testTimeoutParameter() {
        assertThrows(TransactionTimedOutException.class, () -> springTransactionService.transactionWithTimeout());
        assertDoesNotThrow(() -> javaTransactionService.transactionWithoutTimeout());
    }
}
