package com.poc.springbatch.controller;

import com.poc.springbatch.service.CheatSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoint for generating a negotiation cheat sheet.
 *
 * External systems (e.g. CRM, dialler desktop) can call this endpoint before a
 * call to get AI-generated advice tailored to the specific client.
 */
@RestController
@RequestMapping("/api/v1/cheat-sheet")
@RequiredArgsConstructor
@Tag(name = "Cheat Sheet", description = "AI-generated negotiation advice for phone agents")
public class CheatSheetController {

    private final CheatSheetService cheatSheetService;

    @GetMapping(value = "/by-phone/{phoneNumber}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get cheat sheet by phone number",
               description = "Returns AI-generated negotiation advice for the given phone number based on the latest feature store data.")
    public ResponseEntity<String> getByPhoneNumber(
            @Parameter(description = "Phone number to query", example = "5551001001")
            @PathVariable String phoneNumber) {
        String advice = cheatSheetService.generateByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(advice);
    }

    @GetMapping(value = "/by-user/{userId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Get cheat sheet by user ID",
               description = "Returns AI-generated negotiation advice for the given user ID based on the latest feature store data.")
    public ResponseEntity<String> getByUserId(
            @Parameter(description = "User ID to query", example = "U001")
            @PathVariable String userId) {
        String advice = cheatSheetService.generateByUserId(userId);
        return ResponseEntity.ok(advice);
    }
}
