package nl.avthart.todo.app.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface Fields {
    interface HasValuesMap {
        Map<String, Object> getValuesMap();

        default boolean isEquivalent( HasValuesMap them ) {
            return (them != null) && Objects.equals( this.getValuesMap(), them.getValuesMap() ); // Left to Right!
        }
    }

    interface Field {
        String name();

        Class<?> getDataType();
    }

    static Factory factory() {
        return new Factory();
    }

    class Factory {
        public Factory add( Field field ) {
            checkClosed();
            if ( field != null ) {
                String name = field.name();
                Class<?> dataType = field.getDataType();
                if ( (name == null) || name.isEmpty() ) {
                    throw new IllegalArgumentException( "Field has no name" );
                }
                if ( !isValidJavaIdentifier( name ) ) {
                    throw new IllegalArgumentException( "Field name '" + name + "', not valid Java Identifier" );
                }
                if ( null == dataType ) {
                    throw new IllegalArgumentException( "No Field Data Type for Field w/ name: " + name );
                }
                dataTypeByFieldName.put( name, dataType );
            }
            return this;
        }

        public Factory add( List<Field> fields ) {
            checkClosed();
            if ( fields != null ) {
                fields.forEach( this::add );
            }
            return this;
        }

        public Factory add( Field... fields ) {
            checkClosed();
            if ( fields != null ) {
                for ( Field field : fields ) {
                    add( field );
                }
            }
            return this;
        }

        public Factory close() {
            closed = true;
            return this;
        }

        public Tracker newTracker() {
            return new Tracker( close().dataTypeByFieldName );
        }

        private final Map<String, Class<?>> dataTypeByFieldName = new LinkedHashMap<>();
        private volatile boolean closed;

        private Factory() {
        }

        private void checkClosed() {
            if ( closed ) {
                throw new IllegalStateException( "Attempt to add a Field to a 'Closed' Factory" );
            }
        }

        private static boolean isValidJavaIdentifier( String name ) {
            PrimitiveIterator.OfInt points = name.codePoints().iterator();
            if ( !Character.isJavaIdentifierStart( points.nextInt() ) ) { // name assumed to NOT be empty
                return false;
            }
            while ( points.hasNext() ) {
                if ( !Character.isJavaIdentifierPart( points.nextInt() ) ) {
                    return false;
                }
            }
            return true;
        }
    }

    class Tracker implements HasValuesMap {
        public Map<String, Object> getValuesMap() {
            return Collections.unmodifiableMap( valueByFieldName );
        }

        public void setEntry( String fieldName, Object value ) {
            setEntryValidated( fieldName, checkFieldName( fieldName ), value );
        }

        public void setEntry( Field field, Object value ) {
            Class<?> dataType = checkField( field );
            setEntryValidated( field.name(), dataType, value );
        }

        public <FT> FT getEntry( String fieldName ) {
            return getEntryValidated( checkAndReturnFieldName( fieldName ) );
        }

        public <FT> FT getEntry( Field field ) {
            return getEntryValidated( checkAndReturnFieldName( field ) );
        }

        public void clear( String fieldName ) {
            clearValidated( checkAndReturnFieldName( fieldName ) );
        }

        public void clear( Field field ) {
            clearValidated( checkAndReturnFieldName( field ) );
        }

        public boolean hasValueFor( String fieldName ) {
            return hasValueForValidated( checkAndReturnFieldName( fieldName ) );
        }

        public boolean hasValueFor( Field field ) {
            return hasValueForValidated( checkAndReturnFieldName( field ) );
        }

        private Tracker( Map<String, Class<?>> dataTypeByFieldName ) {
            this.dataTypeByFieldName = dataTypeByFieldName;
        }

        @Override
        public boolean equals( Object obj ) {
            throw new UnsupportedOperationException( "equals not supported on Fields.Tracker, use isEquivalent for 'set equivalency'" );
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException( "hashCode not supported on Fields.Tracker" );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            boolean any = false;
            for ( String key : dataTypeByFieldName.keySet() ) {
                if ( valueByFieldName.containsKey( key ) ) {
                    if ( any ) {
                        sb.append( ',' );
                    }
                    sb.append( "\n    " );
                    sb.append( key );
                    sb.append( " = " );
                    sb.append( valueByFieldName.get( key ) );
                    any = true;
                }
            }
            if ( any ) {
                sb.append( '\n' );
            }
            return sb.toString();
        }

        @JsonIgnore
        private final Map<String, Object> valueByFieldName = new HashMap<>();
        @JsonIgnore
        private final Map<String, Class<?>> dataTypeByFieldName;

        private void setEntryValidated( String fieldName, Class<?> dataType, Object value ) {
            if ( value != null ) {
                if ( !dataType.isInstance( value ) ) {
                    throw badType( fieldName,
                                   "value.class", value.getClass(),
                                   "not an instance of",
                                   "", dataType );
                }
            }
            valueByFieldName.put( fieldName, value );
        }

        @SuppressWarnings("unchecked")
        private <FT> FT getEntryValidated( String fieldName ) {
            return (FT)valueByFieldName.get( fieldName );
        }

        private void clearValidated( String fieldName ) {
            valueByFieldName.remove( fieldName );
        }

        private boolean hasValueForValidated( String fieldName ) {
            return valueByFieldName.containsKey( fieldName );
        }

        private String wrapClass( Class<?> Klass ) {
            return " (" + Klass + ") ";
        }

        private IllegalArgumentException badType( String fieldName,
                                                  String actualWhat, Class<?> actualClass,
                                                  String why,
                                                  String expectedWhat, Class<?> expectedClass ) {
            String message = actualWhat + wrapClass( actualClass ) +
                             why +
                             wrapClass( expectedClass ) + expectedWhat +
                             " for field: " + fieldName;
            return new IllegalArgumentException( message );
        }

        private Class<?> checkFieldName( String fieldName ) {
            Class<?> dataType = dataTypeByFieldName.get( fieldName );
            if ( dataType == null ) {
                throw new IllegalArgumentException( "Field Name (" + fieldName + ") not registered" );
            }
            return dataType;
        }

        private String checkAndReturnFieldName( String fieldName ) {
            checkFieldName( fieldName );
            return fieldName;
        }

        private Class<?> checkField( Field field ) {
            if ( field == null ) {
                throw new NullPointerException( "No 'field' provided" );
            }
            String fieldName = field.name();
            Class<?> fieldDataType = field.getDataType();
            Class<?> registeredDateType = checkFieldName( fieldName );
            if ( registeredDateType != fieldDataType ) { // Require "Identity"!
                throw badType( fieldName,
                               "field Data Type", fieldDataType,
                               "!=",
                               "registered Date Type", registeredDateType );
            }
            return registeredDateType;
        }

        private String checkAndReturnFieldName( Field field ) {
            checkField( field );
            return field.name();
        }
    }
}
