package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.*
import org.jetbrains.kotlinx.dataframe.api.Update.UpdateOperationArg
import org.jetbrains.kotlinx.dataframe.columns.ColumnKind
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.documentation.*
import kotlin.reflect.KProperty

// region fillNulls

/**
 * ## The Fill Nulls Operation
 *
 * Replaces `null` values with given value or expression.
 * Specific case of [update].
 *
 * Check out the [`fillNulls` Operation Usage][FillNulls.Usage].
 *
 * For more information: {@include [DocumentationUrls.Fill.FillNulls]}
 */
internal interface FillNulls {

    /** @include [Update.Usage] {@setArg [UpdateOperationArg] [fillNulls][fillNulls]} */
    interface Usage
}

/** {@setArg [SelectingColumns.OperationArg] [fillNulls][fillNulls]} */
private interface SetFillNullsOperationArg

/**
 * @include [FillNulls] {@comment Description of the fillNulls operation.}
 * @include [LineBreak]
 * @include [Update.Columns] {@comment Description of what this function expects the user to do: select columns}
 *
 * ## This Fill Nulls Overload
 *
 */
private interface CommonFillNullsFunctionDoc

/**
 * @include [CommonFillNullsFunctionDoc]
 * @include [SelectingColumns.Dsl.WithExample] {@include [SetFillNullsOperationArg]}
 * @include [Update.DslParam]
 */
public fun <T, C> DataFrame<T>.fillNulls(columns: ColumnsSelector<T, C?>): Update<T, C?> =
    update(columns).where { it == null }

/**
 * @include [CommonFillNullsFunctionDoc]
 * @include [SelectingColumns.ColumnNames.WithExample] {@include [SetFillNullsOperationArg]}
 * @include [Update.ColumnNamesParam]
 */
public fun <T> DataFrame<T>.fillNulls(vararg columns: String): Update<T, Any?> =
    fillNulls { columns.toColumnSet() }

/**
 * @include [CommonFillNullsFunctionDoc]
 * @include [SelectingColumns.KProperties.WithExample] {@include [SetFillNullsOperationArg]}
 * @include [Update.KPropertiesParam]
 */
public fun <T, C> DataFrame<T>.fillNulls(vararg columns: KProperty<C>): Update<T, C?> =
    fillNulls { columns.toColumnSet() }

/**
 * @include [CommonFillNullsFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors.WithExample] {@include [SetFillNullsOperationArg]}
 * @include [Update.ColumnAccessorsParam]
 */
public fun <T, C> DataFrame<T>.fillNulls(vararg columns: ColumnReference<C>): Update<T, C?> =
    fillNulls { columns.toColumnSet() }

// endregion

internal inline val Any?.isNaN: Boolean get() = (this is Double && isNaN()) || (this is Float && isNaN())

internal inline val Any?.isNA: Boolean
    get() = when (this) {
        null -> true
        is Double -> isNaN()
        is Float -> isNaN()
        is AnyRow -> allNA()
        is AnyFrame -> isEmpty()
        else -> false
    }

internal inline val AnyCol.canHaveNaN: Boolean get() = typeClass.let { it == Double::class || it == Float::class }

internal inline val AnyCol.canHaveNA: Boolean get() = hasNulls() || canHaveNaN || kind != ColumnKind.Value

internal inline val Double?.isNA: Boolean get() = this == null || this.isNaN()

internal inline val Float?.isNA: Boolean get() = this == null || this.isNaN()

// region fillNaNs

/**
 * ## The Fill NaNs Operation
 *
 * Replaces [`NaN`][NaN] values with given value or expression.
 * Specific case of [update].
 *
 * Check out the [`fillNaNs` Operation Usage][FillNaNs.Usage].
 *
 * For more information: {@include [DocumentationUrls.Fill.FillNaNs]}
 */
internal interface FillNaNs {

    /** @include [Update.Usage] {@setArg [Update.UpdateOperationArg] [fillNaNs][fillNaNs]} */
    interface Usage
}

/** {@setArg [SelectingColumns.OperationArg] [fillNaNs][fillNaNs]} */
internal interface SetFillNaNsOperationArg

/**
 * @include [FillNaNs] {@comment Description of the fillNaNs operation.}
 * @include [LineBreak]
 * @include [Update.Columns] {@comment Description of what this function expects the user to do: select columns}
 * ## This Fill NaNs Overload
 */
