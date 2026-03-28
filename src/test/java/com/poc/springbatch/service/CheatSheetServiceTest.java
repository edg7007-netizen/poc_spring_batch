package com.poc.springbatch.service;

import com.poc.springbatch.ai.LlmClient;
import com.poc.springbatch.entity.PhoneFeatureStoreEntry;
import com.poc.springbatch.repository.PhoneFeatureStoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheatSheetServiceTest {

    @Mock
    private PhoneFeatureStoreRepository featureStoreRepository;

    @Mock
    private LlmClient llmClient;

    @InjectMocks
    private CheatSheetService service;

    @Test
    void generateByPhoneNumber_whenFound_callsLlm() {
        PhoneFeatureStoreEntry entry = buildEntry("5551001001", "U001");
        when(featureStoreRepository.findTopByPhoneNumberOrderByScoreDateDesc("5551001001"))
                .thenReturn(Optional.of(entry));
        when(llmClient.chat(anyString())).thenReturn("AI advice here");

        String result = service.generateByPhoneNumber("5551001001");

        assertThat(result).isEqualTo("AI advice here");
        verify(llmClient).chat(anyString());
    }

    @Test
    void generateByPhoneNumber_whenNotFound_returnsNotFoundMessage() {
        when(featureStoreRepository.findTopByPhoneNumberOrderByScoreDateDesc("0000000000"))
                .thenReturn(Optional.empty());

        String result = service.generateByPhoneNumber("0000000000");

        assertThat(result).contains("No data found");
        verifyNoInteractions(llmClient);
    }

    @Test
    void generateByUserId_whenFound_callsLlm() {
        PhoneFeatureStoreEntry entry = buildEntry("5551001001", "U001");
        when(featureStoreRepository.findTopByUserIdOrderByScoreDateDesc("U001"))
                .thenReturn(Optional.of(entry));
        when(llmClient.chat(anyString())).thenReturn("Recovery tip for U001");

        String result = service.generateByUserId("U001");

        assertThat(result).isEqualTo("Recovery tip for U001");
    }

    @Test
    void generate_whenLlmThrows_returnsFallback() {
        PhoneFeatureStoreEntry entry = buildEntry("5551001001", "U001");
        when(featureStoreRepository.findTopByPhoneNumberOrderByScoreDateDesc("5551001001"))
                .thenReturn(Optional.of(entry));
        when(llmClient.chat(anyString())).thenThrow(new RuntimeException("Bedrock unavailable"));

        String result = service.generateByPhoneNumber("5551001001");

        assertThat(result).contains("NEGOTIATION CHEAT SHEET");
        assertThat(result).contains("5551001001");
    }

    @Test
    void buildPrompt_containsAllKeyFields() {
        PhoneFeatureStoreEntry entry = buildEntry("5551001001", "U001");
        String prompt = service.buildPrompt(entry);

        assertThat(prompt).contains("5551001001");
        assertThat(prompt).contains("U001");
        assertThat(prompt).contains("1500");
        assertThat(prompt).contains("Reach Score");
        assertThat(prompt).contains("Recovery Score");
    }

    private PhoneFeatureStoreEntry buildEntry(String phone, String userId) {
        return PhoneFeatureStoreEntry.builder()
                .phoneNumber(phone)
                .userId(userId)
                .scoreDate(LocalDate.now())
                .currentBalance(new BigDecimal("1500.00"))
                .previousLoansCount(3)
                .ptpMadeCount(2)
                .hasBrokenPtp(false)
                .appInstalled(true)
                .daysSinceLastContact(5L)
                .historicalAnswerRateSameHour(0.72)
                .historicalAnswerRateSameDow(0.68)
                .reachScore(65.0)
                .recoveryScore(72.0)
                .build();
    }
}
