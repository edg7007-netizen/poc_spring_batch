package com.example.phonebatch.controller;

import com.example.phonebatch.ai.CheatSheetService;
import com.example.phonebatch.repository.FeatureStoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CheatSheetController {

    private final FeatureStoreRepository repository;
    private final CheatSheetService cheatSheetService;

    public CheatSheetController(FeatureStoreRepository repository, CheatSheetService cheatSheetService) {
        this.repository = repository;
        this.cheatSheetService = cheatSheetService;
    }

    @GetMapping("/cheat-sheet/{phoneNumber}")
    public ResponseEntity<Map<String, String>> getCheatSheet(@PathVariable String phoneNumber) {
        return repository.findTopByPhoneNumberOrderByJobDateDesc(phoneNumber)
            .map(entry -> {
                String advice = cheatSheetService.getAdvice(entry);
                return ResponseEntity.ok(Map.of(
                    "phoneNumber", phoneNumber,
                    "advice", advice
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
