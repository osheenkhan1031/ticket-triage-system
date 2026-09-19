package com.example.ticket_triage.service;

import com.example.ticket_triage.dto.TicketRequestDTO;
import com.example.ticket_triage.model.Ticket;
import com.example.ticket_triage.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class TicketServiceTest {

    @MockitoBean
    private TicketRepository ticketRepository;

    @MockitoBean
    private ChatClient.Builder chatClientBuilder; // Spring AI client ko mock karne ke liye

    @Autowired
    private TicketService ticketService;

    // 1. Test: AI Triage aur Ticket Creation
    @Test
    void testCreateAndTriageTicket() {
        // Mocking Spring AI fluent API chain
        ChatClient chatClientMock = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpecMock = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseMock = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClientMock);
        when(chatClientMock.prompt(any(String.class))).thenReturn(requestSpecMock);
        when(requestSpecMock.call()).thenReturn(callResponseMock);
        when(callResponseMock.content()).thenReturn("PRIORITY: HIGH, TEAM: TECH_SUPPORT");

        TicketRequestDTO requestDTO = new TicketRequestDTO();
        requestDTO.setTitle("App Crash");
        requestDTO.setDescription("App crashes on startup");

        Ticket savedTicket = new Ticket();
        savedTicket.setId(1L);
        savedTicket.setTitle("App Crash");
        savedTicket.setPriority("HIGH");
        savedTicket.setAssignedTeam("TECH_SUPPORT");
        savedTicket.setStatus("OPEN");

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        Ticket result = ticketService.createAndTriageTicket(requestDTO);

        assertNotNull(result);
        assertEquals("HIGH", result.getPriority());
        assertEquals("TECH_SUPPORT", result.getAssignedTeam());
        assertEquals("OPEN", result.getStatus());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    // 2. Test: to fetch all tickets
    @Test
    void testGetAllTickets() {
        Ticket t1 = new Ticket();
        t1.setId(1L);
        Ticket t2 = new Ticket();
        t2.setId(2L);

        when(ticketRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Ticket> tickets = ticketService.getAllTickets();

        assertEquals(2, tickets.size());
        verify(ticketRepository, times(1)).findAll();
    }

    // 3. Test: to fetch ticket using id
    @Test
    void testGetTicketById_Success() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    // 4. Test: ID Not Found
    @Test
    void testGetTicketById_NotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Ticket> result = ticketService.getTicketById(99L);

        assertFalse(result.isPresent());
    }

    // 5. Test: Ticket Close  (Success)
    @Test
    void testCloseTicket_Success() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus("OPEN");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket closedTicket = ticketService.closeTicket(1L);

        assertEquals("CLOSED", closedTicket.getStatus());
        verify(ticketRepository, times(1)).save(ticket);
    }

    // 6. Test: wrong ID  Ticket Closure Exception
    @Test
    void testCloseTicket_NotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.closeTicket(99L);
        });

        assertTrue(exception.getMessage().contains("Ticket not found"));
    }
}