package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WelcomeScreenControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Standalone setup je najbrži za obične kontrolere
        mockMvc = MockMvcBuilders.standaloneSetup(new WelcomeScreenController()).build();
    }

    @Test
    @DisplayName("GET /logfleet/api/welcome - Uspješno vraća WelcomeScreen view")
    void showWelcomeScreen_ShouldReturnCorrectView() throws Exception {
        mockMvc.perform(get("/logfleet/api/welcome"))
                .andExpect(status().isOk()) // Provjerava status 200
                .andExpect(view().name("WelcomeScreen")); // Provjerava točan naziv templatea
    }

    @Test
    @DisplayName("GET /logfleet/api/welcome - Ne smije prihvatiti POST metodu")
    void showWelcomeScreen_PostNotAllowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/logfleet/api/welcome"))
                .andExpect(status().isMethodNotAllowed()); // Provjerava status 405
    }

    @Test
    @DisplayName("GET /pogresna-putanja - Vraća 404")
    void welcomeScreen_WrongPath_Returns404() throws Exception {
        mockMvc.perform(get("/logfleet/api/notfound"))
                .andExpect(status().isNotFound());
    }
}