package com.poc.springbatch.controller;

import com.poc.springbatch.service.CheatSheetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheatSheetController.class)
class CheatSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheatSheetService cheatSheetService;

    @Test
    void getByPhoneNumber_returnsAdvice() throws Exception {
        when(cheatSheetService.generateByPhoneNumber("5551001001"))
                .thenReturn("Call them early, be polite.");

        mockMvc.perform(get("/api/v1/cheat-sheet/by-phone/5551001001"))
                .andExpect(status().isOk())
                .andExpect(content().string("Call them early, be polite."));
    }

    @Test
    void getByUserId_returnsAdvice() throws Exception {
        when(cheatSheetService.generateByUserId("U001"))
                .thenReturn("Client has PTP history, negotiate small amounts.");

        mockMvc.perform(get("/api/v1/cheat-sheet/by-user/U001"))
                .andExpect(status().isOk())
                .andExpect(content().string("Client has PTP history, negotiate small amounts."));
    }

    @Test
    void getByPhoneNumber_whenNotFound_returns200WithMessage() throws Exception {
        when(cheatSheetService.generateByPhoneNumber("9999999999"))
                .thenReturn("No data found for phone number: 9999999999");

        mockMvc.perform(get("/api/v1/cheat-sheet/by-phone/9999999999"))
                .andExpect(status().isOk())
                .andExpect(content().string("No data found for phone number: 9999999999"));
    }
}
