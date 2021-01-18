package nl.avthart.todo.app.configuration.security;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

import nl.avthart.todo.app.configuration.Endpoint;

public enum Role implements ScopeForRoles, Endpoint.Role {
    All(AllAccess.class, null, null), // NOSONAR
    User(null, userScopesGet, userScopesNonGet), // No accessAnnotationClass -> Default Role // NOSONAR
    Admin(AdminAccessRequired.class, adminOnlyScopes, adminOnlyScopes, User); // NOSONAR

    private final Class<? extends Annotation> accessAnnotationClass;
    private final List<Endpoint.Role> impliedRoles;
    private final String[] scopesGet, scopesNonGet; // NOSONAR

    // Sonar is apparently too stupid to understand Enum constructors!
    Role(Class<? extends Annotation> accessAnnotationClass, // NOSONAR
         String[] scopesGet, String[] scopesNonGet,
         Endpoint.Role... impliedRoles) {
        this.accessAnnotationClass = accessAnnotationClass;
        this.impliedRoles = Arrays.asList( impliedRoles);
        this.scopesNonGet = scopesNonGet;
        this.scopesGet = scopesGet;
    }

    /**
     * Get the Scopes for the Get request method (null means permit All)
     *
     * @return Scopes for the Get request method or null
     */
    public String[] getScopesGet() {
        return scopesGet;
    }

    /**
     * Get the Scopes for the non-Get request methods (null means permit All)
     *
     * @return Scopes for the non-Get request methods or null
     */
    public String[] getScopesNonGet() {
        return scopesNonGet;
    }

    @Override
    public boolean isDefaultRole() {
        return (accessAnnotationClass == null);
    }

    @Override
    public boolean isAccessAnnotationClass(Class<? extends Annotation> accessAnnotationClass) {
        return (this.accessAnnotationClass == accessAnnotationClass);
    }

    @Override
    public List<Endpoint.Role> impliedRoles() {
        return impliedRoles;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface AllAccess {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface AdminAccessRequired {
    }

    public static List<Endpoint.Role> asEndpointRoles() {
        return Arrays.asList(values());
    }
}
