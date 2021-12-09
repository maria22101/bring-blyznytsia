package com.blyznytsia.bring;

import com.blyznytsia.bring.context.ApplicationContext;


/**
 * {@link Bring} class instantiates ApplicationContext, calls its init() method for
 * further configuration and returns the configured instance
 */
public class Bring {

    private static ApplicationContext applicationContext = new ApplicationContext();

    public static ApplicationContext bringContext() {
        applicationContext.init();
        return applicationContext;
    }
}
