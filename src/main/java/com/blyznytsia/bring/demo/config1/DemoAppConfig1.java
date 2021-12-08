package com.blyznytsia.bring.demo.config1;

import com.blyznytsia.bring.context.annotation.Bean;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.demo.classes_beans.ClassBean;

@Configuration
@ComponentScan({"com.blyznytsia.bring.demo.classes",
                "com.blyznytsia.bring.demo.classes_x",
                "com.blyznytsia.bring.demo.classes_y"})
public class DemoAppConfig1 {

    @Bean
    public ClassBean classBean() {
        return new ClassBean();
    }
}
