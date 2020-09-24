package dev.buildtool.tp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks the plugin class to be loaded. The plugin must implement {@link PreviewProvider}.
 * Trajectory calculation is done by utilizing Entity classes you provide which implement {@link PreviewEntity}.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface PreviewPlugin
{
    /**
     * Mod identifier for which given plugin is intended
     */
    String mod();
}
