package org.javers.core.metamodel.type;

import org.javers.common.collections.EnumerableFunction;
import java.util.Optional;
import org.javers.core.metamodel.object.OwnerContext;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Collection or Array or Map
 * @author bartosz walacik
 */
public abstract class EnumerableType extends JaversType {

    public EnumerableType(Type baseJavaType, int expectedArgs) {
        super(baseJavaType, Optional.<String>empty(), expectedArgs);
    }

    /**
     * OwnerContext aware version of {@link #map(Object, EnumerableFunction, OwnerContext)}
     *
     * @return immutable Enumerable
     */
    public abstract Object map(Object sourceEnumerable, EnumerableFunction mapFunction, OwnerContext owner);

    /**
     * Returns new instance of Enumerable with items from sourceEnumerable mapped by mapFunction.
     */
    public abstract Object map(Object sourceEnumerable, Function mapFunction);

    public abstract boolean isEmpty(Object container);
}