private interface CommonFillNaNsFunctionDoc

/**
 * @include [CommonFillNaNsFunctionDoc]
 * @include [SelectingColumns.Dsl.WithExample] {@include [SetFillNaNsOperationArg]}
 * @include [Update.DslParam]
 */
public fun <T, C> DataFrame<T>.fillNaNs(columns: ColumnsSelector<T, C>): Update<T, C> =
    update(columns).where { it.isNaN }

/**
 * @include [CommonFillNaNsFunctionDoc]
 * @include [SelectingColumns.ColumnNames.WithExample] {@include [SetFillNaNsOperationArg]}
 * @include [Update.ColumnNamesParam]
 */
public fun <T> DataFrame<T>.fillNaNs(vararg columns: String): Update<T, Any?> =
    fillNaNs { columns.toColumnSet() }

/**
 * @include [CommonFillNaNsFunctionDoc]
 * @include [SelectingColumns.KProperties.WithExample] {@include [SetFillNaNsOperationArg]}
 * @include [Update.KPropertiesParam]
 */
public fun <T, C> DataFrame<T>.fillNaNs(vararg columns: KProperty<C>): Update<T, C> =
    fillNaNs { columns.toColumnSet() }

/**
 * @include [CommonFillNaNsFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors.WithExample] {@include [SetFillNaNsOperationArg]}
 * @include [Update.ColumnAccessorsParam]
 */
public fun <T, C> DataFrame<T>.fillNaNs(vararg columns: ColumnReference<C>): Update<T, C> =
    fillNaNs { columns.toColumnSet() }

// endregion

// region fillNA

/**
 * ## The Fill NA Operation
 *
 * Replaces [`NA`][NA] values with given value or expression.
 * Specific case of [update].
 *
 * Check out the [`fillNA` Operation Usage][FillNA.Usage].
 *
 * For more information: {@include [DocumentationUrls.Fill.FillNA]}
 */
internal interface FillNA {

    /** @include [Update.Usage] {@setArg [Update.UpdateOperationArg] [fillNA][fillNA]} */
    interface Usage
}

/** {@setArg [SelectingColumns.OperationArg] [fillNA][fillNA]} */
internal interface SetFillNAOperationArg

/**
 * @include [FillNA] {@comment Description of the fillNA operation.}
 * @include [LineBreak]
 * @include [Update.Columns] {@comment Description of what this function expects the user to do: select columns}
 * ## This Fill NA Overload
 */
private interface CommonFillNAFunctionDoc

/**
 * @include [CommonFillNAFunctionDoc]
 * @include [SelectingColumns.Dsl.WithExample] {@include [SetFillNAOperationArg]}
 * @include [Update.DslParam]
 */
public fun <T, C> DataFrame<T>.fillNA(columns: ColumnsSelector<T, C?>): Update<T, C?> =
    update(columns).where { it.isNA }

/**
 * @include [CommonFillNAFunctionDoc]
 * @include [SelectingColumns.ColumnNames.WithExample] {@include [SetFillNAOperationArg]}
 * @include [Update.ColumnNamesParam]
 */
public fun <T> DataFrame<T>.fillNA(vararg columns: String): Update<T, Any?> =
    fillNA { columns.toColumnSet() }

/**
 * @include [CommonFillNAFunctionDoc]
 * @include [SelectingColumns.KProperties.WithExample] {@include [SetFillNAOperationArg]}
 * @include [Update.KPropertiesParam]
 */
public fun <T, C> DataFrame<T>.fillNA(vararg columns: KProperty<C>): Update<T, C?> =
    fillNA { columns.toColumnSet() }

/**
 * @include [CommonFillNAFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors.WithExample] {@include [SetFillNAOperationArg]}
 * @include [Update.ColumnAccessorsParam]
 */
public fun <T, C> DataFrame<T>.fillNA(vararg columns: ColumnReference<C>): Update<T, C?> =
    fillNA { columns.toColumnSet() }

// endregion

/** @param columns The {@include [SelectingColumns.DslLink]} used to select the columns of this [DataFrame] to drop rows in. */
private interface DropDslParam

/** @param columns The {@include [SelectingColumns.KPropertiesLink]} used to select the columns of this [DataFrame] to drop rows in. */
private interface DropKPropertiesParam

/** @param columns The {@include [SelectingColumns.ColumnNamesLink]} used to select the columns of this [DataFrame] to drop rows in. */
private interface DropColumnNamesParam

