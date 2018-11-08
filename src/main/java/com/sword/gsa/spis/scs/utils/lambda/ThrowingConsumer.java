package com.sword.gsa.spis.scs.utils.lambda;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

    void accept(T t) throws E;

}
