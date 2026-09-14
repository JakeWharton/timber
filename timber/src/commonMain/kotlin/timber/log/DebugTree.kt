package timber.log

import timber.log.Timber.Tree

/** A [Tree] for debug builds. Automatically infers the tag from the calling class. */
expect class DebugTree(): Tree
