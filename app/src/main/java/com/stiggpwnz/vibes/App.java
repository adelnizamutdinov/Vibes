package com.stiggpwnz.vibes;

import android.app.Application;
import android.os.StrictMode;

import com.stiggpwnz.vibes.util.CrashReportingTree;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import lombok.Getter;
import timber.log.Timber;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
public class App extends Application implements Injector {
    @Getter ObjectGraph objectGraph;

    @Override public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
//      Crittercism.initialize(getApplicationContext(), getString(R.string.crittercism_app_id));
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());
        objectGraph = ObjectGraph.create(getModules().toArray());
    }

    // we create a list containing the MyModule Injector module
    // later we can add any other modules to it (for testing)
    @NotNull protected List<Object> getModules() {
        return new ArrayList<>(Arrays.asList(new AppDaggerModule(this)));
    }

}
