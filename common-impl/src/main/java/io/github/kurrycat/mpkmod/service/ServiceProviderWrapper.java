package io.github.kurrycat.mpkmod.service;

import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.util.LogUtil;

import java.io.StringWriter;
import java.util.Optional;

public record ServiceProviderWrapper(ServiceProvider inner, int id) implements ServiceProvider {
    @Override
    public Object provide() {
        return inner.provide();
    }

    @Override
    public Class<?> type() {
        return inner.type();
    }

    @Override
    public String name() {
        return inner.name();
    }

    @Override
    public Optional<String> invalidReason() {
        Optional<String> reason;
        try {
            reason = inner.invalidReason();
        } catch (Exception e) {
            StringWriter builder = new StringWriter();
            builder.append("Exception while checking invalid reason: \n");
            LogUtil.appendPrefixedException(builder, "\t\t", e);
            reason = Optional.of(builder.toString());
        }
        return reason;
    }

    @Override
    public int priority() {
        return inner.priority();
    }
}
