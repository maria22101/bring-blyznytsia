package com.blyznytsia.bring;

import com.blyznytsia.bring.context.ApplicationContext;

public class Bring {

    private static ApplicationContext applicationContext = new ApplicationContext();

    public static ApplicationContext bringContext() {
        applicationContext.init();
        return applicationContext;
    }
}
