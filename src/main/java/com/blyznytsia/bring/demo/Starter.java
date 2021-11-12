package com.blyznytsia.bring.demo;

import com.blyznytsia.bring.context.ApplicationContext;

/**
 * Class contains a single method to be called by the client who can input the application root package
 * To be discussed if the class is necessary
 */
public class Starter {

    public static ApplicationContext run(String packageToScan) {
        var applicationContext = new ApplicationContext();

        // create BeanFactory by passing th context to it (?)

        return applicationContext;
    }
}
