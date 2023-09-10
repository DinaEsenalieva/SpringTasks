package com.desenalieva.springtasks;

import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import com.desenalieva.springtasks.services.SerialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionSystemException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SerialServiceTest {
    @Autowired
    private SerialService serialService;

    @Autowired
    private SerialRepository serialRepository;

    @Value("${serial.info.filepath}")
    public String SERIAL_INFO_FILE_PATH;

    private Path path;

    @BeforeEach
    void setup() {
        serialRepository.deleteAll();
        path = Path.of(SERIAL_INFO_FILE_PATH);
        deleteTestFile();
    }

    @AfterEach
    void cleanup() {
        serialRepository.deleteAll();
        deleteTestFile();
    }

    private void deleteTestFile() {
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Проверяем, что в случае успешного коммита транзакции при создании сериала,
     * происходит запись в файл информации о созданном сериале
     */
    @Test
    void testWriteSerialInfoAfterCommit() {
        serialService.create(2L, "Serial", 10);
        Optional<Serial> serialOpt = serialRepository.findById(2L);
        assertTrue(serialOpt.isPresent());
        Serial serial = serialOpt.get();
        assertTrue(Files.exists(path));
        try (BufferedReader bw = Files.newBufferedReader(path, UTF_8)) {
            String serialInfo = bw.readLine();
            assertEquals(serialInfo, String.format("%1$s, %2$d", serial.getName(), serial.getRating()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверяем, что в случае отката транзакции при создании сериала,
     * НЕ происходит запись в файл информации о сериале (и соответственно сам сериал в БД не создается)
     */
    @Test
    void testWriteSerialInfoAfterRollback() {
        assertThrowsExactly(TransactionSystemException.class, () -> serialService.create(2L, "Serial", 11));
        Optional<Serial> serialOpt = serialRepository.findById(2L);
        assertFalse(serialOpt.isPresent());
        assertFalse(Files.exists(path));
    }
}