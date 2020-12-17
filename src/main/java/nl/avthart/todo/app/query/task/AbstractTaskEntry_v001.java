package nl.avthart.todo.app.query.task;

import java.util.Objects;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractTaskEntry_v001 {
    protected String createdHour; // 2020-11-19T13Z
    protected String username;
    protected String title;
    protected boolean completed;
    protected boolean starred;

    protected AbstractTaskEntry_v001( AbstractTaskEntry_v001 fields ) {
        this( fields.createdHour,
              fields.username,
              fields.title,
              fields.completed,
              fields.starred );
    }

    public boolean isEquivalent( AbstractTaskEntry_v001 them ) {
        return (them != null)
               && Objects.equals( this.createdHour, them.createdHour )
               && Objects.equals( this.username, them.username )
               && Objects.equals( this.title, them.title )
               && Objects.equals( this.completed, them.completed )
               && Objects.equals( this.starred, them.starred )
                ;
    }

    public String delta( @NotNull AbstractTaskEntry_v001 them ) {
        StringBuilder sb = new StringBuilder()
                .append( this.getClass().getSimpleName() ).append( " != " ).append( them.getClass().getSimpleName() ).append( ":" );
        deltaField( sb, "createdHour", this.createdHour, them.createdHour );
        deltaField( sb, "   username", this.username, them.username );
        deltaField( sb, "      title", this.title, them.title );
        deltaField( sb, "  completed", this.completed, them.completed );
        deltaField( sb, "    starred", this.starred, them.starred );
        return sb.toString();
    }

    private static void deltaField( StringBuilder sb, String fieldName, Object thisField, Object themField ) {
        if ( !Objects.equals( thisField, themField ) ) {
            sb.append( "\n    " ).append( fieldName ).append( ": " );
            addendFieldValue( sb, thisField );
            sb.append( " != " );
            addendFieldValue( sb, themField );
        }
    }

    private static void addendFieldValue( StringBuilder sb, Object value ) {
        if ( value instanceof String ) {
            sb.append( "'" ).append( value ).append( "'" );
        } else {
            sb.append( value ); // includes null
        }
    }
}