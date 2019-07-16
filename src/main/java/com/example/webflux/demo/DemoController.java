package com.example.webflux.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class DemoController {

    private static final ObjectMapper mapper = new ObjectMapper();

    private Flux<String> eventFlux = Flux.generate(sink -> {
        Event event = new Event(UUID.randomUUID().toString(), LocalDateTime.now().toString());
        try {
            sink.next(mapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            sink.error(e);
        }
    });

    private Flux<String> intervalFlux = Flux.interval(Duration.ofMillis(1000L))
            .zipWith(eventFlux, (time, event) -> event);

    @GetMapping(path = "event-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> eventStream() {
        return intervalFlux;
    }
}
