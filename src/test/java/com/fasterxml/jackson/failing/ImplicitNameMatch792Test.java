package com.fasterxml.jackson.failing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class ImplicitNameMatch792Test extends BaseMapTest
{
    // Simple introspector that gives generated "ctorN" names for constructor
    // parameters
    static class ConstructorNameAI extends JacksonAnnotationIntrospector
    {
        private static final long serialVersionUID = 1L;

        @Override
        public String findImplicitPropertyName(AnnotatedMember member) {
            if (member instanceof AnnotatedParameter) {
                return String.format("ctor%d", ((AnnotatedParameter) member).getIndex());
            }
            return super.findImplicitPropertyName(member);
        }
    }
    
    @JsonPropertyOrder({ "first" ,"second", "other" })
    static class Issue792Bean
    {
        // Should match implicit name of the first constructor parameter,
        // and thus get renamed as "first" for serialization purposes
        String ctor0;

        public Issue792Bean(@JsonProperty("first") String a,
                @JsonProperty("second") String b) {
            ctor0 = a;
            // ignore second arg
        }

        public int getOther() { return 3; }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    public void testBindingOfImplicitNames() throws Exception
    {
        ObjectMapper m = new ObjectMapper();
        m.setAnnotationIntrospector(new ConstructorNameAI());
        String json = m.writeValueAsString(new Issue792Bean("a", "b"));
        assertEquals(aposToQuotes("{'first':'a','other':3}"), json);
    }
}