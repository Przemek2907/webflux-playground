package com.app;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

public class CustomSubscriber<T> extends BaseSubscriber<T> {

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        // metoda wykona sie kiedy subscriber z sukcesem podepnie sie do
        // publishera
        System.out.println("SUBSCRIBED!");

        request(1); // TODO WROCIC DO TEJ METODY
        // request pozwala zadac kolejnego elementu ze strumienia reaktywnego

        // requestUnbounded(); // pozwala automatycznie na poczatku powiedziec ze subscriber ma
        // reagowac na kazdy kolejny nowo pojawiajacy sie w reaktywnym strumieniu element
        // wtedy nie musisz juz nigdzie pisac request
    }

    @Override
    protected void hookOnNext(T value) {
        // pozwala nam pobrac kolejny element z publishera
        System.out.println("VALUE: " + value);
        request(1); // zadanie kolejnego elementu
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("COMPLETE!");
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        System.out.println("------------- EXCEPTION -------------");
        System.out.println(throwable.getMessage());
        System.out.println("-------------------------------------");
    }
}
