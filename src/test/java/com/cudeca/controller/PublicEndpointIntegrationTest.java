package com.cudeca.controller;

import com.cudeca.config.MailConfigTest;
import com.cudeca.testutil.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * arranca todo el contexto de Spring Boot
 * y verifica que el endpoint público es accesible con seguridad habilitada.
 */
@IntegrationTest
@AutoConfigureMockMvc(addFilters = false)
@Import(MailConfigTest.class)
class PublicEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/public/test → 200 OK con mensaje correcto (integration test)")
    void publicTestEndpoint_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/public/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Conexión correcta con el backend CUDECA"));
    }
}
