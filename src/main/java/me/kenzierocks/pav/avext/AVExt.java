package me.kenzierocks.pav.avext;

import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public abstract class AVExt extends AutoValueExtension {

    @Override
    public final String generateClass(Context context, String className, String classToExtend, boolean isFinal) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className).superclass(ClassName.get(context.packageName(), classToExtend));
        if (isFinal) {
            builder.addModifiers(Modifier.FINAL);
        }
        addConstructor(context, builder);
        return JavaFile.builder(context.packageName(), makeTypeSpec(context, builder))
                .indent("    ")
                .skipJavaLangImports(true)
                .build().toString();
    }

    protected void addConstructor(Context context, TypeSpec.Builder builder) {
        MethodSpec.Builder constr = MethodSpec.constructorBuilder();
        context.properties().forEach((name, element) -> {
            ParameterSpec.Builder param = ParameterSpec.builder(TypeName.get(element.getReturnType()), name);
            param.addAnnotations(Iterables.transform(element.getAnnotationMirrors(), AnnotationSpec::get));
            constr.addParameter(param.build());
        });
        // Joins all names together with ',' and wraps in 'super(' ')'
        constr.addStatement(context.properties().keySet().stream().collect(Collectors.joining(", ", "super(", ")")));
        builder.addMethod(constr.build());
    }

    public abstract TypeSpec makeTypeSpec(Context context, TypeSpec.Builder input);

}
