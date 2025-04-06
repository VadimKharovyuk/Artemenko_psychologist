package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.service.impl.EmailSender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final BlockingQueue<Runnable> emailQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Runnable task = emailQueue.take();
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("Ошибка при выполнении задачи отправки email", e);
                    }
                    TimeUnit.SECONDS.sleep(5); // Задержка между отправками
                } catch (InterruptedException e) {
                    log.warn("Прерывание потока обработки email-очереди");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void queueEmail(Runnable emailTask) {
        if (!emailQueue.offer(emailTask)) {
            log.warn("Не удалось добавить задачу в очередь email-рассылки");
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Завершение службы email-очереди");
        executorService.shutdownNow();
    }
}