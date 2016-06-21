package me.kenzierocks.pav.avext;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import me.kenzierocks.pav.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

@AutoService(AutoValueExtension.class)
public class AVExtPacket extends AVExt {

    private static final String READ_PACKET = "readPacket";
    private static final String WRITE_PACKET = "writePacket";

    private Set<ExecutableElement> getMethods(List<? extends Element> enclosedElements) {
        return enclosedElements.stream().filter(e -> e.getKind() == ElementKind.METHOD).map(ExecutableElement.class::cast).collect(Collectors.toSet());
    }

    @Override
    public boolean applicable(Context context) {
        return context.autoValueClass().getInterfaces().stream()
                .map(TypeName::get)
                .anyMatch(Predicate.isEqual(ClassName.get(Packet.class)));
    }

    @Override
    public Set<ExecutableElement> consumeMethods(Context context) {
        return ImmutableSet.of(getWritePacket(context.abstractMethods()));
    }

    private ExecutableElement getWritePacket(Set<ExecutableElement> methods) {
        return methods.stream().filter(ele -> ele.getSimpleName().contentEquals(WRITE_PACKET)).findAny()
                .orElseThrow(() -> new IllegalStateException("PacketAV: No writePacket method"));
    }

//    private ExecutableElement getReadPacket(Set<ExecutableElement> methods) {
//        return methods.stream().filter(ele -> ele.getSimpleName().contentEquals(READ_PACKET)).findAny()
//                .orElseThrow(() -> new IllegalStateException("PacketAV: No readPacket method"));
//    }

    @Override
    public TypeSpec makeTypeSpec(Context context, TypeSpec.Builder input) {
        TypeSpec tmpSpec = input.build();
        ClassName thisName = ClassName.get(context.packageName(), tmpSpec.name);
        // Generate methods to build to/from a set of Keys

        input.addType(createBuilderExtension(thisName.nestedClass("Builder"), context, ((ClassName) tmpSpec.superclass).nestedClass("Builder")));
        input.addMethod(createWritePacket(context));
        return input.build();
    }

    private TypeSpec createBuilderExtension(ClassName thisName, Context context, TypeName supertype) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(thisName).addModifiers(Modifier.STATIC).superclass(supertype);
        builder.addMethod(createReadPacket(thisName.enclosingClassName(), context));
        return builder.build();
    }

    private MethodSpec createReadPacket(TypeName container, Context context) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(READ_PACKET)
                .addModifiers(Modifier.PUBLIC).returns(container);

        // Input
        String inputName = "stream";
        method.addParameter(ClassName.get(DataInputStream.class), inputName);

        // Call builder methods with key data
        context.properties().forEach((name, element) -> {
            method.addStatement("$L($L.$L())",
                    name, inputName, getStreamIOName("read", TypeName.get(element.getReturnType()))
            );
        });

        // And return the built version.
        method.addStatement("return build()");
        return method.build();
    }

    private MethodSpec createWritePacket(Context context) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(WRITE_PACKET)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL).addAnnotation(Override.class);

        // Input
        String inputName = "stream";
        method.addParameter(ClassName.get(DataOutputStream.class), inputName);

        // Call builder methods with key data
        context.properties().forEach((name, element) -> {
            method.addStatement("$L.$L($L())",
                    inputName, getStreamIOName("write", TypeName.get(element.getReturnType())), element.getSimpleName().toString()
            );
        });

        return method.build();
    }

    private String getStreamIOName(String prefix, TypeName type) {
        if (type.equals(ClassName.get(String.class))) {
            return prefix + "UTF";
        }
        if (type.isPrimitive()) {
            return prefix + Util.uppercaseFirstLetter(type.toString());
        }
        throw new IllegalArgumentException("Can't serialize type " + type);
    }

}

