package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataFrameExpression
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.Selector
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.FrameColumn
import org.jetbrains.kotlinx.dataframe.columns.UnresolvedColumnsPolicy
import org.jetbrains.kotlinx.dataframe.columns.ValueColumn
import org.jetbrains.kotlinx.dataframe.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.impl.api.SortFlag
import org.jetbrains.kotlinx.dataframe.impl.api.addFlag
import org.jetbrains.kotlinx.dataframe.impl.api.sortByImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.newColumnWithActualType
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.index
import org.jetbrains.kotlinx.dataframe.nrow
import org.jetbrains.kotlinx.dataframe.type
import kotlin.reflect.KProperty

public interface SortDsl<out T> : ColumnsSelectionDsl<T> {
    public fun <C> ColumnSet<C>.desc(): ColumnSet<C> = addFlag(SortFlag.Reversed)

    public fun String.desc(): ColumnSet<Comparable<*>?> = invoke<Comparable<*>>().desc()

    public fun <C> KProperty<C>.desc(): ColumnSet<C> = toColumnAccessor().desc()

    public fun <C> ColumnSet<C?>.nullsLast(flag: Boolean = true): ColumnSet<C?> =
        if (flag) addFlag(SortFlag.NullsLast) else this

    public fun String.nullsLast(flag: Boolean = true): ColumnSet<Comparable<*>?> =
        invoke<Comparable<*>>().nullsLast(flag)

    public fun <C> KProperty<C?>.nullsLast(flag: Boolean = true): ColumnSet<C?> = toColumnAccessor().nullsLast(flag)
}

/**
 * [SortColumnsSelector] is used to express or select multiple columns to sort by, represented by [ColumnSet]`<C>`,
 * using the context of [SortDsl]`<T>` as `this` and `it`.
 *
 * So:
 * ```kotlin
 * SortDsl<T>.(it: SortDsl<T>) -> ColumnSet<C>
 * ```
 */
public typealias SortColumnsSelector<T, C> = Selector<SortDsl<T>, ColumnSet<C>>

// region DataColumn

public fun <T : Comparable<T>> DataColumn<T>.sort(): ValueColumn<T> =
    DataColumn.createValueColumn(name, values().sorted(), type, defaultValue = defaultValue())

public fun <T : Comparable<T>> DataColumn<T>.sortDesc(): ValueColumn<T> =
    DataColumn.createValueColumn(name, values().sortedDescending(), type, defaultValue = defaultValue())

/**
 * ## Sort [DataColumn] With
 *
 * This function returns the sorted version of the current [ValueColumn], [FrameColumn], or [ColumnGroup] based
 * on the given [Comparator]. The [comparator\] can either be given as an instance of [Comparator], or directly
 * as a lambda.
 *
 * #### For example
 *
 * `df`[`[`][DataFrame.get]`"price"`[`]`][DataFrame.get]`.`[sortWith][sortWith]` { a, b -> a - b }`
 *
 * {@include [LineBreak]}
 * `df.`[select][DataFrame.select]` {`
 *
 * {@include [Indent]}`name.`[sortWith][sortWith]`(myComparator) `[and][ColumnsSelectionDsl.and]` `[allAfter][ColumnsSelectionDsl.allAfter]`(name)`
 *
 * `}`
 *
 * @receiver The [DataColumn] to sort. This can be either a [ValueColumn], [FrameColumn], or [ColumnGroup] and will
 *   dictate the return type of the function.
 * @param [comparator\] The [Comparator] to use for sorting the [DataColumn]. This can either be a [Comparator]<[T\]> or
 *   a lambda of type `(`[T][T\]`, `[T][T\]`) -> `[Int][Int].
 * @return The sorted [DataColumn] [this\] of the same type as the receiver.
 */
private interface CommonDataColumnSortWithDocs

/** @include [CommonDataColumnSortWithDocs] */
public fun <T, C : DataColumn<T>> C.sortWith(comparator: Comparator<T>): C =
    DataColumn.create(name, values().sortedWith(comparator), type) as C

/** @include [CommonDataColumnSortWithDocs] */
public fun <T, C : DataColumn<T>> C.sortWith(comparator: (T, T) -> Int): C =
    sortWith(Comparator(comparator))

// endregion

// region DataFrame

public fun <T, C> DataFrame<T>.sortBy(columns: SortColumnsSelector<T, C>): DataFrame<T> = sortByImpl(
    UnresolvedColumnsPolicy.Fail, columns
)

public fun <T> DataFrame<T>.sortBy(vararg cols: ColumnReference<Comparable<*>?>): DataFrame<T> =
    sortBy { cols.toColumnSet() }

public fun <T> DataFrame<T>.sortBy(vararg cols: String): DataFrame<T> = sortBy { cols.toColumnSet() }

