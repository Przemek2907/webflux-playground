package com.app;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Webflux5Application {

    public <T> Flux<T> appendError(Flux<T> source) {
        return source.concatWith(Mono.error(new IllegalArgumentException("ERROR")));
    }

    @Test
    public void test1() {

        // StepVerifier czyli specjalny typ ktory dostarcza nam niesamowitych mechanizmow
        // do testowania
        var f = Flux.just("A", "B");
        StepVerifier
                .create(appendError(f))
                .expectNext("A")
                .expectNext("B")
                .expectErrorMessage("ERROR")
                .verify();

    }

    @Test
    public void test2() {

        var f = Flux.just("A", "A", "A", "B", "B", "B", "B");
        StepVerifier
                .create(appendError(f))
                .expectNext("A")
                .expectNextCount(2) // wiem ze kolejne 3 to tez ma byc "A"
                .expectNext("B")
                .expectNextCount(3)
                .verifyError(); // oczekujemy ze sie wszystko zakonczy dobrze

    }
}
