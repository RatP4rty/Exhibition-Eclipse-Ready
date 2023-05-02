package exhibition.event;



import java.lang.annotation.*;
	
import exhibition.event.EventListener.Priority;
import exhibition.module.impl.movement.Priority1;


@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {

    byte value() default Priority1.MEDIUM;

	
}