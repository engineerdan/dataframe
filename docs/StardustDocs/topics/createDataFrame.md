[//]: # (title: Create DataFrame)
<!---IMPORT org.jetbrains.kotlinx.dataframe.samples.api.Create-->

This section describes ways to create [`DataFrame`](DataFrame.md).

### dataFrameOf

Returns [`DataFrame`](DataFrame.md) with given column names and values.

```kotlin
dataFrameOf(columnNames)(columnValues)
```

<!---FUN createDataFrameOf-->

```kotlin
// DataFrame with 2 columns and 3 rows
val df = dataFrameOf("name", "age")(
    "Alice", 15,
    "Bob", 20,
    "Mark", 100
)
```

<!---END-->

<!---FUN createDataFrameWithFill-->

```kotlin
// DataFrame with columns from 'a' to 'z' and values from 1 to 10 for each column
val df = dataFrameOf('a'..'z') { 1..10 }
```

<!---END-->

<!---FUN createDataFrameWithRandom-->

```kotlin
// DataFrame with 5 columns filled with 7 random double values:
val names = (1..5).map { "column$it" }
val df = dataFrameOf(names).randomDouble(7)
```

<!---END-->

<!---FUN createDataFrameFillConstant-->

```kotlin
val names = listOf("first", "second", "third")

// DataFrame with 3 columns, fill each column with 15 `true` values
val df = dataFrameOf(names).fill(15, true)
```

<!---END-->

<!---FUN createDataFrameFromColumns-->

```kotlin
val name by columnOf("Alice", "Bob")
val age by columnOf(15, 20)

// DataFrame with 2 columns
val df = dataFrameOf(name, age)
```

<!---END-->

### toDataFrame

`DataFrame` from `Iterable<DataColumn>`:

<!---FUN createDataFrameFromIterable-->

```kotlin
val name by columnOf("Alice", "Bob")
val age by columnOf(15, 20)

// DataFrame with 2 columns
listOf(name, age).toDataFrame()
```

<!---END-->

`DataFrame` from `Map<String, List<*>>`:

<!---FUN createDataFrameFromMap-->

```kotlin
val map = mapOf("name" to listOf("Alice", "Bob"), "age" to listOf(15, 20))

// DataFrame with 2 columns
map.toDataFrame()
```

<!---END-->