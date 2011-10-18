package org.jetbrains.jet.lang.resolve.calls.autocasts;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.types.JetStandardClasses;
import org.jetbrains.jet.lang.types.JetType;

/**
 * @author abreslav
 */
public class DataFlowValue {
    
    public static final DataFlowValue NULL = new DataFlowValue(new Object(), JetStandardClasses.getNullableNothingType(), false, Nullability.NULL);
    public static final DataFlowValue NULLABLE = new DataFlowValue(new Object(), JetStandardClasses.getNullableAnyType(), false, Nullability.UNKNOWN);

    private final boolean stableIdentifier;
    private final JetType type;
    private final Object id;
    private final Nullability immanentNullability;

    // Use DataFlowValueFactory
    /*package*/ DataFlowValue(Object id, JetType type, boolean stableIdentifier, Nullability immanentNullability) {
        this.stableIdentifier = stableIdentifier;
        this.type = type;
        this.id = id;
        this.immanentNullability = immanentNullability;
    }

    @NotNull
    public Nullability getImmanentNullability() {
        return immanentNullability;
    }

    /**
     * Stable identifier is a non-literal value that is statically known to be immutable
     * @return
     */
    public boolean isStableIdentifier() {
        return stableIdentifier;
    }

    @NotNull
    public JetType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataFlowValue that = (DataFlowValue) o;

        if (stableIdentifier != that.stableIdentifier) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return (stableIdentifier ? "stable " : "unstable ") + (id == null ? null : id.toString()) + " " + immanentNullability;
    }

    @Override
    public int hashCode() {
        int result = (stableIdentifier ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}