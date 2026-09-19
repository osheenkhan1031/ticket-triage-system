package com.example.ticket_triage.controller;

import com.example.ticket_triage.dto.TicketRequestDTO;
import com.example.ticket_triage.model.Ticket;
import com.example.ticket_triage.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateTicket() throws Exception {
        TicketRequestDTO requestDTO = new TicketRequestDTO();
        requestDTO.setTitle("Login Failure");
        requestDTO.setDescription("Unable to sign in");

        Ticket createdTicket = new Ticket();
        createdTicket.setId(1L);
        createdTicket.setTitle("Login Failure");
        createdTicket.setPriority("HIGH");
        createdTicket.setStatus("OPEN");

        when(ticketService.createAndTriageTicket(any(TicketRequestDTO.class))).thenReturn(createdTicket);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Login Failure"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void testGetAllTickets() throws Exception {
        Ticket t1 = new Ticket();
        t1.setId(1L);
        Ticket t2 = new Ticket();
        t2.setId(2L);

        when(ticketService.getAllTickets()).thenReturn(Arrays.asList(t1, t2));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testGetTicketById_Success() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Network Issue");

        when(ticketService.getTicketById(1L)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Network Issue"));
    }

    @Test
    void testGetTicketById_NotFound() throws Exception {
        when(ticketService.getTicketById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCloseTicket() throws Exception {
        Ticket closedTicket = new Ticket();
        closedTicket.setId(1L);
        closedTicket.setStatus("CLOSED");

        when(ticketService.closeTicket(1L)).thenReturn(closedTicket);

        mockMvc.perform(put("/api/tickets/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}