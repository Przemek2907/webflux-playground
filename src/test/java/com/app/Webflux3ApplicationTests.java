package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class Webflux3ApplicationTests {

    /*
        SCHEDULERS

        Scheduling to przypisanie wirtualnie przygotowanych obliczen do konkretnych zasobow.

        Mamy 4 rodzaje schedulerow:

        - SINGLE
        scheduler z jedym watkiem

        - IMMEDIATE
        uruchamiamy przetwarzanie strumienia w watku w ktorym ten strumien powolalismy do zycia

        - PARALLEL
        uruchamiamy tyle watkow ile mamy coreow CPU

        - ELASTIC
        tworzysz tyle watkow ile sprzet moze i ile potrzeba

        Dwie metody pozwalaja zarzadzac schematem wielowatkowym
        subscribeOn
        publishOn
    */

    @Test
    public void test1() throws InterruptedException {
        System.out.println("TEST: " + Thread.currentThread().getName());

        // domyslnie podstawowy Flux czy Mono ma ustawiony scheduler na IMMEDIATE
        Flux
                .just("A", "B", "C")
                // .interval(Duration.ofSeconds(1))
                .subscribeOn(Schedulers.parallel()) // liczy sie tylko pierwszy subscribeOn wiec kolejne zignoruje
                .subscribeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.single())
                .doOnEach(signal -> {
                    System.out.println(Thread.currentThread().getName());
                })
                .publishOn(Schedulers.elastic()) // publishOn zmienia dotychczasowy Scheduler
                .doOnEach(signal -> {
                    System.out.println("2 -> " + Thread.currentThread().getName());
                })
                .subscribe(System.out::println);

        System.out.println("END");
        TimeUnit.SECONDS.sleep(10);
    }
}
