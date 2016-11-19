package com.cmu.miis.capstone;

import com.cmu.miis.capstone.rr.RestApi;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class ApplicationConfig extends Application{
    private Set<Object> singletons;

    public ApplicationConfig() throws NoSuchAlgorithmException {
        HashSet s = new HashSet();

        s.add(new RestApi());
        singletons = Collections.unmodifiableSet(s);
    }

    @Override
    public Set<Object> getSingletons() { return  singletons; }
}
