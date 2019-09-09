package org.janelia.saalfeldlab.conversion

import org.apache.spark.api.java.JavaSparkContext
import org.janelia.saalfeldlab.label.spark.N5Helpers

abstract class DatasetConverter(val info: DatasetInfo) {

    fun convert(
            sc: JavaSparkContext,
            parameters: DatasetSpecificParameters,
            overwriteExisiting: Boolean) {
        convertSpecific(sc, parameters, overwriteExisiting)


        val downsamplingFactor = DoubleArray(parameters.blockSize.size) { 1.0 }
        val writer = info.outputContainer.n5Writer(DEFAULT_BUILDER)
        writer.setAttribute(scaleGroup(info.outputGroup, 0), DOWNSAMPLING_FACTORS, downsamplingFactor)

        for ((scaleNum, scale) in parameters.scales.withIndex()) {
            for (i in downsamplingFactor.indices)
                downsamplingFactor[i] *= scale[i].toDouble()
            writer.setAttribute(scaleGroup(info.outputGroup, scaleNum + 1), DOWNSAMPLING_FACTORS, downsamplingFactor)

        }

        val res = parameters.resolution
                ?: N5Helpers.n5Reader(info.inputContainer).getDoubleArrayAttribute(info.inputDataset, RESOLUTION_KEY)
                ?: DoubleArray(3) { 1.0 }
        writer.setAttribute("${info.outputGroup}/data", RESOLUTION_KEY, res)

        val off = parameters.offset
                ?: N5Helpers.n5Reader(info.inputContainer).getDoubleArrayAttribute(info.inputDataset, OFFSET_KEY)
                ?: DoubleArray(3) { 0.0 }
        writer.setAttribute("${info.outputGroup}/data", OFFSET_KEY, off)
        writer.setPainteraDataType(info.outputGroup, type)
    }

    protected abstract fun convertSpecific(
            sc: JavaSparkContext,
            parameters: DatasetSpecificParameters,
            overwriteExisiting: Boolean)

    protected open val legalDimensions: Set<Int>
        get() = setOf(3)

    protected abstract val type: String

    companion object {
        operator fun get(info: DatasetInfo, type: String) = when(type.toLowerCase()) {
            "raw" -> DatasetConverterRaw(info)
            "channel" -> DatasetConverterChannel(info)
            "label" -> DatasetConverterLabel(info)
            else -> null
        }
    }

}