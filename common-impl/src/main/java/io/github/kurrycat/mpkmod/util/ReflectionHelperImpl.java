package io.github.kurrycat.mpkmod.util;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.util.ReflectionHelper;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public final class ReflectionHelperImpl implements ReflectionHelper {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ReflectionHelper> {
        public Provider() {
            super(ReflectionHelperImpl::new, ReflectionHelper.class);
        }
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Override
    public <T, V> Optional<FieldAccessor<T, V>> lookupField(
            Class<T> cls,
            Class<V> type,
            String... fieldNames) {
        for (String name : fieldNames) {
            try {
                Field field = cls.getDeclaredField(name);
                if (!type.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                MethodHandle getter = LOOKUP.unreflectGetter(field);
                MethodHandle setter = LOOKUP.unreflectSetter(field);
                return Optional.of(new FieldAccessorImpl<>(getter, setter));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return Optional.empty();
    }

    @Override
    public <T, R> Optional<MethodInvoker<T, R>> lookupMethod(
            Class<T> cls,
            Class<R> returnType,
            Class<?>[] paramTypes,
            String... methodNames) {
        for (String name : methodNames) {
            try {
                Method method = cls.getDeclaredMethod(name, paramTypes);
                if (!returnType.isAssignableFrom(method.getReturnType())) continue;
                method.setAccessible(true);
                MethodHandle handle = LOOKUP.unreflect(method);
                return Optional.of(new MethodInvokerImpl<>(handle));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<ConstructorInvoker<T>> lookupConstructor(
            Class<T> cls,
            Class<?>[] paramTypes,
            String... constructorNames) {
        try {
            Constructor<T> ctor = cls.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            MethodHandle handle = LOOKUP.unreflectConstructor(ctor);
            return Optional.of(new ConstructorInvokerImpl<>(handle));
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }

    private record FieldAccessorImpl<T, V>(MethodHandle getter, MethodHandle setter) implements FieldAccessor<T, V> {
        @SuppressWarnings("unchecked")
        @Override
        public V get(T obj) {
            try {
                return (V) getter.invoke(obj);
            } catch (Throwable t) {
                throw new ReflectionRuntimeException("Failed to get field", t);
            }
        }

        @Override
        public void set(T obj, V value) {
            try {
                setter.invoke(obj, value);
            } catch (Throwable t) {
                throw new ReflectionRuntimeException("Failed to set field", t);
            }
        }
    }

    private record MethodInvokerImpl<T, R>(MethodHandle handle) implements MethodInvoker<T, R> {
        @SuppressWarnings("unchecked")
        @Override
        public R invoke(T obj, Object... args) {
            try {
                Object result = handle.invokeWithArguments(prepend(obj, args));
                return (R) result;
            } catch (Throwable t) {
                throw new ReflectionRuntimeException("Failed to invoke method", t);
            }
        }

        private Object[] prepend(Object first, Object[] rest) {
            Object[] arr = new Object[rest.length + 1];
            arr[0] = first;
            System.arraycopy(rest, 0, arr, 1, rest.length);
            return arr;
        }
    }

    private record ConstructorInvokerImpl<T>(MethodHandle handle) implements ConstructorInvoker<T> {
        @SuppressWarnings("unchecked")
        @Override
        public T newInstance(Object... args) {
            try {
                return (T) handle.invokeWithArguments(args);
            } catch (Throwable t) {
                throw new ReflectionRuntimeException("Failed to invoke constructor", t);
            }
        }
    }

    public static class ReflectionRuntimeException extends RuntimeException {
        public ReflectionRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
