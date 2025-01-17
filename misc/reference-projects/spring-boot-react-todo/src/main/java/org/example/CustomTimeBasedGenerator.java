package org.example;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.uuid.UuidValueGenerator;

import java.util.UUID;

public class CustomTimeBasedGenerator implements UuidValueGenerator {
    static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    @Override
    public UUID generateUuid(SharedSessionContractImplementor session) {
        return GENERATOR.generate();
    }
}
