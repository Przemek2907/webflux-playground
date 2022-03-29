package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@SpringBootTest
class Webflux1ApplicationTests {

    /*
        CZYM JEST PROGRAMOWANIE REAKTYWNE:

        1. ASYNCHRONICZNOSC, NON-BLOCKING
           http://reactivex.io/

        2. STRUMIENIE REAKTYWNE

        3. WYKORZYSTANY WZORZEC OBSERVATOR

        4. BACKPRESSURE

        5. ERROR AS MESSAGE
    */

    @Test
    void test1() {

        // webflux wprowadza dwa wazne typy:

        // Flux - 'asynchroniczny strumien'
        // Mono - 'asynchroniczny optional'

        // spsoby tworzenia:

        Flux<String> f1 = Flux.just("A", "B", "C");

        Flux<String> f2 = Flux.fromIterable(List.of("X", "Y", "Z"));

        Flux<String> f3 = Flux.fromStream(Stream.of("A", "B", "C"));

        // flux emituje co 1 sekunde liczby 0, 1, 2, 3, ...
        Flux<Long> f4 = Flux.interval(Duration.ofSeconds(1));

        Flux<Integer> f5 = Flux.range(1, 5);

        Flux<String> f6 = Flux.from(f1);

        // NOTHING HAPPENS WITHOUT SUBSCRIBE
        // czyli strumien reaktywny nie bedzie przetwarzany dopoki nie wywolasz subscribe

        // subscribe okresla 3 rzeczy

        // 1. co sie 1ma stac z danymi ktore dostaniesz w ramach strumienia reaktywnego

        // 2. co sie tanie kiedy bedzie blad

        // 3. co sie ma wydarzyc kiedy juz zakonczy sie przetwarzanie danych ze strumienia reaktywnego

        /*Flux<String> f11 = Flux.just("A", "B", "C");
        f11.subscribe(
                data -> System.out.println(data),
                error -> System.out.println(error.getMessage()),
                () -> System.out.println("COMPLETE")
        );*/

        // kiedy robisz interval to dane zarzadzane w ramach tego strumienia reaktywnego
        // sa przetwarzane w osobnym watku
        Flux<Long> f12 = Flux
                .interval(Duration.ofSeconds(2))
                .take(4);
        f12.subscribe(data -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println(data);
        });


        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("END: " + Thread.currentThread().getName());

        // TODO Mono

        Mono<String> m1 = Mono.just("A");
        Mono<String> m2 = Mono.empty();
        Mono<String> m3 = Mono.fromCallable(() -> "ROMAN");
        Mono<String> m4 = Mono.fromSupplier(() -> "ROMAN");
        Mono<String> m5 = Mono.fromDirect(m1);

        // REAKTYWNY JDBC
        // https://r2dbc.io/

    }

    @Test
    void test2() {

        Flux<String> f1 = Flux.generate(
                // przyjmujemy wartosc poczatkowa stanu
                () -> 0,
                // generujemy nowy element ewentualnie mozemy wykorzystac stan
                (state, sink) -> {
                    if (state == 10) {
                        sink.complete();
                    }

                    sink.next("VALUE: " + state);
                    return state + 1; // zwracamy nowy stan ktory bedzie przekazany do kolejnego
                    // wywolania
                }
        );

        Flux<String> f2 = Flux
                .range(0, 10)
                .map(value -> "VALUE: " + value);

    }

    @Test
    void test3() {

        Flux<String> f1 = Flux.create(sink -> {
            List<String> names = List.of("A", "B", "B");

            for (int i = 0; i < names.size(); i++) {
                if (i == 2) {
                    sink.error(new IllegalStateException("NIE BEDE GENEROWAL DALEJ!"));
                }
                sink.next("VALUE: " + names.get(i));
            }
        });

        f1.subscribe(System.out::println, System.out::print);

    }

    @Test
    void test4() {
        Flux<String> f = Flux.just("A", "B", "C");
        CustomSubscriber<String> customSubscriber = new CustomSubscriber<>();
        f.subscribe(customSubscriber);

    }

}
