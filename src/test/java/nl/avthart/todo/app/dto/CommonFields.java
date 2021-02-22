package nl.avthart.todo.app.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CommonFields implements Fields.HasValuesMap {
    public enum Field implements Fields.Field {
        title( String.class ),
        completed( Boolean.class ),
        version( Long.class ),
        lastModified( Instant.class );

        private Class<?> dataType;

        Field( Class<?> dataType ) {
            this.dataType = dataType;
        }

        public Class<?> getDataType() {
            return dataType;
        }
    }

    private static final Fields.Factory FIELDS = Fields.factory()
            .add( Field.values() )
            .close();

    @JsonIgnore
    private final Fields.Tracker fields = FIELDS.newTracker();

    @JsonAnyGetter
    public Map<String, Object> getValuesMap() {
        return fields.getValuesMap();
    }

    @JsonAnySetter
    public void setEntry( String fieldName, Object value ) {
        fields.setEntry( fieldName, value );
    }

    private CommonFields setEntry( Field key, Object value ) {
        fields.setEntry( key, value );
        return this;
    }

    private <FT> FT getEntry( Field key ) {
        return fields.getEntry( key );
    }

    public void clear( Field key ) {
        fields.clear( key );
    }

    public boolean hasValueFor( Field key ) {
        return fields.hasValueFor( key );
    }

    @JsonIgnore
    public String getTitle() {
        return getEntry( Field.title );
    }

    public void setTitle( String value ) {
        withTitle( value );
    }

    public CommonFields withTitle( String value ) {
        return setEntry( Field.title, value );
    }

    @JsonIgnore
    public Boolean getCompleted() {
        return getEntry( Field.completed );
    }

    public void setCompleted( Boolean value ) {
        withTitle( value );
    }

    public CommonFields withTitle( Boolean value ) {
        return setEntry( Field.completed, value );
    }

    @JsonIgnore
    public Long getVersion() {
        return getEntry( Field.version );
    }

    public void setVersion( Long value ) {
        withVersion( value );
    }

    public CommonFields withVersion( Long value ) {
        return setEntry( Field.version, value );
    }

    @JsonIgnore
    public Instant getLastModified() {
        return getEntry( Field.lastModified );
    }

    public void setLastModified( Instant value ) {
        withLastModified( value );
    }

    public CommonFields withLastModified( Instant value ) {
        return setEntry( Field.lastModified, value );
    }

    @Override
    public String toString() {
        return "CommonFields{" +
               fields +
               '}';
    }
}
