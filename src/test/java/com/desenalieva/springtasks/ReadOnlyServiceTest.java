package com.desenalieva.springtasks;

import com.desenalieva.springtasks.repositories.SerialRepository;
import com.desenalieva.springtasks.services.ReadOnlyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReadOnlyServiceTest {
    @Autowired
    private ReadOnlyService readOnlyService;

    @Autowired
    private SerialRepository serialRepository;

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
}