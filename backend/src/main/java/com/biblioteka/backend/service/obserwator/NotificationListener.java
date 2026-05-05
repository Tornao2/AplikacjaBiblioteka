package com.biblioteka.backend.service.obserwator;

import com.biblioteka.backend.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final SystemLogService systemLogService;
    @EventListener
    public void onBookReturned(LoanReturnedEvent event) {
        systemLogService.addLog(event.username(), "LOAN_RETURN", "Zwrócono książkę o ID: " + event.bookId(), "INFO");
    }
}
