package ru.practicum.shareit.common;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 23:44
 * @project java-shareit
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceIdGenerator {
    private long nextId = 1;

    public synchronized long getNextId() {
        return nextId++;
    }
}
