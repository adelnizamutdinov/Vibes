package com.companyname.appname.dagger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.stiggpwnz.vibes.dagger.Dagger.getAppScope;
import static com.stiggpwnz.vibes.dagger.Dagger.getObjectGraph;
import static com.stiggpwnz.vibes.dagger.Dagger.getUiScope;
import static org.junit.Assert.assertEquals;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DaggerTest {
    @Test public void testGetAppScope() throws Exception {

    }

    @Test public void testGetUiScope() throws Exception {
        try {
            getUiScope(Robolectric.application);
        } catch (ClassCastException e) {

        }
    }

    @Test public void testGetObjectGraph() throws Exception {
        assertEquals(getObjectGraph(Robolectric.application), getAppScope(Robolectric.application));
    }

    @Test public void testInject() throws Exception {

    }
}
