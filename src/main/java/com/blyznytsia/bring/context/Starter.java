package com.blyznytsia.bring.context;

import com.blyznytsia.bring.context.services.BeanFactory;
import com.blyznytsia.bring.context.services.impl.BeanFactoryImpl;

public class Starter {

    public static ApplicationContext run(String packageToScan) {
        var applicationContext = new ApplicationContext();

//        var beanFactory = new BeanFactoryImpl(applicationContext);

        // create BeanFactory by passing th context to it

        return applicationContext;
    }
}
