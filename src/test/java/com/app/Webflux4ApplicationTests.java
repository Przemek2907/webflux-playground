package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SpringBootTest
public class Webflux4ApplicationTests {

    // BACKPRESSURE

    @Test
    public void test1() {

        List<Person> people = List.of(
                Person.builder().name("ADAM").age(20).build(),
                Person.builder().name("PAWEL").age(25).build(),
                Person.builder().name("ANDRZEJ").age(32).build(),
                Person.builder().name("IZA").age(27).build(),
                Person.builder().name("KAMIL").age(36).build());

        // metoa buffer
        // zbiera dane z Flux-a do kolekcji list i emituje je naraz
        // kiedy Flux zakonczy swoje dzialanie

        // UWAGA SPOSOB KONWERSJI Flux<String> -> Flux<List<String>>
        Flux<List<Person>> f = Flux
                .fromIterable(people)
                .buffer();

        Flux<Person> map = Flux
                .fromIterable(people)
                .buffer()
                .map(pep -> pep
                        .stream()
                        .max(Comparator.comparing(Person::getName))
                        .orElseThrow());

        map.subscribe(System.out::println);

    }

    @Test
    public void test2() {

        List<Person> people = List.of(
                Person.builder().name("ADAM").age(20).build(),
                Person.builder().name("PAWEL").age(25).build(),
                Person.builder().name("ANDRZEJ").age(32).build(),
                Person.builder().name("IZA").age(27).build(),
                Person.builder().name("KAMIL").age(36).build());

        // tworzy Flux<List<Person>> ale w kazdej liscie jest max po 2 elementy
        Flux<List<Person>> f = Flux
                .fromIterable(people)
                .buffer(2);
        f.subscribe(System.out::println);
    }

    @Test
    public void test3() throws InterruptedException {

        List<Person> people = List.of(
                Person.builder().name("ADAM").age(20).build(),
                Person.builder().name("PAWEL").age(25).build(),
                Person.builder().name("ANDRZEJ").age(32).build(),
                Person.builder().name("IZA").age(27).build(),
                Person.builder().name("KAMIL").age(36).build());

        // max size na 3 czyli max ilosc elementow we fluxie to 3
        // ewentualnie tyle ile sie uda nazbierac w czasie 5 seconds
        Flux<List<Person>> f = Flux
                .fromIterable(people)
                .delayElements(Duration.ofSeconds(3))
                .bufferTimeout(3, Duration.ofSeconds(5));
        f.subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(20);
    }

    @Test
    public void test4() throws InterruptedException {

        List<Person> people = List.of(
                Person.builder().name("ADAM").age(20).build(),
                Person.builder().name("PAWEL").age(25).build(),
                Person.builder().name("ANDRZEJ").age(32).build(),
                Person.builder().name("IZA").age(27).build(),
                Person.builder().name("KAMIL").age(36).build());

        // tworzony jest bufor z kolejnych elementow dopoki dany element
        // nie spelni predykatu, jezeli spelni to tez zostanie dodany
        // jeszcze do tego buforea a nastepnie bufor zostanie zamkniety
        // i kolejne sa dodawane do nowego bufora az ponownie nie zajdzie predicate
        Flux
                .fromIterable(people)
                .bufferUntil(p -> p.getAge() > 30)
                .subscribe(System.out::println);
    }

    @Test
    public void test5() throws InterruptedException {

        List<Person> people = List.of(
                Person.builder().name("ADAM").age(20).build(),
                Person.builder().name("PAWEL").age(25).build(),
                Person.builder().name("ANDRZEJ").age(32).build(),
                Person.builder().name("IZA").age(27).build(),
                Person.builder().name("KAMIL").age(36).build());

        // collectList
        // Flux<X> -> Mono<List<X>>
        Flux
                .fromIterable(people)
                .collectList()
                .map(peopleList -> peopleList.stream().findFirst().orElseThrow())
                .subscribe(System.out::println);
    }

