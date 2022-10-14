package br.com.armange.commons.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionsUtil {

    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
