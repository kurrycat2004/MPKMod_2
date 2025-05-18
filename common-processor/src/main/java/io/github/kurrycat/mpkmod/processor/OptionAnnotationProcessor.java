package io.github.kurrycat.mpkmod.processor;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.annotation.Option;
import io.github.kurrycat.mpkmod.processor.util.AnnotationUtil;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
public class OptionAnnotationProcessor extends AbstractProcessor {
    private final Map<String, String> fieldEntries = new HashMap<>();
    private final List<String> listenerEntries = new ArrayList<>();
    private Messager messager;
    private Filer filer;
    private boolean written = false;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                Option.Field.class.getCanonicalName(),
                Option.ChangeListener.class.getCanonicalName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.messager = env.getMessager();
        this.filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Processing Option annotations...");
        if (roundEnv.processingOver()) {
            if (!written) {
                List<String> fieldEntries = new ArrayList<>(this.fieldEntries.values());
                Collections.sort(fieldEntries);
                Collections.sort(listenerEntries);
                writeEntries("META-INF/mpk-option-fields.txt", fieldEntries);
                writeEntries("META-INF/mpk-option-listeners.txt", listenerEntries);
                written = true;
            }
            return false;
        }

        // Collect @Field
        for (Element elt : roundEnv.getElementsAnnotatedWith(Option.Field.class)) {
            if (elt.getKind() != ElementKind.FIELD) {
                error(elt, "@Field can only be applied to fields");
                continue;
            }
            VariableElement var = (VariableElement) elt;
            if (!var.getModifiers().contains(Modifier.PUBLIC)) {
                error(var, "@Field '%s' must be public", var.getSimpleName());
            }
            String className = ((TypeElement) var.getEnclosingElement())
                    .getQualifiedName().toString();
            String fieldName = var.getSimpleName().toString();
            fieldEntries.put(fieldName, className + "|" + fieldName);
        }

        // Collect @ChangeListener
        for (Element elt : roundEnv.getElementsAnnotatedWith(Option.ChangeListener.class)) {
            if (elt.getKind() != ElementKind.METHOD) {
                error(elt, "@ChangeListener can only be applied to methods");
                continue;
            }
            ExecutableElement method = (ExecutableElement) elt;
            if (!method.getModifiers().contains(Modifier.PUBLIC)) {
                error(method, "@ChangeListener '%s' must be public", method.getSimpleName());
            }
            if (!method.getParameters().isEmpty()) {
                error(method, "@ChangeListener '%s' must have no parameters", method.getSimpleName());
            }
            if (method.getReturnType().getKind() != TypeKind.VOID) {
                error(method, "@ChangeListener '%s' must return void", method.getSimpleName());
            }
            Option.ChangeListener ann = method.getAnnotation(Option.ChangeListener.class);
            String className = ((TypeElement) method.getEnclosingElement())
                    .getQualifiedName().toString();
            String methodName = method.getSimpleName().toString();
            String fieldRef = ann.field();
            if (fieldRef.isEmpty() || !fieldEntries.containsKey(fieldRef)) {
                AnnotationUtil.printMessage(
                        messager,
                        Diagnostic.Kind.ERROR,
                        String.format("@ChangeListener '%s' must reference a valid field", method.getSimpleName()),
                        method,
                        Option.ChangeListener.class,
                        "field"
                );
            }
            listenerEntries.add(className + "|" + methodName);
        }

        return true;
    }

    private void writeEntries(String path, List<String> entries) {
        try {
            FileObject file = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    path
            );
            try (Writer w = file.openWriter()) {
                for (String line : entries) {
                    w.write(line);
                    w.write("\n");
                }
            }
        } catch (IOException e) {
            messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to write " + path + ": " + e.getMessage()
            );
        }
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e
        );
    }
}