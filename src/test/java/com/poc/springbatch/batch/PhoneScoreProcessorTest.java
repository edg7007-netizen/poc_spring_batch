package com.poc.springbatch.batch;

import com.poc.springbatch.batch.processor.PhoneScoreProcessor;
import com.poc.springbatch.model.PhoneRawData;
import com.poc.springbatch.model.PhoneScore;
import com.poc.springbatch.service.DataMergeService;
import com.poc.springbatch.service.ScoreCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneScoreProcessorTest {

    @Mock
    private DataMergeService dataMergeService;

    @Mock
    private ScoreCalculationService scoreCalculationService;

    @InjectMocks
    private PhoneScoreProcessor processor;

    @Test
    void process_callsMergeAndScore() throws Exception {
        PhoneRawData input = PhoneRawData.builder().phoneNumber("5551001001").build();
        PhoneRawData merged = PhoneRawData.builder().phoneNumber("5551001001")
                .currentBalance(new BigDecimal("1500.00")).build();
        PhoneScore expectedScore = PhoneScore.builder()
                .phoneNumber("5551001001")
                .reachScore(72.0)
                .recoveryScore(60.0)
                .scoreDate(LocalDate.now())
                .build();

        when(dataMergeService.enrich(input)).thenReturn(merged);
        when(scoreCalculationService.compute(merged)).thenReturn(expectedScore);

        PhoneScore result = processor.process(input);

        assertThat(result).isEqualTo(expectedScore);
        verify(dataMergeService).enrich(input);
        verify(scoreCalculationService).compute(merged);
    }
}
