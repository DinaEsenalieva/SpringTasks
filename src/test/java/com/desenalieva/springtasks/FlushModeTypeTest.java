package com.desenalieva.springtasks;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.BookRepository;
import com.desenalieva.springtasks.repositories.SerialRepository;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class FlushModeTypeTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SerialRepository serialRepository;

    @Autowired
    private BookRepository bookRepository;

    private TransactionTemplate transactionTemplate;

    /**
     * JdbcTemplate нужен для запросов, которые не будет отслеживать EntityManager
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SELECT_SERIALS = "SELECT * FROM Serial s";

    @BeforeEach
    void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void cleanup() {
        serialRepository.deleteAll();
    }

    /**
     * Проверка FlushModeType.ALWAYS
     * flush происходит:
     * - перед каждым запросом
     * - перед фиксацией транзакции
     * - при прямом вызове flush()
     */
    @Test
    void testFlushModeTypeAlways() {
        transactionTemplate.execute(status -> {
            Session session = (Session) entityManager.getDelegate();
            session.setHibernateFlushMode(FlushMode.ALWAYS);

            // сохраняем cериал
            serialRepository.save(new Serial(1L));

            // flush не произошел
            assertEquals(0, jdbcTemplate.queryForList(SELECT_SERIALS).size());

            // делаем запрос на получение всех книг
            bookRepository.findAll();

            // произошел flush
            assertEquals(1, jdbcTemplate.queryForList(SELECT_SERIALS).size());

            // сохраняем вторую сущность
            serialRepository.save(new Serial(2L));

            // flush не произошел (будет вызван перед коммитом транзакции, см. следующий assert)
            assertEquals(1, jdbcTemplate.queryForList(SELECT_SERIALS).size());
            return status;
        });

        // произошел flush перед коммитом транзакции
        assertEquals(2, jdbcTemplate.queryForList(SELECT_SERIALS).size());
    }

    /**
     * Проверка FlushModeType.AUTO
     * flush происходит:
     * - перед выполнением запроса, использующего любую таблицу БД, для которой контекст персистентности
     * содержит любые изменения
     * - перед фиксацией транзакции
     * - при прямом вызове flush()
     */
    @Test
    void testFlushModeTypeAuto() {
        transactionTemplate.execute(status -> {

            // сохраняем cериал
            serialRepository.save(new Serial(1L));

            // flush не произошел
            assertEquals(0, jdbcTemplate.queryForList(SELECT_SERIALS).size());

            // делаем запрос на получение всех книг
            bookRepository.findAll();

            // flush не произошел
            assertEquals(0, jdbcTemplate.queryForList(SELECT_SERIALS).size());

            // делаем запрос к таблице serial, flush перед запросом произошел
            assertEquals(1, serialRepository.count());

            //сохраняем второй инстанс сериала
            serialRepository.save(new Serial(2L));

            // flush не произошел, в бд по прежнему один сериал
            assertEquals(1, jdbcTemplate.queryForList(SELECT_SERIALS).size());
            return status;
        });

        // произошел flush перед коммитом транзакции
        assertEquals(2, jdbcTemplate.queryForList(SELECT_SERIALS).size());
    }


    /**
     * Проверка FlushModeType.COMMIT
     * flush происходит:
     * - перед фиксацией транзакции
     * - при прямом вызове flush()
     */
    @Test
    void testFlushModeTypeCommit() {
        transactionTemplate.execute(status -> {
            Session session = (Session) entityManager.getDelegate();
            session.setHibernateFlushMode(FlushMode.COMMIT);

            // сохраняем сериал
            serialRepository.save(new Serial(1L));

            // делаем запрос к таблице serial, flush перед запросом НЕ произошел
            assertEquals(0, serialRepository.count());
            return status;
        });

        // произошел flush перед коммитом транзакции
        assertEquals(1, serialRepository.count());

    }

    /**
     * Проверка FlushModeType.MANUAL
     * flush происходит:
     * - при прямом вызове flush()
     */
    @Test
    void testFlushModeTypeManual() {
        transactionTemplate.execute(status -> {
            Session session = (Session) entityManager.getDelegate();
            session.setHibernateFlushMode(FlushMode.MANUAL);

            //сохраняем сериал
            serialRepository.save(new Serial(1L));

            // делаем запрос к таблице serial, flush перед запросом НЕ произошел
            assertEquals(0, serialRepository.count());
            return status;
        });

        // НЕ произошел flush перед коммитом транзакции
        assertEquals(0, serialRepository.count());

        transactionTemplate.execute(status -> {
            Session session = (Session) entityManager.getDelegate();
            session.setHibernateFlushMode(FlushMode.MANUAL);

            //сохраняем сериал
            serialRepository.save(new Serial(2L));

            // вызываем flush()
            session.flush();

            // изменения попали в бд
            assertEquals(1, serialRepository.count());
            return status;
        });
    }
}
