package nl.avthart.todo.app.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldAccessorsTest {

    @Data
    @AllArgsConstructor
    private abstract static class A {
        protected String aString;
        protected boolean aBool;

        private static final FieldAccessors<A> FIELD_ACCESSORS =
                FieldAccessors.builder( A.class )
                        .add( A::getAString, "aString" )
                        .add( A::isABool, "aBool" )
                        .build();

        protected A( A fields ) {
            this( fields.aString, fields.aBool );
        }

        public boolean isEquivalent( A them ) {
            return FIELD_ACCESSORS.areEquivalent( this, them );
        }

        public String delta( A them ) {
            return FIELD_ACCESSORS.delta( this, them );
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    private static class B extends A {
        private final char aChar = 'B';

        public B( String aString, boolean aBool ) {
            super( aString, aBool );
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    private static class C extends A {
        private final char aChar = 'C';

        public C( String aString, boolean aBool ) {
            super( aString, aBool );
        }

        public C( A fields ) {
            super( fields );
        }
    }

    @Test
    void it() {
        B bf = new B( null, false );
        B b1t = new B( "1", true );
        B b2f = new B( "2", false );

        C cf = new C( null, false );
        C c1t = new C( "1", true );
        C c2f = new C( "2", false );

        assertNotEquals( bf, b1t );
        assertNotEquals( bf, b2f );
        assertNotEquals( b1t, b2f );

        assertNotEquals( cf, c1t );
        assertNotEquals( cf, c2f );
        assertNotEquals( c1t, c2f );

        assertNotEquals( bf, c1t );
        assertNotEquals( bf, c2f );
        assertNotEquals( bf, c2f );

        assertNotEquals( b1t, c1t );
        assertNotEquals( b1t, c2f );
        assertNotEquals( b1t, c2f );

        assertNotEquals( b2f, c1t );
        assertNotEquals( b2f, c2f );
        assertNotEquals( b2f, c2f );

        assertTrue( bf.isEquivalent( cf ) );
        assertTrue( cf.isEquivalent( bf ) );

        assertTrue( b1t.isEquivalent( c1t ) );
        assertTrue( c1t.isEquivalent( b1t ) );

        assertTrue( b2f.isEquivalent( c2f ) );
        assertTrue( c2f.isEquivalent( b2f ) );

        assertEquals( "B == C", bf.delta( cf ) );
        assertEquals( "B == C", b1t.delta( c1t ) );
        assertEquals( "B == C", b2f.delta( c2f ) );

        deltaIs( bf, c1t,
                 "    aString: null != '1'",
                 "      aBool: false != true"
        );
        deltaIs( bf, c2f,
                 "    aString: null != '2'"
        );

        deltaIs( b1t, cf,
                 "    aString: '1' != null",
                 "      aBool: true != false"
        );
        deltaIs( b1t, c2f,
                 "    aString: '1' != '2'",
                 "      aBool: true != false"
        );

        deltaIs( b2f, cf,
                 "    aString: '2' != null"
        );
        deltaIs( b2f, c1t,
                 "    aString: '2' != '1'",
                 "      aBool: false != true"
        );

        C bfCopy = new C( bf );
        assertNotEquals( bf, bfCopy );
        assertTrue( bf.isEquivalent( bfCopy ) );
        assertEquals( "B == C", bf.delta( bfCopy ) );
    }

    private void deltaIs( B b, C c, String... expected ) {
        StringBuilder sb = new StringBuilder().append( "B != C" );
        for ( String s : expected ) {
            sb.append( '\n' ).append( s );
        }

        assertEquals( sb.toString(), b.delta( c ) );
    }
}