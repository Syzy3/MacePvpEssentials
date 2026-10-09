package dev.macepvpessentials;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

/**
 * A shield's components plus the Shield Status tint, carried from the moment the game looks at
 * the held shield to the moment it draws it. Behaves exactly like the components it wraps.
 */
public record TintedComponents(DataComponentMap delegate, int tint) implements DataComponentMap {

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        return delegate.get(type);
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        return delegate.keySet();
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        return delegate.iterator();
    }
}
