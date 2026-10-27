package com.tikit.rag.controller;

import com.tikit.rag.controller.advice.RagControllerAdvice;
import com.tikit.rag.service.RagDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminRagDocumentControllerTest {

    private RagDocumentService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(RagDocumentService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AdminRagDocumentController(service))
                .setControllerAdvice(new RagControllerAdvice())
                .build();
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        for (String query : new String[]{"page=-1", "size=0", "size=101"}) {
            mvc.perform(get("/api/v1/admin/rag/documents?" + query))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void rejectsInvalidDocumentId() throws Exception {
        for (String id : new String[]{"0", "-1", "abc"}) {
            mvc.perform(get("/api/v1/admin/rag/documents/" + id))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void rejectsBlankDocument() throws Exception {
        mvc.perform(post("/api/v1/admin/rag/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"content\":\"본문\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(service);
    }
}
