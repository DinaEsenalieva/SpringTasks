package com.desenalieva.springtasks;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import com.desenalieva.springtasks.services.CustomSerialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CustomSerialServiceTest {
    @Autowired
    private CustomSerialService customSerialService;

    @Autowired
    private SerialRepository serialRepository;

    @BeforeEach
    void setup() {
        serialRepository.deleteAll();
        serialRepository.save(new Serial(1L, "Serial", 5));
    }

    @AfterEach
    void cleanup() {
        serialRepository.deleteAll();
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Внутри транзакционного метода updateRating1, возникает исключение RecoverableDataAccessException.
     * <br>
     * Так как мы НЕ перехватываем исключение И у аннотации @Transactional НЕ задан параметр
     * noRollbackFor = RecoverableDataAccessException.class, транзакция помечается как rollbackOnly и откатывается
     *
     */
    @Test
    void testUpdateRating1() {
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating1(1L, 10));
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Внутри транзакционного метода updateRating2, возникает исключение RecoverableDataAccessException.
     * <br>
     * Так как мы перехватываем исключение, транзакция НЕ помечается как rollbackOnly и НЕ откатывается
     *
     */
    @Test
    void testUpdateRating2() {
        // Проверяем, что изначально у сериала c id = 1, рейтинг = 5
        Optional<Serial> serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(5, serial.get().getRating());
        // Проверяем, что выполнение updateRating2 НЕ вызывает исключение
        assertDoesNotThrow(() -> customSerialService.updateRating2(1L, 10));
        // Проверяем, что транзакция была закоммичена (у сериала c id = 1, рейтинг изменился и стал = 10)
        serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(10, serial.get().getRating());
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Внутри транзакционного метода updateRating3, возникает исключение RecoverableDataAccessException.
     * <br>
     * мы НЕ перехватываем исключение, НО у аннотации @Transactional задан параметр
     * noRollbackFor = RecoverableDataAccessException.class,
     * поэтому транзакция НЕ помечается как rollbackOnly и НЕ откатывается
     *
     */
    @Test
    void testUpdateRating3() {
        // Проверяем, что изначально у сериала c id = 1, рейтинг = 5
        Optional<Serial> serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(5, serial.get().getRating());
        // Проверяем, что выполнение updateRating3 вызывает исключение RecoverableDataAccessException
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating3(1L, 10));
        // Проверяем, что транзакция была закоммичена (у сериала c id = 1, рейтинг изменился и стал = 10)
        serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(10, serial.get().getRating());
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating4 происходит вызов транзакционного метода helpMethod1.
     * У аннотации @Transactional над методом helpMethod1 propagation = REQUIRED
     * (т.е действия в updateRating4 и helpMethod1 выполняются в рамках одной транзакции).
     * <br>
     * Внутри helpMethod1, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку.
     * <br>
     * helpMethod1 помечает транзакцию как rollbackOnly, и в итоге транзакция откатывается.
     */
    @Test
    void testUpdateRating4() {
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating4(1L, 10));
    }

    /**
     * Проверка кейса:
     * Из транзакционного метода updateRating5 происходит вызов транзакционного метода helpMethod1 (этот вызов обернут в try/catch).
     * У аннотации @Transactional над методом helpMethod1 propagation = REQUIRED
     * (т.е действия в updateRating5 и helpMethod1 выполняются в рамках одной транзакции).
     * <br>
     * Внутри helpMethod1, возникает исключение RecoverableDataAccessException, пробрасывается вверх по стеку
     * и перехватывается в updateRating5.
     * <br>
     * helpMethod1 помечает транзакцию как rollbackOnly, и в итоге транзакция откатывается
     * (т.е то что мы перехватили исключение в updateRating5 никак не влияет на откат транзакции).
     */
    @Test
    void testUpdateRating5() {
        assertThrowsExactly(UnexpectedRollbackException.class, () -> customSerialService.updateRating5(1L, 10));
    }

    /**
     * Проверка кейса:
     * Из транзакционного метода updateRating6 происходит вызов транзакционного метода helpMethod1.
     * У аннотации @Transactional над методом helpMethod1 propagation = REQUIRED
     * (т.е действия в updateRating6 и helpMethod1 выполняются в рамках одной транзакции).
     * У аннотации @Transactional над методом updateRating6 задан параметр noRollbackFor = RecoverableDataAccessException.class
     * <br>
     * Внутри helpMethod1, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку.
     * <br>
     * helpMethod1 помечает транзакцию как rollbackOnly, и в итоге транзакция откатывается
     * (noRollbackFor влияет только на аннотируемый метод, его поведение не "наследуется" компонентами ниже по стеку вызовов,
     * даже если они используют ту же транзакцию).
     */
    @Test
    void testUpdateRating6() {
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating6(1L, 10));
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRatingAndDelete1 происходит вызов транзакционного метода deleteAllById.
     * У аннотации @Transactional над методом deleteAllById propagation = REQUIRED
     * (т.е действия в updateRatingAndDelete1 и deleteAllById выполняются в рамках одной транзакции).
     * <br>
     * Внутри deleteAllById, возникает исключение EmptyResultDataAccessException и пробрасывается вверх по стеку.
     * <br>
     * deleteAllById помечает транзакцию как rollbackOnly, и в итоге транзакция откатывается.
     * <br>
     * Тест аналогичен тесту testUpdateRating4
     */
    @Test
    void testUpdateRatingAndDelete1() {
        assertThrowsExactly(EmptyResultDataAccessException.class, () -> customSerialService.updateRatingAndDelete1(1L, 10, 2L));
    }

    /**
     * Проверка кейса:
     * Из транзакционного метода updateRatingAndDelete2 происходит вызов транзакционного метода deleteAllById (этот вызов обернут в try/catch).
     * У аннотации @Transactional над методом deleteAllById propagation = REQUIRED
     * (т.е действия в updateRatingAndDelete2 и deleteAllById выполняются в рамках одной транзакции).
     * <br>
     * Внутри deleteAllById, возникает исключение EmptyResultDataAccessException, пробрасывается вверх по стеку
     * и перехватывается в updateRatingAndDelete2.
     * <br>
     * deleteAllById помечает транзакцию как rollbackOnly, и в итоге транзакция откатывается
     * (т.е то что мы перехватили исключение в updateRatingAndDelete2 никак не влияет на откат транзакции).
     * <br>
     * Тест аналогичен тесту testUpdateRating5
     */
    @Test
    void testUpdateRatingAndDelete2() {
        assertThrowsExactly(UnexpectedRollbackException.class, () -> customSerialService.updateRatingAndDelete2(1L, 10, 2L));
    }

    /**
     * Проверка кейса:
     * Из транзакционного метода updateRatingAndDelete3 происходит вызов транзакционного метода deleteAllById.
     * У аннотации @Transactional над методом deleteAllById propagation = REQUIRED
     * (т.е действия в updateRatingAndDelete3 и deleteAllById выполняются в рамках одной транзакции).
     * У аннотации @Transactional над методом updateRatingAndDelete3 задан параметр noRollbackFor = EmptyResultDataAccessException.class
     * <br>
     * Внутри deleteAllById, возникает исключение EmptyResultDataAccessException и пробрасывается вверх по стеку.
     * <br>
     * deleteAllById помечает транзакцию как rollbackOnly, и в итоге транзакция откатывается
     * (noRollbackFor влияет только на аннотируемый метод, его поведение не "наследуется" компонентами ниже по стеку вызовов,
     * даже если они используют ту же транзакцию).
     * <br>
     * Тест аналогичен тесту testUpdateRating6
     */
    @Test
    void testUpdateRatingAndDelete3() {
        assertThrowsExactly(UnexpectedRollbackException.class, () -> customSerialService.updateRatingAndDelete3(1L, 10, 2L));
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating7 происходит вызов транзакционного метода helpMethod2.
     * У аннотации @Transactional над методом helpMethod2 propagation = REQUIRED
     * (т.е действия в updateRating7 и helpMethod2 выполняются в рамках одной транзакции).
     * <br>
     * Внутри helpMethod2, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку.
     * НО у аннотации @Transactional над методом helpMethod2 задан параметр noRollbackFor = RecoverableDataAccessException.class,
     * поэтому транзакция не помечается как rollbackOnly.
     * <br>
     * В методе updateRating7 ранее возникшее исключение RecoverableDataAccessException НЕ перехватывается и пробрасывается вверх по стеку.
     * Так как у @Transactional над методом updateRating7 НЕ задан параметр noRollbackFor = RecoverableDataAccessException.class
     * транзакция помечается как rollbackOnly и откатывается
     */
    @Test
    void testUpdateRating7() {
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating7(1L, 10));
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating8 происходит вызов транзакционного метода helpMethod2.
     * У аннотации @Transactional над методом helpMethod2 propagation = REQUIRED
     * (т.е действия в updateRating8 и helpMethod2 выполняются в рамках одной транзакции).
     * <br>
     * Внутри helpMethod2, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку.
     * НО у аннотации @Transactional над методом helpMethod2 задан параметр noRollbackFor = RecoverableDataAccessException.class,
     * поэтому транзакция не помечается как rollbackOnly.
     * <br>
     * В методе updateRating8 ранее возникшее исключение RecoverableDataAccessException перехватывается и транзакция НЕ откатывается
     */
    @Test
    void testUpdateRating8() {
        // Проверяем, что изначально у сериала c id = 1, рейтинг = 5
        Optional<Serial> serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(5, serial.get().getRating());
        // Проверяем, что выполнение updateRating11 НЕ вызывает исключение
        assertDoesNotThrow(() -> customSerialService.updateRating8(1L, 10));
        // Проверяем, что транзакция была закоммичена (у сериала c id = 1, рейтинг изменился и стал = 10)
        serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(10, serial.get().getRating());
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating9 происходит вызов транзакционного метода helpMethod2.
     * У аннотации @Transactional над методом helpMethod2 propagation = REQUIRED
     * (т.е действия в updateRating9 и helpMethod2 выполняются в рамках одной транзакции).
     * <br>
     * Внутри helpMethod2, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку.
     * НО у аннотации @Transactional над методом helpMethod2 задан параметр noRollbackFor = RecoverableDataAccessException.class,
     * поэтому транзакция не помечается как rollbackOnly.
     * <br>
     * В методе updateRating9 ранее возникшее исключение RecoverableDataAccessException НЕ перехватывается и пробрасывается вверх по стеку.
     * Но так как у @Transactional над методом updateRating7 задан параметр noRollbackFor = RecoverableDataAccessException.class
     * транзакция НЕ помечается как rollbackOnly и НЕ откатывается
     */
    @Test
    void testUpdateRating9() {
        // Проверяем, что изначально у сериала c id = 1, рейтинг = 5
        Optional<Serial> serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(5, serial.get().getRating());
        // Проверяем, что выполнение updateRating11 вызывает исключение RecoverableDataAccessException
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating9(1L, 10));
        // Проверяем, что транзакция была закоммичена (у сериала c id = 1, рейтинг изменился и стал = 10)
        serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(10, serial.get().getRating());
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating10 происходит вызов транзакционного метода helpMethod3.
     * У аннотации @Transactional над методом helpMethod3 propagation = REQUIRES_NEW
     * (т.е действия в updateRating10 и helpMethod3 выполняются в отдельных транзакциях).
     * <br>
     * Внутри helpMethod3, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку
     * (и помечает транзакцию в которой выполняется helpMethod3 как rollbackOnly).
     * <br>
     * Так как мы НЕ перехватываем исключение в updateRating10, то оно также прокидывается вверх по стеку
     * и помечает транзакцию в которой выполняется updateRating10 как rollbackOnly и она откатывается
     */
    @Test
    void testUpdateRating10() {
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating10(1L, 10));
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating11 происходит вызов транзакционного метода helpMethod3.
     * У аннотации @Transactional над методом helpMethod3 propagation = REQUIRES_NEW
     * (т.е действия в updateRating11 и helpMethod3 выполняются в отдельных транзакциях).
     * <br>
     * Внутри helpMethod3, возникает исключение RecoverableDataAccessException и пробрасывается вверх по стеку
     * (и помечает транзакцию в которой выполняется helpMethod3 как rollbackOnly).
     * <br>
     * Так как мы перехватываем исключение в updateRating11, то оно НЕ прокидывается вверх по стеку
     * и НЕ помечает транзакцию в которой выполняется updateRating10 как rollbackOnly и она НЕ откатывается
     */
    @Test
    void testUpdateRating11() {
        // Проверяем, что изначально у сериала c id = 1, рейтинг = 5
        Optional<Serial> serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(5, serial.get().getRating());
        // Проверяем, что выполнение updateRating11 НЕ вызывает исключение
        assertDoesNotThrow(() -> customSerialService.updateRating11(1L, 10));
        // Проверяем, что транзакция была закоммичена (у сериала c id = 1, рейтинг изменился и стал = 10)
        serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(10, serial.get().getRating());
    }

    /**
     * Проверка кейса:
     * <br>
     * <br>
     * Из транзакционного метода updateRating12 происходит вызов транзакционного метода helpMethod3.
     * У аннотации @Transactional над методом helpMethod3 propagation = REQUIRES_NEW
     * (т.е методы updateRating12 и helpMethod3 выполняются в разных транзакциях).
     * <br>
     * Внутри helpMethod3, возникает исключение RecoverableDataAccessException, пробрасывается вверх по стеку
     * и помечает транзакцию в которой выполняется helpMethod3 как rollbackOnly.
     * <br>
     * Так как мы НЕ перехватываем исключение в updateRating12, то оно также прокидывается вверх по стеку,
     * НО так как у аннотации @Transactional над методом updateRating12 задан параметр
     * noRollbackFor = RecoverableDataAccessException.class,
     * транзакция в которой выполняется updateRating12 НЕ откатывается
     */
    @Test
    void testUpdateRating12() {
        // Проверяем, что изначально у сериала c id = 1, рейтинг = 5
        Optional<Serial> serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(5, serial.get().getRating());
        // Проверяем, что выполнение updateRating11 вызывает исключение
        assertThrowsExactly(RecoverableDataAccessException.class, () -> customSerialService.updateRating12(1L, 10));
        // Проверяем, что транзакция была закоммичена (у сериала c id = 1, рейтинг изменился и стал = 10)
        serial = serialRepository.findById(1L);
        assertTrue(serial.isPresent());
        assertEquals(10, serial.get().getRating());
    }
}