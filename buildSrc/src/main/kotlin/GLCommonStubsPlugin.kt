package buildlogic

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.apache.commons.compress.archivers.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import java.io.File
import javax.lang.model.element.Modifier

open class LwjglStubExtension(objects: ObjectFactory) {
    val lwjgl2Dep = objects.property<String>()
    val lwjgl3Dep = objects.property<String>()
    val stubSourceSet: Property<SourceSet> = objects.property<SourceSet>()
}

class GLCommonStubsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<LwjglStubExtension>("lwjglStub", project.objects)
        val sourceSets = project.extensions.getByType<JavaPluginExtension>().sourceSets
        ext.stubSourceSet.convention(sourceSets.maybeCreate("lwjglStub"))

        val outputDir = project.layout.buildDirectory.dir("generated/sources/lwjgl-stub-sources")

        val generate = project.tasks.register<GenerateStubSourcesTask>("generateLwjglStubSources") {
            this.outputDir.set(outputDir)
            lwjgl2Dep.set(ext.lwjgl2Dep)
            lwjgl3Dep.set(ext.lwjgl3Dep)
        }

        ext.stubSourceSet.get().java.srcDir(generate.flatMap { it.outputDir })
        project.tasks.named("compileLwjglStubJava") {
            dependsOn(generate)
        }
    }
}

abstract class GenerateStubSourcesTask : DefaultTask() {
    @Input
    val lwjgl2Dep: Property<String> = project.objects.property(String::class.java)

    @Input
    val lwjgl3Dep: Property<String> = project.objects.property(String::class.java)

    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun generate() {
        val config2 = project.configurations.detachedConfiguration(
            project.dependencies.create(lwjgl2Dep.get())
        ).apply { isTransitive = false }
        val config3 = project.configurations.detachedConfiguration(
            project.dependencies.create(lwjgl3Dep.get())
        ).apply { isTransitive = false }

        val lwjgl2Jar = config2.resolve().single()
        val lwjgl3Jar = config3.resolve().single()

        val lwjgl2Map = extractStubFromJar(lwjgl2Jar)
        val lwjgl3Map = extractStubFromJar(lwjgl3Jar)

        val javaRoot = outputDir.get().asFile
        javaRoot.mkdirs()

        for ((cls, stub2) in lwjgl2Map) {
            val stub3 = lwjgl3Map[cls] ?: continue
            val commonKeys = stub2.methods.keys.intersect(stub3.methods.keys)
            if (commonKeys.isEmpty()) continue

            val typeBuilder = TypeSpec.classBuilder(cls)
                .addModifiers(Modifier.PUBLIC)

            val sharedConstants = stub2.constants.associate { it.name to it.value }
                .filterKeys { name -> stub3.constants.any { it.name == name } }

            for ((name, value) in sharedConstants) {
                val hex = "0x" + value.toUInt().toString(16).uppercase()
                val field = FieldSpec.builder(TypeName.INT, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(hex)
                    .build()
                typeBuilder.addField(field)
            }

            for ((name, desc) in commonKeys) {
                val params = stub2.methods[name to desc]!!
                val method = MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.NATIVE)
                    .returns(Type.getReturnType(desc).asTypeName())
                    .addParameters(Type.getArgumentTypes(desc).mapIndexed { i, type ->
                        ParameterSpec.builder(type.asTypeName(), params[i]).build()
                    })
                    .build()
                typeBuilder.addMethod(method)
            }

            JavaFile.builder("org.lwjgl.opengl", typeBuilder.build())
                .build()
                .writeTo(javaRoot)
        }

        println("Generated stub source in: ${javaRoot.absolutePath}")
    }

    data class ClassStub(
        val methods: Map<Pair<String, String>, List<String>>,
        val constants: List<ConstantField>
    )

    data class ConstantField(val name: String, val value: Int)

    private fun extractStubFromJar(jar: File): Map<String, ClassStub> {
        val zip = ZipFile.builder()
            .setFile(jar)
            .setUseUnicodeExtraFields(true)
            .get()

        val result = mutableMapOf<String, ClassStub>()

        val prefix = "org/lwjgl/opengl/GL"
        val postfix = ".class"
        zip.entries.asSequence()
            .filter {
                if (!it.name.startsWith(prefix)) return@filter false
                if (!it.name.endsWith(postfix)) return@filter false
                val name = it.name.substring(prefix.length, it.name.length - postfix.length)
                name.all { c -> c.isDigit() }
            }
            .forEach { entry ->
                val classNode = ClassNode()
                zip.getInputStream(entry).use {
                    ClassReader(it).accept(classNode, 0)
                }

                val className = classNode.name.substringAfterLast('/')
                val methods = mutableMapOf<Pair<String, String>, List<String>>()
                val constants = mutableListOf<ConstantField>()

                for (method in classNode.methods) {
                    if ((method.access and Opcodes.ACC_PUBLIC != 0) && (method.access and Opcodes.ACC_STATIC != 0)) {
                        val paramTypes = Type.getArgumentTypes(method.desc)
                        val paramCount = paramTypes.size
                        val argNames: List<String> = List(paramCount) { i ->
                            val localVars = method.localVariables
                            if (localVars != null && i < localVars.size && localVars[i] != null && localVars[i].name.isNotBlank()) {
                                localVars[i].name
                            } else {
                                "arg$i"
                            }
                        }
                        methods[method.name to method.desc] = argNames
                    }
                }

                for (field in classNode.fields) {
                    if ((field.access and Opcodes.ACC_PUBLIC != 0) &&
                        (field.access and Opcodes.ACC_STATIC != 0) &&
                        (field.access and Opcodes.ACC_FINAL != 0) &&
                        field.desc == "I" &&
                        field.value is Int
                    ) {
                        constants.add(ConstantField(field.name, field.value as Int))
                    }
                }

                result[className] = ClassStub(methods, constants)
            }

        return result
    }

    private fun Type.asTypeName(): TypeName = when (sort) {
        Type.VOID -> TypeName.VOID
        Type.BOOLEAN -> TypeName.BOOLEAN
        Type.CHAR -> TypeName.CHAR
        Type.BYTE -> TypeName.BYTE
        Type.SHORT -> TypeName.SHORT
        Type.INT -> TypeName.INT
        Type.FLOAT -> TypeName.FLOAT
        Type.LONG -> TypeName.LONG
        Type.DOUBLE -> TypeName.DOUBLE
        Type.ARRAY -> ArrayTypeName.of(this.elementType.asTypeName())
        Type.OBJECT -> ClassName.bestGuess(this.className.replace('/', '.'))
        else -> throw IllegalArgumentException("Unknown type: $this")
    }
}