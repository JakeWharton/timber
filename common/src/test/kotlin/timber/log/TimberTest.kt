package timber.log

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TimberTest {
  @BeforeTest @AfterTest fun after() {
    Timber.uprootAll()
  }

  @Test fun size() {
    assertEquals(0, Timber.size)

    for (i in 1..50) {
      Timber.plant(ListTree())
      assertEquals(i, Timber.size)
    }

    assertEquals(50, Timber.size)

    Timber.uprootAll()
    assertEquals(0, Timber.size)
  }

  @Test fun plant() {
    val one = ListTree()
    Timber.plant(one)
    val two = ListTree()
    Timber.plant(two)

    assertEquals(listOf(one, two), Timber.trees)
  }

  @Test fun plantVarargs() {
    val one = ListTree()
    val two = ListTree()
    Timber.plant(one, two)

    assertEquals(listOf(one, two), Timber.trees)
  }

  @Test fun plantAll() {
    val one = ListTree()
    val two = ListTree()
    Timber.plantAll(listOf(one, two))

    assertEquals(listOf(one, two), Timber.trees)
  }

  @Test fun uprootThrowsIfMissing() {
    val tree = ListTree()
    assertFailsWith(IllegalArgumentException::class,
        "Cannot uproot tree which is not planted: $tree") {
      Timber.uproot(tree)
    }
  }
}
