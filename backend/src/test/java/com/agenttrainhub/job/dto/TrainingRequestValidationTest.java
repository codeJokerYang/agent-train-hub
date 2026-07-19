package com.agenttrainhub.job.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsValidationRatioOutsideZeroToOneRange() {
        TrainingParams negative = new TrainingParams();
        negative.setValidationRatio(-0.01);
        TrainingParams one = new TrainingParams();
        one.setValidationRatio(1.0);

        assertEquals(1, validator.validate(negative).size());
        assertEquals(1, validator.validate(one).size());
    }

    @Test
    void rejectsTaskNameLongerThanDatabaseColumn() {
        CreateJobRequest request = new CreateJobRequest();
        request.setTaskName("x".repeat(129));
        request.setDatasetId(1L);
        request.setTemplateId(1L);

        assertEquals(1, validator.validate(request).size());
    }
}
