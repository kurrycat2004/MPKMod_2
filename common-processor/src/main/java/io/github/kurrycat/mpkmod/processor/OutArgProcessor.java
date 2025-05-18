package io.github.kurrycat.mpkmod.processor;

import com.google.auto.service.AutoService;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import io.github.kurrycat.mpkmod.annotation.OutArg;
import io.github.kurrycat.mpkmod.processor.util.AnnotationUtil;

import javax.annotation.processing.AbstractProcessor;
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
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class OutArgProcessor extends AbstractProcessor {
    private Trees trees;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                OutArg.class.getCanonicalName()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.trees = Trees.instance(env);
        this.messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Processing OutArg annotations...");
        for (Element paramElt : roundEnv.getElementsAnnotatedWith(OutArg.class)) {
            if (paramElt.getKind() != ElementKind.PARAMETER) continue;

            VariableElement var = (VariableElement) paramElt;
            ExecutableElement method = (ExecutableElement) var.getEnclosingElement();
            Element classElt = method.getEnclosingElement();

            /*if (isSuppressed(var) || isSuppressed(method) || isSuppressed(classElt)) {
                continue;
            }*/

            String paramName = var.getSimpleName().toString();
            TypeMirror tm = var.asType();
            TypeElement typeElt = (TypeElement) processingEnv.getTypeUtils().asElement(tm);

            // collect all public, non-static fields
            Set<String> allFields = typeElt.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.FIELD)
                    .map(VariableElement.class::cast)
                    .filter(f -> f.getModifiers().contains(Modifier.PUBLIC))
                    .filter(f -> !f.getModifiers().contains(Modifier.STATIC))
                    .map(f -> f.getSimpleName().toString())
                    .collect(Collectors.toSet());

            // scan method body
            MethodTree tree = trees.getTree(method);
            FieldAssignmentScanner scanner = new FieldAssignmentScanner(paramName);
            scanner.scan(tree.getBody(), null);
            Set<String> assigned = scanner.getAssignedFieldNames();

            Set<String> missing = new HashSet<>(allFields);
            missing.removeAll(assigned);
            if (!missing.isEmpty()) {
                AnnotationUtil.printMessage(
                        messager,
                        Diagnostic.Kind.WARNING,
                        "Missing assignments to fields of '" + tm + "': " + missing,
                        paramElt,
                        OutArg.class,
                        null
                );
            }
        }
        return true;
    }

    /*private boolean isSuppressed(Element e) {
        var sw = e.getAnnotationSuppressWarnings();
        if (sw == null) return false;
        for (String key : sw.value()) {
            if (KEY.equals(key)) return true;
        }
        return false;
    }*/

    private static class FieldAssignmentScanner extends TreePathScanner<Void, Void> {
        private final String targetParam;
        private final Set<String> assigned = new HashSet<>();

        FieldAssignmentScanner(String paramName) {
            this.targetParam = paramName;
        }

        @Override
        public Void visitAssignment(AssignmentTree asg, Void unused) {
            ExpressionTree lhs = asg.getVariable();
            if (lhs instanceof MemberSelectTree sel) {
                if (sel.getExpression().toString().equals(targetParam)) {
                    assigned.add(sel.getIdentifier().toString());
                }
            }
            return super.visitAssignment(asg, unused);
        }

        Set<String> getAssignedFieldNames() {
            return assigned;
        }
    }
}