/** @param columns The {@include [SelectingColumns.ColumnAccessors]} used to select the columns of this [DataFrame] to drop rows in. */
private interface DropColumnAccessorsParam

// region dropNulls

/**
 * ## The Drop Nulls Operation
 *
 * Removes rows with `null` values. Specific case of [drop][DataFrame.drop].
 *
 * Optionally, you can select which columns to operate on (see {@include [SelectingColumnsLink]}).
 * Also, you can supply `whereAllNull = true` to only drop rows where all selected cells are `null`. By default,
 * rows are dropped if any of the selected cells are `null`.
 *
 * For more information: {@include [DocumentationUrls.Drop.DropNulls]}
 */
internal interface DropNulls {

    /**
     * @param whereAllNull `false` by default.
     *   If `true`, rows are dropped if all selected cells are `null`.
     *   If `false`, rows are dropped if any of the selected cells is `null`.
     */
    interface WhereAllNullParam
}

/** {@setArg [SelectingColumns.OperationArg] [dropNulls][dropNulls]} */
private interface SetDropNullsOperationArg

/**
 * @include [DropNulls] {@comment Description of the dropNulls operation.}
 * ## This Drop Nulls Overload
 */
private interface CommonDropNullsFunctionDoc

/**
 * @include [CommonDropNullsFunctionDoc]
 * @include [SelectingColumns.Dsl.WithExample] {@include [SetDropNullsOperationArg]}
 * `df.`[dropNulls][dropNulls]`(whereAllNull = true) { `[colsOf][colsOf]`<`[Double][Double]`>() }`
 * @include [DropNulls.WhereAllNullParam]
 * @include [DropDslParam]
 */
public fun <T> DataFrame<T>.dropNulls(whereAllNull: Boolean = false, columns: ColumnsSelector<T, *>): DataFrame<T> {
    val cols = this[columns]
    return if (whereAllNull) drop { row -> cols.all { col -> col[row] == null } }
    else drop { row -> cols.any { col -> col[row] == null } }
}

/**
 * @include [CommonDropNullsFunctionDoc]
 * This overload operates on all columns in the [DataFrame].
 * @include [DropNulls.WhereAllNullParam]
 */
public fun <T> DataFrame<T>.dropNulls(whereAllNull: Boolean = false): DataFrame<T> =
    dropNulls(whereAllNull) { all() }

/**
 * @include [CommonDropNullsFunctionDoc]
 * @include [SelectingColumns.KProperties.WithExample] {@include [SetDropNullsOperationArg]}
 * `df.`[dropNulls][dropNulls]`(Person::length, whereAllNull = true)`
 * @include [DropNulls.WhereAllNullParam]
 * @include [DropKPropertiesParam]
 */
public fun <T> DataFrame<T>.dropNulls(vararg columns: KProperty<*>, whereAllNull: Boolean = false): DataFrame<T> =
    dropNulls(whereAllNull) { columns.toColumnSet() }

/**
 * @include [CommonDropNullsFunctionDoc]
 * @include [SelectingColumns.ColumnNames.WithExample] {@include [SetDropNullsOperationArg]}
 * `df.`[dropNulls][dropNulls]`("length", whereAllNull = true)`
 * @include [DropNulls.WhereAllNullParam]
 * @include [DropColumnNamesParam]
 */
public fun <T> DataFrame<T>.dropNulls(vararg columns: String, whereAllNull: Boolean = false): DataFrame<T> =
    dropNulls(whereAllNull) { columns.toColumnSet() }

/**
 * @include [CommonDropNullsFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors.WithExample] {@include [SetDropNullsOperationArg]}
 * `df.`[dropNulls][dropNulls]`(length, whereAllNull = true)`
 * @include [DropNulls.WhereAllNullParam]
 * @include [DropColumnAccessorsParam]
 */
public fun <T> DataFrame<T>.dropNulls(vararg columns: AnyColumnReference, whereAllNull: Boolean = false): DataFrame<T> =
    dropNulls(whereAllNull) { columns.toColumnSet() }

/**
 * ## The Drop Nulls Operation
 *
 * Removes `null` values from this [DataColumn], adjusting the type accordingly.
 */
public fun <T> DataColumn<T?>.dropNulls(): DataColumn<T> =
    (if (!hasNulls()) this else filter { it != null }) as DataColumn<T>

