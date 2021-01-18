package nl.avthart.todo.app.configuration.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface ScopeForRoles { // NOSONAR
    List<String> userScopesRead = Collections.singletonList( "read" );

    List<String> userScopesWrite = Collections.singletonList( "write" );

    List<String> adminScopes = Collections.singletonList( "admin" );

    String[] userScopesGet = within( userScopesRead, userScopesWrite, adminScopes ); // NOSONAR
    String[] userScopesNonGet = within( userScopesWrite, adminScopes ); // NOSONAR
    String[] adminOnlyScopes = within( adminScopes ); // NOSONAR

    @SafeVarargs
    static String[] within( List<String>... lists ) {
        return Stream.of( lists )
                .flatMap( Collection::stream )
                .toArray( String[]::new );
    }
}
