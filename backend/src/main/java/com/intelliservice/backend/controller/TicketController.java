package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.Ticket;
import com.intelliservice.backend.service.TicketService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<List<Ticket>> list(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(ticketService.getUserTickets(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Ticket> detail(@PathVariable Long id) {
        return ApiResponse.success(ticketService.getTicketById(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Ticket> updateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ApiResponse.success(ticketService.updateTicketStatus(id, status));
    }
}
