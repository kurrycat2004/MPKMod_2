package io.github.kurrycat.mpkmod.processor.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Map;

public class AnnotationUtil {
    public static AnnotationMirror getAnnotationMirror(Element el, Class<?> annotationClass) {
        String annotationName = annotationClass.getName().replaceAll("\\$", ".");
        for (AnnotationMirror mirror : el.getAnnotationMirrors()) {
            String fqName = ((TypeElement) mirror.getAnnotationType().asElement())
                    .getQualifiedName().toString();
            if (fqName.equals(annotationName)) {
                return mirror;
            }
        }
        return null;
    }

    public static AnnotationValue getAnnotationValue(AnnotationMirror mirror, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void printMessage(Messager messager,
                                    Diagnostic.Kind kind,
                                    CharSequence msg,
                                    Element e,
                                    Class<?> annotationClass,
                                    String fieldName) {
        AnnotationMirror mirror;
        if (annotationClass == null || (mirror = getAnnotationMirror(e, annotationClass)) == null) {
            messager.printMessage(kind, msg, e);
            return;
        }
        AnnotationValue value;
        if (fieldName == null || (value = getAnnotationValue(mirror, fieldName)) == null) {
            messager.printMessage(kind, msg, e, mirror);
            return;
        }
        messager.printMessage(kind, msg, e, mirror, value);
    }
}
