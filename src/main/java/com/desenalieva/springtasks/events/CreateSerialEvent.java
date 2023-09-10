package com.desenalieva.springtasks.events;

import com.desenalieva.springtasks.entities.Serial;
import org.springframework.context.ApplicationEvent;

/**
 * Событие создания сериала
 */
public class CreateSerialEvent extends ApplicationEvent {
    private final Serial serial;

    /**
     * Конструктор.
     * @param source объект, на котором изначально произошло событие или с которым событие связано
     * @param serial сериал
     */
    public CreateSerialEvent(Object source, Serial serial) {
        super(source);
        this.serial = serial;
    }

    public Serial getSerial() {
        return serial;
    }
}
