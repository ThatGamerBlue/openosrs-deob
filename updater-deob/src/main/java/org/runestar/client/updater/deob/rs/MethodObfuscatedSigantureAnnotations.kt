package org.runestar.client.updater.deob.rs;

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.kxtra.slf4j.getLogger
import org.runestar.client.updater.deob.Transformer
import org.runestar.client.updater.deob.util.*
import java.nio.file.Path
import java.util.logging.Logger

object MethodObfuscatedSigantureAnnotations : Transformer {
	private val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

	override fun transform(source: Path, destination: Path){
		val classNodes = readJar(source)

		var opFile = destination.resolveSibling(destination.fileName.toString() + ".op-descs.json").toFile()

		var annoDecoders: Map<String, String> = mapper.readValue(opFile)

		var descriptionInjections = 0;
		var descriptionMissedInjections = 0
		for (mult in annoDecoders.keys) {
			val className = mult.split(".")[0]
			val methodNameAndDesc = mult.split(".")[1]
			val clasz = classNodes.find { classNode -> classNode.name == className }
			if (clasz != null) {
				val method = (clasz.methods.find {
					method -> method.name + method.desc == methodNameAndDesc
				})
				if (method !=null) {

					if (method.visibleAnnotations!=null) {
						val annotation = method.visibleAnnotations.find { annotation -> annotation.desc == "Lnet/runelite/mapping/ObfuscatedSignature;"}
						if (annotation != null) {
							val garbageVal = annotation.values.get(1)
							method.visibleAnnotations.remove(annotation)
							method.visitAnnotation("Lnet/runelite/mapping/ObfuscatedSignature;", true).visit("signature", annoDecoders[mult])
							val newAnnotation = method.visibleAnnotations.find { newAnnotation -> annotation.desc == "Lnet/runelite/mapping/ObfuscatedSignature;"}
							newAnnotation!!.visit("garbageValue", garbageVal)
							descriptionInjections++
						} else {
							println("Didnt get ObfuscatedSignature annotation (should already exist)")
							descriptionMissedInjections++
						}
					} else {
						method.visitAnnotation("Lnet/runelite/mapping/ObfuscatedSignature;", true).visit("signature", annoDecoders[mult])
						descriptionInjections++
					}
				} else {
					println("Didnt get method ${clasz.name}.$methodNameAndDesc")
					descriptionMissedInjections++
				}
			} else {
				System.out.println("Didnt get Class ")
				descriptionMissedInjections++
			}
		}
		Logger.getAnonymousLogger().info("Added " + descriptionInjections + " ObfuscatedSignature Annotations, missed " + descriptionMissedInjections)

		writeJar(classNodes, destination)
	}
}
