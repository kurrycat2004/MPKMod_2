package io.github.kurrycat.mpkmod.api.util;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

public interface ReflectionHelper {
    static ReflectionHelper instance() {
        return ServiceManager.instance().get(ReflectionHelper.class);
    }

    interface FieldAccessor<T, V> {
        V get(T obj);

        void set(T obj, V value);

        MethodHandle getter();

        MethodHandle setter();
    }

    interface MethodInvoker<T, R> {
        R invoke(T obj, Object... args);

        MethodHandle handle();
    }

    interface ConstructorInvoker<T> {
        T newInstance(Object... args);

        MethodHandle handle();
    }

    <T, V> Optional<FieldAccessor<T, V>> lookupField(Class<T> cls, Class<V> type, String... fieldName);

    <T, R> Optional<MethodInvoker<T, R>> lookupMethod(Class<T> cls, Class<R> type, Class<?>[] paramTypes, String... methodName);

    <T> Optional<ConstructorInvoker<T>> lookupConstructor(Class<T> cls, Class<?>[] paramTypes, String... constructorName);
}