    @Test
    public void test6() throws InterruptedException {
        // Laczenie publisherow
        Flux<String> f1 = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        Flux<String> f2 = Flux.just("AA", "BB", "CC").delayElements(Duration.ofMillis(230));

        // zip - bierze pierwszy element z pierwszego strumienia i pierwszy element z drugiego
        // strumienia i zwraca jako tuple, potem to samo z drugimi eleentami, trzecimi elementami
        // i tak dalej
        // http://reactivex.io/documentation/operators/zip.html

        // Flux.zip(f1, f2)
        /*f1
                .zipWith(f2)
                .subscribe(x -> System.out.println(x.getT1() + " " + x.getT2()));*/

        // concat - najpierw elementy pierwszego strumienia a potem drugiego
        // http://reactivex.io/documentation/operators/concat.html
        /*f1
                .concatWith(f2)
                .subscribe(System.out::println);*/

        // merge
        // scala publishery w taki sposob ze bierze ten element z tego publishera
        // ktora akurat jest emitowany
        // http://reactivex.io/documentation/operators/merge.html
        f1
                .mergeWith(f2)
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test7() throws InterruptedException {
        // window
        // OKNO CZASOWE
        // kolejne elementy ktore mieszcza sie w danym oknie czasowym gromadzi nowy Flux
        // kiedy okno czasowe sie konczy dany Flux jest "zamykany" i w nowym oknie czasowym
        // znowy Flux zaczyna zbierac nowe elementy
        Flux<String> f1 = Flux.just("A", "B", "C", "D", "E", "F").delayElements(Duration.ofSeconds(1));

        // window moze kolejne elementy zarowno po ilosci jak i po czasie
        f1
                .window(Duration.ofSeconds(3))
                .map(flux -> flux.reduce("", String::concat))
                .subscribe(flux -> flux.subscribe(System.out::println));

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test8() throws InterruptedException {

        AtomicInteger counter = new AtomicInteger();

        Function<Flux<Integer>, Flux<Integer>> converter = f -> {
            counter.incrementAndGet();
            return f.map(x -> x * 10);
        };

        System.out.println("--------------------- COMPOSE ------------------------");
        /*Flux<Integer> f = Flux.just(1).compose(converter); // za kazdym razem od nowa
        // przeprowadza konwersje
        f.subscribe(System.out::println);
        f.subscribe(System.out::println);
        f.subscribe(System.out::println);
        System.out.println("COUNTER = " + counter.get());*/

        System.out.println("-------------------- TRANSFORM -----------------------");
        Flux<Integer> f = Flux.just(1).transform(converter); // transform raz na poczatek
        // wylicza sposob konwersji dla fluxa i przy kolejnych subscribe-ach juz jej nie
        // powtarza tylko korzysta z wczesniej wyznaczonego fluxa
        f.subscribe(System.out::println);
        f.subscribe(System.out::println);
        f.subscribe(System.out::println);
        System.out.println("COUNTER = " + counter.get());
    }

    @Test
    public void test9() throws InterruptedException {

        /*var f1 = Flux
                .just("A", "B", "C")
                // zakonczy emisje danych jednego strumienia reaktywnego nullem
                // a potem przerzuci sie na emisje kolejnego strumienia
                .doOnEach(stringSignal -> System.out.println("1 -> " + stringSignal.get()))
                .thenMany(Flux.just("AA", "BB", "CC"))
                .doOnEach(stringSignal -> System.out.println("2 -> " + stringSignal.get()));
        f1.subscribe(data -> System.out.println("3 -> " + data));*/

        /*var f2 = Flux
                .just("A", "B", "C")
                // zakonczy emisje danych jednego strumienia reaktywnego nullem
                // a potem przerzuci sie na emisje mono
                .doOnEach(stringSignal -> System.out.println("1 -> " + stringSignal.get()))
                .then(Mono.just("AA"))
                .doOnEach(stringSignal -> System.out.println("2 -> " + stringSignal.get()));
        f2.subscribe(data -> System.out.println("3 -> " + data));*/

        var f3 = Flux
                .just("A", "B", "C")
                // zakonczy emisje danych jednego strumienia reaktywnego nullem
                // a potem przerzuci sie na emisje pustego mono w ktorym mozesz np wykonac pewne czynnosci konowe
                .doOnEach(stringSignal -> System.out.println("1 -> " + stringSignal.get()))
                .thenEmpty(Mono.create(sink -> System.out.println("SUCCESS"))) // zwraca nam Mono<Void>
                .doOnEach(stringSignal -> System.out.println("2 -> " + stringSignal.get()));
        f3.subscribe(data -> System.out.println("3 -> " + data));
    }
}
