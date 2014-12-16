package de.dentrassi.pm.storage.service.internal;

@FunctionalInterface
public interface ThrowingConsumer<T>
{
    public void accept ( T t ) throws Exception;
}
