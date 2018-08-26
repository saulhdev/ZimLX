package org.zimmob.zimlx.util;

public abstract class Provider {
    public static Provider of(Object obj) {
        return new ProviderImpl(obj);
    }

    public abstract Object get();

    static final class ProviderImpl extends Provider {
        final Object value;

        ProviderImpl(Object obj) {
            this.value = obj;
        }

        @Override
        public Object get() {
            return this.value;
        }
    }
}