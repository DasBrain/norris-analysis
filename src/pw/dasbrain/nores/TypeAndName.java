package pw.dasbrain.nores;

import java.util.Objects;

public final class TypeAndName {
    private final String type;
    private final String name;
    
    public TypeAndName(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public String type() {
        return type;
    }

    public String name() {
        return name;
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeAndName other = (TypeAndName) obj;
        return Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }
    
    @Override
    public String toString() {
        return name + type;
    }
}