public fun <T> DataFrame<T>.sortBy(vararg cols: KProperty<Comparable<*>?>): DataFrame<T> = sortBy { cols.toColumnSet() }

public fun <T> DataFrame<T>.sortWith(comparator: Comparator<DataRow<T>>): DataFrame<T> {
    val permutation = rows().sortedWith(comparator).map { it.index }
    return this[permutation]
}

public fun <T> DataFrame<T>.sortWith(comparator: (DataRow<T>, DataRow<T>) -> Int): DataFrame<T> =
    sortWith(Comparator(comparator))

public fun <T, C> DataFrame<T>.sortByDesc(columns: SortColumnsSelector<T, C>): DataFrame<T> {
    val set = columns.toColumnSet()
    return sortByImpl { set.desc() }
}

public fun <T, C> DataFrame<T>.sortByDesc(vararg columns: KProperty<Comparable<C>?>): DataFrame<T> =
    sortByDesc { columns.toColumnSet() }

public fun <T> DataFrame<T>.sortByDesc(vararg columns: String): DataFrame<T> = sortByDesc { columns.toColumnSet() }

public fun <T, C> DataFrame<T>.sortByDesc(vararg columns: ColumnReference<Comparable<C>?>): DataFrame<T> =
    sortByDesc { columns.toColumnSet() }

// endregion

// region GroupBy

public fun <T, G> GroupBy<T, G>.sortBy(vararg cols: String): GroupBy<T, G> = sortBy { cols.toColumnSet() }

public fun <T, G> GroupBy<T, G>.sortBy(vararg cols: ColumnReference<Comparable<*>?>): GroupBy<T, G> =
    sortBy { cols.toColumnSet() }

public fun <T, G> GroupBy<T, G>.sortBy(vararg cols: KProperty<Comparable<*>?>): GroupBy<T, G> =
    sortBy { cols.toColumnSet() }

public fun <T, G, C> GroupBy<T, G>.sortBy(selector: SortColumnsSelector<G, C>): GroupBy<T, G> = sortByImpl(selector)

public fun <T, G> GroupBy<T, G>.sortByDesc(vararg cols: String): GroupBy<T, G> = sortByDesc { cols.toColumnSet() }

public fun <T, G> GroupBy<T, G>.sortByDesc(vararg cols: ColumnReference<Comparable<*>?>): GroupBy<T, G> =
    sortByDesc { cols.toColumnSet() }

public fun <T, G> GroupBy<T, G>.sortByDesc(vararg cols: KProperty<Comparable<*>?>): GroupBy<T, G> =
    sortByDesc { cols.toColumnSet() }

public fun <T, G, C> GroupBy<T, G>.sortByDesc(selector: SortColumnsSelector<G, C>): GroupBy<T, G> {
    val set = selector.toColumnSet()
    return sortByImpl { set.desc() }
}

private fun <T, G, C> GroupBy<T, G>.createColumnFromGroupExpression(
    receiver: ColumnsSelectionDsl<T>,
    expression: DataFrameExpression<G, C>,
): DataColumn<C?> {
    return receiver.newColumnWithActualType("") { row ->
        val group = row[groups]
        expression(group, group)
    }
}

public fun <T, G, C> GroupBy<T, G>.sortByGroup(
    nullsLast: Boolean = false,
    expression: DataFrameExpression<G, C>,
): GroupBy<T, G> = toDataFrame().sortBy {
    createColumnFromGroupExpression(this, expression).nullsLast(nullsLast)
}.asGroupBy(groups)

public fun <T, G, C> GroupBy<T, G>.sortByGroupDesc(
    nullsLast: Boolean = false,
    expression: DataFrameExpression<G, C>,
): GroupBy<T, G> = toDataFrame().sortBy {
    createColumnFromGroupExpression(this, expression).desc().nullsLast(nullsLast)
}.asGroupBy(groups)

public fun <T, G> GroupBy<T, G>.sortByCountAsc(): GroupBy<T, G> = sortByGroup { nrow }

public fun <T, G> GroupBy<T, G>.sortByCount(): GroupBy<T, G> = sortByGroupDesc { nrow }

public fun <T, G> GroupBy<T, G>.sortByKeyDesc(nullsLast: Boolean = false): GroupBy<T, G> = toDataFrame()
    .sortBy { keys.columns().toColumnSet().desc().nullsLast(nullsLast) }.asGroupBy(groups)

public fun <T, G> GroupBy<T, G>.sortByKey(nullsLast: Boolean = false): GroupBy<T, G> = toDataFrame()
    .sortBy { keys.columns().toColumnSet().nullsLast(nullsLast) }.asGroupBy(groups)

// endregion
