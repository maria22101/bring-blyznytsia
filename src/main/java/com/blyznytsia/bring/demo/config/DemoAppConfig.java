package com.blyznytsia.bring.demo.config;

import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.blyznytsia.bring.demo.classes",
                "com.blyznytsia.bring.demo.classes_x",
                "com.blyznytsia.bring.demo.classes_y"})
public class DemoAppConfig {
}
