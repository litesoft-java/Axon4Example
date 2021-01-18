package nl.avthart.todo.app.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import lombok.AllArgsConstructor;

public class FieldAccessors<T> {

    @AllArgsConstructor
    private static class Field<T, V> {
        private String name;
        private Function<T, V> accessor;
        private BiConsumer<T, V> mutator;
    }

    private final Field<T, ?>[] fields;

    private FieldAccessors( Field<T, ?>[] fields ) {
        this.fields = fields;
    }

    @SuppressWarnings("unused")
    public static <T> Builder<T> builder( Class<T> type ) {
        return new Builder<>();
    }

    public static class Builder<T> {
        List<Field<T, ?>> fields = new ArrayList<>();

        public <V> Builder<T> add( String name, Function<T, V> accessor, BiConsumer<T, V> mutator ) {
            if ( accessor == null ) {
                throw new NullPointerException( "Accessor" );
            }
            if ( name == null ) {
                throw new NullPointerException( "Name" );
            }
            name = name.trim();
            if ( name.isEmpty() ) {
                throw new IllegalArgumentException( "Name empty" );
            }
            fields.add( new Field<>( name, accessor, mutator ) );
            return this;
        }

        @SuppressWarnings({"StringConcatenationInLoop", "unchecked"})
        public FieldAccessors<T> build() {
            if ( fields.isEmpty() ) {
                throw new IllegalArgumentException( "No fields added" );
            }
            int maxLength = fields.stream().map( f -> f.name.length() ).max( Integer::compareTo ).orElse( -1 );
            for ( Field<T, ?> field : fields ) {
                while ( field.name.length() < maxLength ) {
                    field.name = " " + field.name;
                }
            }
            Field<T, ?>[] fieldArray = fields.toArray( new Field[0] );
            return new FieldAccessors<>( fieldArray );
        }
    }

    public <THIS extends T, THEM extends T> boolean areEquivalent( THIS thisInstance, THEM themInstance ) {
        if ( thisInstance == themInstance ) { // same or both null
            return true;
        }
        if ( (thisInstance == null) || (themInstance == null) ) { // can't both be null
            return false;
        }
        for ( Field<T, ?> field : fields ) {
            if ( !Objects.equals( field.accessor.apply( thisInstance ), field.accessor.apply( themInstance ) ) ) {
                return false;
            }
        }
        return true;
    }

    public <THIS extends T, THEM extends T> String delta( THIS thisInstance, THEM themInstance ) {
        boolean different = !areEquivalent( thisInstance, themInstance );
        StringBuilder sb = new StringBuilder();
        sb.append( (thisInstance == null) ? "null" : thisInstance.getClass().getSimpleName() );
        sb.append( different ? " != " : " == " );
        sb.append( (themInstance == null) ? "null" : themInstance.getClass().getSimpleName() );
        if ( different ) {
            for ( Field<T, ?> field : fields ) {
                deltaField( sb, field, thisInstance, themInstance );
            }
        }
        return sb.toString();
    }

    public <THIS extends T, THEM extends T> void updateFirst( THIS firstInstance, THEM secondInstance ) {
        if ( (firstInstance != null) && (secondInstance != null) ) {
            for ( Field<T, ?> field : fields ) {
                updateField( field, firstInstance, firstInstance, secondInstance );
            }
        }
    }

    public <THIS extends T, THEM extends T> void defaultFromSecond( THIS firstInstance, THEM secondInstance ) {
        if ( (firstInstance != null) && (secondInstance != null) ) {
            for ( Field<T, ?> field : fields ) {
                updateField( field, firstInstance, secondInstance, firstInstance );
            }
        }
    }

    public static <V> V loadUpdateField( V existingValue, V newValue ) {
        if ( newValue == null ) {
            return existingValue;
        }
        // If there is an existing value AND the new value is an "effectively" empty string, then return the existing value.
        if ( (existingValue != null) && (newValue instanceof String) ) {
            if ( newValue.toString().trim().isEmpty() ) { // newValue
                return existingValue;
            }
        }
        return newValue;
    }

    private <V, UPDATE extends T, DEF extends T, NEW extends T> void updateField( Field<T, V> field,
                                                                                  UPDATE updateInstance,
                                                                                  DEF defInstance,
                                                                                  NEW newInstance ) {
        BiConsumer<T, V> mutator = field.mutator;
        if ( mutator != null ) {
            mutator.accept( updateInstance,
                            loadUpdateField( field.accessor.apply( defInstance ),
                                             field.accessor.apply( newInstance ) ) );
        }
    }

    private <THIS extends T, THEM extends T> void deltaField( StringBuilder sb, Field<T, ?> field, THIS thisInstance, THEM themInstance ) {
        Object thisField = extractField( field, thisInstance );
        Object themField = extractField( field, themInstance );
        if ( !Objects.equals( thisField, themField ) ) {
            sb.append( "\n    " ).append( field.name ).append( ": " )
                    .append( thisField ).append( " != " ).append( themField );
        }
    }

    private <O extends T> Object extractField( Field<T, ?> field, O instance ) {
        if ( instance == null ) {
            return "N/A";
        }
        Object value = field.accessor.apply( instance );
        return (value instanceof String) ? new QuotedString( value.toString() ) : value;
    }

    private static class QuotedString {
        private final String value;

        private QuotedString( String value ) {
            this.value = "'" + value + "'";
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals( Object o ) {
            return (this == o) ||
                   ((o instanceof QuotedString) && equals( (QuotedString)o )); // Left to Right
        }

        public boolean equals( QuotedString them ) {
            return (this == them) ||
                   ((them != null) && this.value.equals( them.value ));
        }
    }
}
