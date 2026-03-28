package com.example.phonebatch.ai;

import com.example.phonebatch.domain.FeatureStoreEntry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(name = "bedrockCheatSheetService")
public class MockCheatSheetService implements CheatSheetService {

    @Override
    public String getAdvice(FeatureStoreEntry entry) {
        return String.format(
            "Mock advice for %s: Balance owed: $%.2f. ReachScore: %.4f. Consider payment plan options.",
            entry.getPhoneNumber(),
            entry.getBalanceOwed(),
            entry.getReachScore()
        );
    }
}
