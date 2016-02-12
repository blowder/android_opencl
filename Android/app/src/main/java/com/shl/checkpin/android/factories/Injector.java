package com.shl.checkpin.android.factories;

import dagger.ObjectGraph;

/**
 * Created by sesshoumaru on 11.02.16.
 */
public class Injector {
    public static ObjectGraph graph;

    public static void init(Object... modules) {
        graph = ObjectGraph.create(modules);
    }

    public static void inject(Object target) {
        graph.inject(target);
    }
}