// endregion

// region dropNA

/**
 * ## The Drop `NA` Operation
 *
 * Removes rows with [`NA`][NA] values. Specific case of [drop][DataFrame.drop].
 *
 * Optionally, you can select which columns to operate on (see {@include [SelectingColumnsLink]}).
 * Also, you can supply `whereAllNA = true` to only drop rows where all selected cells are [`NA`][NA]. By default,
 * rows are dropped if any of the selected cells are [`NA`][NA].
 *
 * For more information: {@include [DocumentationUrls.Drop.DropNA]}
 */
internal interface DropNA {

    /**
     * @param whereAllNA `false` by default.
     *   If `true`, rows are dropped if all selected cells are [`NA`][NA].
     *   If `false`, rows are dropped if any of the selected cells is [`NA`][NA].
     */
    interface WhereAllNAParam
}

/** {@setArg [SelectingColumns.OperationArg] [dropNA][dropNA]} */
private interface SetDropNAOperationArg

/**
 * @include [DropNA] {@comment Description of the dropNA operation.}
 * ## This Drop NA Overload
 */
private interface CommonDropNAFunctionDoc

/**
 * @include [CommonDropNAFunctionDoc]
 * @include [SelectingColumns.Dsl.WithExample] {@include [SetDropNAOperationArg]}
 * `df.`[dropNA][dropNA]`(whereAllNA = true) { `[colsOf][colsOf]`<`[Double][Double]`>() }`
 * @include [DropNA.WhereAllNAParam]
 * @include [DropDslParam]
 */
public fun <T> DataFrame<T>.dropNA(whereAllNA: Boolean = false, columns: ColumnsSelector<T, *>): DataFrame<T> {
    val cols = this[columns]
    return if (whereAllNA) drop { cols.all { this[it].isNA } }
    else drop { cols.any { this[it].isNA } }
}

/**
 * @include [CommonDropNAFunctionDoc]
 * @include [SelectingColumns.KProperties.WithExample] {@include [SetDropNAOperationArg]}
 * `df.`[dropNA][dropNA]`(Person::length, whereAllNA = true)`
 * @include [DropNA.WhereAllNAParam]
 * @include [DropKPropertiesParam]
 */
public fun <T> DataFrame<T>.dropNA(vararg columns: KProperty<*>, whereAllNA: Boolean = false): DataFrame<T> =
    dropNA(whereAllNA) { columns.toColumnSet() }

/**
 * @include [CommonDropNAFunctionDoc]
 * @include [SelectingColumns.ColumnNames.WithExample] {@include [SetDropNAOperationArg]}
 * `df.`[dropNA][dropNA]`("length", whereAllNA = true)`
 * @include [DropNA.WhereAllNAParam]
 * @include [DropColumnNamesParam]
 */
public fun <T> DataFrame<T>.dropNA(vararg columns: String, whereAllNA: Boolean = false): DataFrame<T> =
    dropNA(whereAllNA) { columns.toColumnSet() }

/**
 * @include [CommonDropNAFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors.WithExample] {@include [SetDropNAOperationArg]}
 * `df.`[dropNA][dropNA]`(length, whereAllNA = true)`
 * @include [DropNA.WhereAllNAParam]
 * @include [DropColumnAccessorsParam]
 */
public fun <T> DataFrame<T>.dropNA(vararg columns: AnyColumnReference, whereAllNA: Boolean = false): DataFrame<T> =
    dropNA(whereAllNA) { columns.toColumnSet() }

/**
 * @include [CommonDropNAFunctionDoc]
 * This overload operates on all columns in the [DataFrame].
 * @include [DropNA.WhereAllNAParam]
 */
public fun <T> DataFrame<T>.dropNA(whereAllNA: Boolean = false): DataFrame<T> =
    dropNA(whereAllNA) { all() }

/**
 * ## The Drop `NA` Operation
 *
 * Removes [`NA`][NA] values from this [DataColumn], adjusting the type accordingly.
 */
public fun <T> DataColumn<T?>.dropNA(): DataColumn<T> =
    when (typeClass) {
        Double::class, Float::class -> filter { !it.isNA }.cast()
        else -> (if (!hasNulls()) this else filter { it != null }) as DataColumn<T>
    }

// endregion

// region dropNaNs

