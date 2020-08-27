package dev.msfjarvis.todo.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.msfjarvis.todo.data.model.TodoItem

@Database(
  entities = [
    TodoItem::class,
  ],
  version = 1,
  exportSchema = false,
)
@TypeConverters(DateTimeTypeConverters::class)
abstract class TodoDatabase : RoomDatabase() {
  abstract fun todoItemsDao(): TodoItemDao
}
