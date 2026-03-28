package com.example.phonebatch.ai;

import com.example.phonebatch.domain.FeatureStoreEntry;

public interface CheatSheetService {
    String getAdvice(FeatureStoreEntry entry);
}
