package com.mx.gillustrated.util;

import androidx.annotation.NonNull;
import android.util.Log;

import com.mx.gillustrated.common.DBCall;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Array;
import java.util.concurrent.Callable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Created by maoxin on 2017/2/21.
 */

public class ServiceUtils {

    public static <T> Flowable<T> createConnect(@NonNull final DBCall<T> call) {
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(final FlowableEmitter<T> e) throws Exception {
                T result = call.enqueue();
                e.onNext(result);
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable){

                    }
                });
    }




    public static void createMultVoidConnect(){
        Flowable<Boolean> f1 = Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                return true;
            }
        });

        Flowable<String> f3 = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
                e.onNext("a");
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER);

        final Flowable<String> f4 = Flowable.just("f4");
        Flowable<String> f2 = Flowable.just("bc").map(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                return s + "def";
            }
        }).flatMap(new Function<String, Publisher<String>>() {
            @Override
            public Publisher<String> apply(final String s) throws Exception {
                return Flowable.create(new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> e) throws Exception {
                        e.onNext(s + "flatMap");
                    }
                }, BackpressureStrategy.BUFFER);
            }
        });

        Flowable.combineLatest(getData1(), getData2(), new BiFunction<String, String, String[]>() {
            @Override
            public String[] apply(String s, String s2) throws Exception {
                return  new String[]{s, s2};
            }
        }).flatMap(new Function<String[], Publisher<?>>() {
            @Override
            public Publisher<?> apply(String[] strings) throws Exception {
                return getData3(strings[0], strings[1]);
            }
        }).doOnNext(new Consumer<Object>() {
            @Override
            public void accept(Object s) throws Exception {
                Log.i("ServiceUtils", "------------------------" + s);
            }
        }).subscribe();



//        Flowable.combineLatest(f3, f2, new BiFunction<String, String, Object>() {
//            @Override
//            public Object apply(String s, String s2) throws Exception {
//                return  s + s2;
//            }
//        }).subscribe(new DisposableSubscriber<Object>() {
//            @Override
//            public void onNext(Object o) {
//                Log.i("ServiceUtils", "------------------------" + o);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });

    }

    static Flowable<String> getData1(){
        return Flowable.just("one");
    }

    static Flowable<String> getData2(){
        return Flowable.just("two");
    }

    static Flowable<String> getData3(String s1, String s2){
        return Flowable.just(s1 + " ===  " + s2);
    }

}
