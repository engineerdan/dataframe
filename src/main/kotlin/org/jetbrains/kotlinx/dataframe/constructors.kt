package org.jetbrains.kotlinx.dataframe

import org.jetbrains.kotlinx.dataframe.api.AddExpression
import org.jetbrains.kotlinx.dataframe.api.toAnyFrame
import org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.FrameColumn
import org.jetbrains.kotlinx.dataframe.impl.DataFrameImpl
import org.jetbrains.kotlinx.dataframe.impl.EmptyMany
import org.jetbrains.kotlinx.dataframe.impl.ManyImpl
import org.jetbrains.kotlinx.dataframe.impl.asList
import org.jetbrains.kotlinx.dataframe.impl.columns.ColumnAccessorImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.ColumnWithParent
import org.jetbrains.kotlinx.dataframe.impl.columns.createColumn
import org.jetbrains.kotlinx.dataframe.impl.columns.newColumn
import org.jetbrains.kotlinx.dataframe.impl.columns.newColumnWithActualType
import org.jetbrains.kotlinx.dataframe.impl.getType
import kotlin.random.Random
import kotlin.reflect.KProperty
import kotlin.reflect.full.withNullability

// region create ColumnAccessor

public fun <T> column(): ColumnDelegate<T> = ColumnDelegate()

public fun columnGroup(): ColumnDelegate<AnyRow> = column()

public fun <T> MapColumnReference.column(): ColumnDelegate<T> = ColumnDelegate<T>(this)

public fun <T> MapColumnReference.column(name: String): ColumnAccessor<T> = ColumnAccessorImpl(path() + name)

public fun columnGroup(parent: MapColumnReference): ColumnDelegate<AnyRow> = parent.column()

public fun frameColumn(): ColumnDelegate<AnyFrame> = column()

public fun <T> columnMany(): ColumnDelegate<Many<T>> = column()

public fun <T> columnGroup(name: String): ColumnAccessor<DataRow<T>> = column(name)

public fun <T> frameColumn(name: String): ColumnAccessor<DataFrame<T>> = column(name)

public fun <T> columnMany(name: String): ColumnAccessor<Many<T>> = column(name)

public fun <T> column(name: String): ColumnAccessor<T> = ColumnAccessorImpl(name)

public class ColumnDelegate<T>(private val parent: MapColumnReference? = null) {
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): ColumnAccessor<T> = named(property.name)

    public infix fun named(name: String): ColumnAccessor<T> =
        parent?.let { ColumnAccessorImpl(it.path() + name) } ?: ColumnAccessorImpl(name)
}

// endregion

// region create DataColumn

public inline fun <reified T> columnOf(vararg values: T): DataColumn<T> = createColumn(values.asIterable(), getType<T>(), true)

public fun columnOf(vararg values: AnyColumn): DataColumn<AnyRow> = columnOf(values.asIterable())

public fun <T> columnOf(vararg frames: DataFrame<T>?): FrameColumn<T> = columnOf(frames.asIterable())

public fun columnOf(columns: Iterable<AnyColumn>): DataColumn<AnyRow> = DataColumn.create("", dataFrameOf(columns)) as DataColumn<AnyRow>

public fun <T> columnOf(frames: Iterable<DataFrame<T>?>): FrameColumn<T> = DataColumn.create("", frames.toList())

public inline fun <reified T> column(values: Iterable<T>): DataColumn<T> = createColumn(values, getType<T>(), false)

// TODO: replace with extension
public inline fun <reified T> column(name: String, values: List<T>): DataColumn<T> = when {
    values.size > 0 && values.all { it is AnyCol } -> DataColumn.create(
        name,
        values.map { it as AnyCol }.toAnyFrame()
    ) as DataColumn<T>
    else -> column(name, values, values.any { it == null })
}

// TODO: replace with extension
public inline fun <reified T> column(name: String, values: List<T>, hasNulls: Boolean): DataColumn<T> =
    DataColumn.create(name, values, getType<T>().withNullability(hasNulls))

// endregion

// region create DataFrame

public fun dataFrameOf(columns: Iterable<AnyColumn>): AnyFrame {
    fun AnyColumn.unbox(): AnyCol = when (this) {
        is ColumnWithPath<*> -> data.unbox()
        is ColumnWithParent<*> -> source.unbox()
        else -> this as AnyCol
    }

    val cols = columns.map { it.unbox() }
    if (cols.isEmpty()) return DataFrame.empty()
    return DataFrameImpl<Unit>(cols)
}

public fun dataFrameOf(vararg header: ColumnReference<*>): DataFrameBuilder = DataFrameBuilder(header.map { it.name() })

public fun dataFrameOf(vararg columns: AnyColumn): AnyFrame = dataFrameOf(columns.asIterable())

public fun dataFrameOf(vararg header: String): DataFrameBuilder = dataFrameOf(header.toList())

public inline fun <T, reified C> dataFrameOf(first: T, second: T, vararg other: T, fill: (T) -> Iterable<C>): AnyFrame = dataFrameOf(listOf(first, second) + other, fill)

