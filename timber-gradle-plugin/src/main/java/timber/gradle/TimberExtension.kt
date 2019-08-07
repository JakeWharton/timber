package timber.gradle

open class TimberExtension {
  var variantNameFilter: (String) -> Boolean = { true }
  var classNameFilter: (String) -> Boolean = { true }
}