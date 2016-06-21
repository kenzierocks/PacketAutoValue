package me.kenzierocks.pav.avext;

import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public final class Util {

    public static String uppercaseFirstLetter(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public static TypeMirror getAnnotationClassValue(TypeElement annotated, Class<?> annotationClass, String value) {
        AnnotationMirror am = getAnnotationMirror(annotated, annotationClass);
        if (am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, value);
        if (av == null) {
            return null;
        } else {
            return (TypeMirror) av.getValue();
        }
    }

    private Util() {
    }

}
