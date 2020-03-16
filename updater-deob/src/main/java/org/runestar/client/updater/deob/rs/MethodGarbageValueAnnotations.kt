package org.runestar.client.updater.deob.rs

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.kxtra.slf4j.getLogger
import org.runestar.client.updater.deob.Transformer
import org.runestar.client.updater.deob.util.*
import java.nio.file.Path
import java.util.logging.Logger

object MethodGarbageValueAnnotations : Transformer {

    private val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    private val logger = getLogger()

    override fun transform(source: Path, destination: Path) {
        val classNodes = readJar(source)
        var opFile = destination.resolveSibling(destination.fileName.toString() + ".op.json").toFile()

        var annoDecoders: Map<String, String> = mapper.readValue(opFile)

        var garbageValueInjections = 0;
        var garbageValueMissedInjections = 0
        for (mult in annoDecoders.keys) {
            val className = mult.split(".")[0]
            val methodNameAndDesc = mult.split(".")[1]
            val methodName = methodNameAndDesc.substring(0, methodNameAndDesc.indexOf("("))
            val methodDesc = methodNameAndDesc.substring(methodNameAndDesc.indexOf("("))
            val clasz = classNodes.find { classNode -> classNode.name == className }
            if (clasz != null) {
                val method = (clasz.methods.find {
                    method -> method.name + method.desc == methodNameAndDesc
                })
                if (method != null) {
                    method.visitAnnotation("Lnet/runelite/mapping/ObfuscatedSignature;", true).visit("garbageValue", annoDecoders[mult])
                    garbageValueInjections++
                } else {
                    System.out.println("Didnt get Method " + className + "." + methodNameAndDesc)
                    garbageValueMissedInjections++
                }
            } else {
                System.out.println("Didnt get Class ")
                garbageValueMissedInjections++
            }
        }
        Logger.getAnonymousLogger().info("Added " + garbageValueInjections + " ObfuscatedSignature Annotations, missed " + garbageValueMissedInjections)

        writeJar(classNodes, destination)
    }
}