public fun <T> dataFrameOf(first: T, second: T, vararg other: T): DataFrameBuilder = dataFrameOf((listOf(first, second) + other).map { it.toString() })

public fun <T> dataFrameOf(header: Iterable<T>): DataFrameBuilder = dataFrameOf(header.map { it.toString() })

public inline fun <T, reified C> dataFrameOf(header: Iterable<T>, fill: (T) -> Iterable<C>): AnyFrame = header.map { value -> fill(value).asList().let { DataColumn.createWithNullCheck(value.toString(), it) } }.toAnyFrame()

public fun dataFrameOf(header: CharProgression): DataFrameBuilder = dataFrameOf(header.map { it.toString() })

public fun dataFrameOf(header: List<String>): DataFrameBuilder = DataFrameBuilder(header)

public class DataFrameBuilder(private val header: List<String>) {

    public operator fun invoke(vararg columns: AnyCol): AnyFrame = invoke(columns.asIterable())

    public operator fun invoke(columns: Iterable<AnyCol>): AnyFrame {
        val cols = columns.asList()
        require(cols.size == header.size) { "Number of columns differs from number of column names" }
        return cols.mapIndexed { i, col ->
            col.rename(header[i])
        }.toAnyFrame()
    }

    public operator fun invoke(vararg values: Any?): AnyFrame = withValues(values.asIterable())

    @JvmName("invoke1")
    internal fun withValues(values: Iterable<Any?>): AnyFrame {
        val list = values.asList()

        val ncol = header.size

        require(header.size > 0 && list.size.rem(ncol) == 0) {
            "Number of values ${list.size} is not divisible by number of columns $ncol"
        }

        val nrow = list.size / ncol

        return (0 until ncol).map { col ->
            val colValues = (0 until nrow).map { row ->
                list[row * ncol + col]
            }
            DataColumn.create(header[col], colValues)
        }.toAnyFrame()
    }

    public operator fun invoke(args: Sequence<Any?>): AnyFrame = invoke(*args.toList().toTypedArray())

    public fun withColumns(columnBuilder: (String) -> AnyCol): AnyFrame = header.map(columnBuilder).toAnyFrame()

    public inline operator fun <reified T> invoke(crossinline valuesBuilder: (String) -> Iterable<T>): AnyFrame = withColumns { name -> valuesBuilder(name).let { DataColumn.createWithNullCheck(name, it.asList()) } }

    public inline fun <reified C> fill(nrow: Int, value: C): AnyFrame = withColumns { name -> DataColumn.create(name, List(nrow) { value }, getType<C>().withNullability(value == null)) }

    public inline fun <reified C> nulls(nrow: Int): AnyFrame = fill<C?>(nrow, null)

    public inline fun <reified C> fillIndexed(nrow: Int, crossinline init: (Int, String) -> C): AnyFrame = withColumns { name -> DataColumn.createWithNullCheck(name, List(nrow) { init(it, name) }) }

    public inline fun <reified C> fill(nrow: Int, crossinline init: (Int) -> C): AnyFrame = withColumns { name -> DataColumn.createWithNullCheck(name, List(nrow, init)) }

    private inline fun <reified C> fillNotNull(nrow: Int, crossinline init: (Int) -> C) = withColumns { name -> DataColumn.create(name, List(nrow, init), getType<C>()) }

    public fun randomInt(nrow: Int): AnyFrame = fillNotNull(nrow) { Random.nextInt() }

    public fun randomDouble(nrow: Int): AnyFrame = fillNotNull(nrow) { Random.nextDouble() }

    public fun randomFloat(nrow: Int): AnyFrame = fillNotNull(nrow) { Random.nextFloat() }

    public fun randomBoolean(nrow: Int): AnyFrame = fillNotNull(nrow) { Random.nextBoolean() }
}

public fun emptyDataFrame(nrow: Int): AnyFrame = DataFrame.empty(nrow)

// endregion

// region create ColumnPath

public fun pathOf(vararg columnNames: String): ColumnPath = ColumnPath(columnNames.asList())

// endregion

// region create DataColumn from DataFrame

public inline fun <T, reified R> DataFrameBase<T>.newColumn(
    name: String = "",
    noinline expression: AddExpression<T, R>
): DataColumn<R> = newColumn(name, false, expression)

public inline fun <T, reified R> DataFrameBase<T>.newColumn(
    name: String = "",
    useActualType: Boolean,
    noinline expression: AddExpression<T, R>
): DataColumn<R> {
    if (useActualType) return newColumnWithActualType(name, expression)
    return newColumn(getType<R>(), name, expression)
}

// endregion

// region create Many

public fun <T> emptyMany(): Many<T> = EmptyMany

public fun <T> manyOf(element: T): Many<T> = ManyImpl(listOf(element))

public fun <T> manyOf(vararg values: T): Many<T> = ManyImpl(listOf(*values))

// endregion