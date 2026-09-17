package com.example.ticket_triage.service;

import com.example.ticket_triage.dto.TicketRequestDTO;
import com.example.ticket_triage.model.Ticket;
import com.example.ticket_triage.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    // Logger instance for tracking metrics and application flows
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final ChatClient chatClient;

    // Spring AI automatically injects the ChatClient builder
    public TicketService(TicketRepository ticketRepository, ChatClient.Builder chatClientBuilder) {
        this.ticketRepository = ticketRepository;
        this.chatClient = chatClientBuilder.build();
    }

    // 1. Create ticket, call Gemini for AI Triage (Priority + Team), and save as OPEN
    public Ticket createAndTriageTicket(TicketRequestDTO requestDTO) {
        // Start time for measuring AI triage latency
        long startTime = System.currentTimeMillis();

        Ticket ticket = new Ticket();
        ticket.setTitle(requestDTO.getTitle());
        ticket.setDescription(requestDTO.getDescription());
        ticket.setCategory(requestDTO.getCategory() != null ? requestDTO.getCategory() : "GENERAL");
        ticket.setStatus("OPEN");

        // Construct a structured prompt for Gemini to assign both Priority and Team
        String prompt = String.format(
                "Analyze the following support ticket. " +
                        "1. Determine its priority: HIGH, MEDIUM, or LOW. " +
                        "2. Determine the responsible team: TECH_SUPPORT, BILLING, or GENERAL. " +
                        "Format your response strictly as: PRIORITY: [value], TEAM: [value]\n\n" +
                        "Title: %s\n" +
                        "Description: %s",
                requestDTO.getTitle(), requestDTO.getDescription()
        );

        // Call Gemini via Spring AI
        String aiResponse = chatClient.prompt(prompt).call().content();

        // Default fallbacks
        String evaluatedPriority = "MEDIUM";
        String assignedTeam = "GENERAL";

        // Parse Gemini's response safely
        if (aiResponse != null) {
            String upper = aiResponse.toUpperCase();

            // Extract Priority
            if (upper.contains("HIGH")) {
                evaluatedPriority = "HIGH";
            } else if (upper.contains("LOW")) {
                evaluatedPriority = "LOW";
            }

            // Extract Team
            if (upper.contains("TECH_SUPPORT")) {
                assignedTeam = "TECH_SUPPORT";
            } else if (upper.contains("BILLING")) {
                assignedTeam = "BILLING";
            }
        }

        ticket.setPriority(evaluatedPriority);
        ticket.setAssignedTeam(assignedTeam);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Calculate latency and log the metric
        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;
        logger.info("METRIC: AI Triage completed for ticket ID [{}] in {} ms", savedTicket.getId(), latency);

        return savedTicket;
    }

    // 2. Read operations
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    // 3. Manual Close/Resolution Endpoint Logic
    public Ticket closeTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        ticket.setStatus("CLOSED");
        return ticketRepository.save(ticket);
    }
}