/**
 * ## The Drop `NaN` Operation
 *
 * Removes rows with [`NaN`][Double.isNaN] values. Specific case of [drop][DataFrame.drop].
 *
 * Optionally, you can select which columns to operate on (see {@include [SelectingColumnsLink]}).
 * Also, you can supply `whereAllNaN = true` to only drop rows where all selected cells are [`NaN`][Double.isNaN]. By default,
 * rows are dropped if any of the selected cells are [`NaN`][Double.isNaN].
 *
 * For more information: {@include [DocumentationUrls.Drop.DropNaNs]}
 */
internal interface DropNaNs {

    /**
     * @param whereAllNaN `false` by default.
     *   If `true`, rows are dropped if all selected cells are [`NaN`][Double.isNaN].
     *   If `false`, rows are dropped if any of the selected cells is [`NaN`][Double.isNaN].
     */
    interface WhereAllNaNParam
}

/** {@setArg [SelectingColumns.OperationArg] [dropNaNs][dropNaNs]} */
private interface SetDropNaNsOperationArg

/**
 * @include [DropNaNs] {@comment Description of the dropNaNs operation.}
 * ## This Drop NaNs Overload
 */
private interface CommonDropNaNsFunctionDoc

/**
 * @include [CommonDropNaNsFunctionDoc]
 * @include [SelectingColumns.Dsl.WithExample] {@include [SetDropNaNsOperationArg]}
 * `df.`[dropNaNs][dropNaNs]`(whereAllNaN = true) { `[colsOf][colsOf]`<`[Double][Double]`>() }`
 * @include [DropNaNs.WhereAllNaNParam]
 * @include [DropDslParam]
 */
public fun <T> DataFrame<T>.dropNaNs(whereAllNaN: Boolean = false, columns: ColumnsSelector<T, *>): DataFrame<T> {
    val cols = this[columns]
    return if (whereAllNaN) drop { cols.all { this[it].isNaN } }
    else drop { cols.any { this[it].isNaN } }
}

/**
 * @include [CommonDropNaNsFunctionDoc]
 * @include [SelectingColumns.KProperties.WithExample] {@include [SetDropNaNsOperationArg]}
 * `df.`[dropNaNs][dropNaNs]`(Person::length, whereAllNaN = true)`
 * @include [DropNaNs.WhereAllNaNParam]
 * @include [DropKPropertiesParam]
 */
public fun <T> DataFrame<T>.dropNaNs(vararg columns: KProperty<*>, whereAllNaN: Boolean = false): DataFrame<T> =
    dropNaNs(whereAllNaN) { columns.toColumnSet() }

/**
 * @include [CommonDropNaNsFunctionDoc]
 * @include [SelectingColumns.ColumnNames.WithExample] {@include [SetDropNaNsOperationArg]}
 * `df.`[dropNaNs][dropNaNs]`("length", whereAllNaN = true)`
 * @include [DropNaNs.WhereAllNaNParam]
 * @include [DropColumnNamesParam]
 */
public fun <T> DataFrame<T>.dropNaNs(vararg columns: String, whereAllNaN: Boolean = false): DataFrame<T> =
    dropNaNs(whereAllNaN) { columns.toColumnSet() }

/**
 * @include [CommonDropNaNsFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors.WithExample] {@include [SetDropNaNsOperationArg]}
 * `df.`[dropNaNs][dropNaNs]`(length, whereAllNaN = true)`
 * @include [DropNaNs.WhereAllNaNParam]
 * @include [DropColumnAccessorsParam]
 */
public fun <T> DataFrame<T>.dropNaNs(vararg columns: AnyColumnReference, whereAllNaN: Boolean = false): DataFrame<T> =
    dropNaNs(whereAllNaN) { columns.toColumnSet() }

/**
 * @include [CommonDropNaNsFunctionDoc]
 * This overload operates on all columns in the [DataFrame].
 * @include [DropNaNs.WhereAllNaNParam]
 */
public fun <T> DataFrame<T>.dropNaNs(whereAllNaN: Boolean = false): DataFrame<T> =
    dropNaNs(whereAllNaN) { all() }

/**
 * ## The Drop `NaN` Operation
 *
 * Removes [`NaN`][NaN] values from this [DataColumn], adjusting the type accordingly.
 */
public fun <T> DataColumn<T>.dropNaNs(): DataColumn<T> =
    when (typeClass) {
        Double::class, Float::class -> filter { !it.isNaN }.cast()
        else -> this
    }

// endregion
