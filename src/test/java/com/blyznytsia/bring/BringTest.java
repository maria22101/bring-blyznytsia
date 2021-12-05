package com.blyznytsia.bring;

import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blyznytsia.bring.context.ApplicationContext;

class BringTest {

    @Test
    void bringContext() {

        // given: field of ApplicationContext type is set
        var fields = Bring.class.getDeclaredFields();
        var applicationContextField = Arrays.stream(fields)
                .filter(field -> field.getType().getName().equals(ApplicationContext.class.getName()))
                .findFirst()
                .orElseThrow();
        applicationContextField.setAccessible(true);

        var mockedApplicationContext = Mockito.mock(ApplicationContext.class);
        try {
            applicationContextField.set(null, mockedApplicationContext);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // when:
        var context = Bring.bringContext();

        // then:
        verify(mockedApplicationContext).init();
    }
}
