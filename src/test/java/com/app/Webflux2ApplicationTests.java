package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest
public class Webflux2ApplicationTests {

    private String convert(int value) {
        if (value < 10) {
            throw new IllegalArgumentException("VALUE IS NOT CORRECT: " + value);
        }
        return String.valueOf(value);
    }

    @Test
    public void test1() {
        var f1 = Flux
                .range(1, 4)
                .map(value -> convert(value));


        // f1.subscribe(System.out::println, System.err::println);

        // onErrorValue w obliczu bledu zwraca wartosc zapasowa
        // oczywiscie przerywa strumien reaktywny i zwraca zapasowa wartosc zamiast bledu
        /*f1
                .onErrorReturn("WRONG VALUE")
                .subscribe(System.out::println);*/

        // zwraca nam zapsowa wartosc kiedy wystapi okreslony warunek
        /*f1
                .onErrorReturn(e -> e.getMessage().endsWith("NOT CORRECT: 1"),"WRONG VALUE")
                .subscribe(System.out::println);*/

        // mozesz w obliczu bledu zwrocic zapasowy publisher
        /*f1
                .onErrorResume(error -> {
                    if (error instanceof IllegalStateException) {
                        return Flux.just("A", "B", "C");
                    } else if (error instanceof IllegalArgumentException) {
                        return Flux.empty();
                    }
                    // jezeli nie pasuja warunki do przetworzenia bledu to mozecie
                    // zrobic fluxowe re-throw i rzucic ten blad dalej:
                    return Flux.error(new IllegalStateException("NEW EXCEPTION"));
                }).subscribe(System.out::println);*/

        f1
                .onErrorMap(e -> new IllegalStateException(e.getMessage()))
                .onErrorReturn("XXX")
                // SIDE EFFECT
                .doFinally(signal -> {
                    System.out.println("FINALLY");
                    if (signal == SignalType.CANCEL) {
                        System.out.println("CANCELED!");
                    }
                })
                .subscribe(System.out::println);
    }

    @Test
    public void test2() {
        // zarzadzanie zasobami

        AtomicBoolean isDisposed = new AtomicBoolean();

        Disposable disposable = new Disposable() {
            @Override
            public void dispose() {
                // mozesz interfejs Disposable implementowac do dowolnej klasy
                // a potem w metodzie dispose zamykac zasoby ktore dla tej przygotowales
                isDisposed.set(true);
            }

            @Override
            public String toString() {
                return "INSIDE DISPOSABLE";
            }
        };

        Flux<String> f = Flux.using(
                () -> disposable, // dajesz instancje klasy ktora implementuje Disposable
                disp -> Flux.just(disp.toString()), // tutaj mozesz przetwarzac instancje
                // przekazana w pierwszym argumencie
                Disposable::dispose // gwarantuje nam to ze w momencie kiedy juz nie bedzie
                // przetwarzany strumien reaktywny uzyskany w wyniku implementacji z drugiego
                // argumento zostanie wywolana metoda dispose z przekazanego w pierwszym
                // argumencie disposable
        );
        f.subscribe(System.out::println, System.out::println, () -> System.out.println(isDisposed.get()));
    }

    @Test
    public void test3() throws InterruptedException {
        // mozesz probowac ponownie przetwarzac flux i dopiero po x probach jak sie nie uda dostaniesz blad
        Flux
                .interval(Duration.ofMillis(150))
                .map(value -> {
                    System.out.println(Thread.currentThread().getName());
                    if (value < 3) {
                        return "VALUE: " + value;
                    }
                    throw new IllegalStateException("COS POSZLO NIE TAK");
                })
                .elapsed() // uruchamiamy mechanizm ktory zrealizuje nam powtorzenia
                .retry(5)
                .subscribe(System.out::println, System.out::println);

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test4() throws InterruptedException {
        Flux
                // .interval(Duration.ofSeconds(1))
                .just("A", "B")
                .take(1) // metoda take w pracy z interval po 3 elemencie wyemituje sygnal CANCEL
                .doFinally(signal -> System.out.println(/*signal.equals(SignalType.CANCEL)*/signal.toString()))
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }
}
