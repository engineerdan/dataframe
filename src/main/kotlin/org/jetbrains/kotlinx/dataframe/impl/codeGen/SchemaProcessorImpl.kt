package org.jetbrains.kotlinx.dataframe.impl.codeGen

import org.jetbrains.kotlinx.dataframe.internal.codeGen.GeneratedField
import org.jetbrains.kotlinx.dataframe.internal.codeGen.Marker
import org.jetbrains.kotlinx.dataframe.internal.codeGen.MarkerVisibility
import org.jetbrains.kotlinx.dataframe.internal.codeGen.SchemaProcessor
import org.jetbrains.kotlinx.dataframe.internal.codeGen.ValidFieldName
import org.jetbrains.kotlinx.dataframe.internal.schema.ColumnSchema
import org.jetbrains.kotlinx.dataframe.internal.schema.DataFrameSchema

internal class SchemaProcessorImpl(
    existingMarkers: Iterable<Marker>,
    override val namePrefix: String
) : SchemaProcessor {

    private val registeredMarkers = existingMarkers.toMutableList()

    private val usedMarkerNames = existingMarkers.map { it.shortName }.toMutableSet()

    override val generatedMarkers = mutableListOf<Marker>()

    private fun DataFrameSchema.getAllSuperMarkers() = registeredMarkers
        .filter { it.schema.compare(this).isSuperOrEqual() }

    private fun List<Marker>.onlyLeafs(): List<Marker> {
        val skip = flatMap { it.allBaseMarkers.keys }.toSet()
        return filter { !skip.contains(it.name) }
    }

    private fun generateValidFieldName(columnName: String, index: Int, usedNames: Collection<String>): ValidFieldName {
        var result = ValidFieldName.of(columnName)
        if (result.unquoted.isEmpty()) result = ValidFieldName.of("_$index")
        val baseName = result
        var attempt = 2
        while (usedNames.contains(result.quotedIfNeeded)) {
            result = if (result.needsQuote) baseName + ValidFieldName.of(" ($attempt)") else baseName + ValidFieldName.of("_$attempt")
            attempt++
        }
        return result
    }

    private fun generateUniqueMarkerClassName(prefix: String): String {
        if (!usedMarkerNames.contains(prefix)) return prefix
        var id = 1
        while (usedMarkerNames.contains("$prefix$id"))
            id++
        return "$prefix$id"
    }

    private fun generateFields(
        schema: DataFrameSchema,
        visibility: MarkerVisibility,
        requiredSuperMarkers: List<Marker> = emptyList()
    ): List<GeneratedField> {
        val usedFieldNames = requiredSuperMarkers.flatMap { it.allFields.map { it.fieldName.quotedIfNeeded } }.toMutableSet()

        fun getMarker(column: ColumnSchema) = when (column) {
            is ColumnSchema.Value -> null
            is ColumnSchema.Map -> process(column.schema, false, visibility)
            is ColumnSchema.Frame -> process(column.schema, false, visibility)
            else -> throw NotImplementedError()
        }

        return schema.sortedColumns.flatMapIndexed { index, column ->
            val (columnName, columnSchema) = column

            // find all fields that were already generated for this column name in base interfaces
            val superFields = requiredSuperMarkers.mapNotNull { it.allFieldsByColumn[columnName] }

            val fieldsToOverride = superFields.filter { it.columnSchema != columnSchema }

            val newFields = when {
                fieldsToOverride.isNotEmpty() -> fieldsToOverride.map {
                    GeneratedField(it.fieldName, it.columnName, true, columnSchema, getMarker(columnSchema)?.name)
                }
                superFields.isNotEmpty() -> emptyList()
                else -> {
                    val fieldName = generateValidFieldName(columnName, index, usedFieldNames)
                    usedFieldNames.add(fieldName.quotedIfNeeded)
                    listOf(GeneratedField(fieldName, columnName, false, columnSchema, getMarker(columnSchema)?.name))
                }
            }
            newFields
        }
    }

    private fun createMarkerSchema(
        scheme: DataFrameSchema,
        name: String,
        withBaseInterfaces: Boolean,
        isOpen: Boolean,
        visibility: MarkerVisibility
    ): Marker {
        val baseMarkers = mutableListOf<Marker>()
        val fields = if (withBaseInterfaces) {
            baseMarkers += scheme.getRequiredMarkers().onlyLeafs()

            val columnNames = scheme.columns.keys
            val superColumns = baseMarkers.flatMap { it.allFields.map { it.columnName } }.toSet()

            val newColumns = (columnNames - superColumns).toMutableSet()

            if (newColumns.isNotEmpty()) {
                val availableMarkers = scheme.getAllSuperMarkers().toMutableList()
                availableMarkers -= baseMarkers

                while (newColumns.size > 0) {
                    val bestMarker =
                        availableMarkers.map { marker -> marker to newColumns.count { marker.containsColumn(it) } }
                            .maxByOrNull { it.second }
                    if (bestMarker != null && bestMarker.second > 0) {
                        newColumns.removeAll(bestMarker.first.columnNames)
                        baseMarkers += bestMarker.first
                        availableMarkers -= bestMarker.first
                    } else break
                }
            }
            generateFields(scheme, visibility, baseMarkers)
        } else generateFields(scheme, visibility)
        return Marker(name, isOpen, fields, baseMarkers.onlyLeafs(), visibility)
    }

    private fun DataFrameSchema.getRequiredMarkers() = getRequiredMarkers(this, registeredMarkers)

    override fun process(
        schema: DataFrameSchema,
        isOpen: Boolean,
        visibility: MarkerVisibility
    ): Marker {
        val markerName: String
        val required = schema.getRequiredMarkers()
        val existingMarker = registeredMarkers.firstOrNull {
            (!isOpen || it.isOpen) && it.schema == schema && it.implementsAll(required)
        }
        if (existingMarker != null) {
            return existingMarker
        } else {
            markerName = generateUniqueMarkerClassName(namePrefix)
            usedMarkerNames.add(markerName)
            val marker = createMarkerSchema(schema, markerName, true, isOpen, visibility)
            registeredMarkers.add(marker)
            generatedMarkers.add(marker)
            return marker
        }
    }
}