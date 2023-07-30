package com.dev.damagehandler.events.attack_handle.attack_priority;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AttackHandle {
    int priority();
    boolean ignoreCancelled() default false;
}
