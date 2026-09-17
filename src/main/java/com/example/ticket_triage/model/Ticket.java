package com.example.ticket_triage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String priority; // HIGH, MEDIUM, LOW
    private String category; // BUG, FEATURE, SUPPORT
    private String status;   // OPEN, IN_PROGRESS, RESOLVED
    private String assignedTeam;//team assignment
}