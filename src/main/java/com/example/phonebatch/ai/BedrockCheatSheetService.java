package com.example.phonebatch.ai;

import com.example.phonebatch.domain.FeatureStoreEntry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Stub Bedrock implementation. When app.ai.provider=bedrock, this bean is active.
 * Swap in real AWS Bedrock calls when credentials are available.
 */
@Service("bedrockCheatSheetService")
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "bedrock")
public class BedrockCheatSheetService implements CheatSheetService {

    @Override
    public String getAdvice(FeatureStoreEntry entry) {
        return String.format(
            "[Bedrock] Advice for %s: Balance $%.2f, ReachScore %.4f, RecoveryScore %.4f. "
                + "High-priority contact recommended.",
            entry.getPhoneNumber(),
            entry.getBalanceOwed(),
            entry.getReachScore(),
            entry.getRecoveryScore()
        );
    }
}
