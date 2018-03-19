package timber.log;

import org.jetbrains.annotations.Nullable;

class PermanentlyTaggedTree extends Timber.Tree {

  private final Timber.Tree[] forest;

  private final String permanentTag;

  PermanentlyTaggedTree(Timber.Tree[] forest, String permanentTag) {
    this.forest = forest;
    this.permanentTag = permanentTag;
  }

  @Override
  public void d(Throwable t) {
    super.d(t);
    resetExplicitTag();
  }

  @Override
  public void d(String message, Object... args) {
    super.d(message, args);
    resetExplicitTag();
  }

  @Override
  public void d(Throwable t, String message, Object... args) {
    super.d(t, message, args);
    resetExplicitTag();
  }


    @Override
  public void e(Throwable t) {
    super.e(t);
    resetExplicitTag();
  }

  @Override
  public void e(String message, Object... args) {
    super.e(message, args);
    resetExplicitTag();
  }

  @Override
  public void e(Throwable t, String message, Object... args) {
    super.e(t, message, args);
    resetExplicitTag();
  }


  @Override
  public void i(Throwable t) {
    super.i(t);
    resetExplicitTag();
  }

  @Override
  public void i(String message, Object... args) {
    super.i(message, args);
    resetExplicitTag();
  }

  @Override
  public void i(Throwable t, String message, Object... args) {
    super.i(t, message, args);
    resetExplicitTag();
  }

  @Override
  @Nullable String getTag() {
    final String temporaryTag = explicitTag.get();
    if (temporaryTag != null) {
      return temporaryTag;
    }

    return permanentTag;
  }

  @Override
  protected void log(int priority, String tag, String message, Throwable t) {
    for (Timber.Tree tree : forest) {
      tree.log(priority, tag, message, t);
    }
  }

  private void resetExplicitTag() {
    if (explicitTag.get() != null) {
      explicitTag.remove();
    }
  }

  @Override
  public void v(Throwable t) {
    super.v(t);
    resetExplicitTag();
  }

  @Override
  public void v(String message, Object... args) {
    super.v(message, args);
    resetExplicitTag();
  }

  @Override
  public void v(Throwable t, String message, Object... args) {
    super.v(t, message, args);
    resetExplicitTag();
  }

  @Override
  public void w(Throwable t) {
    super.w(t);
    resetExplicitTag();
  }

  @Override
  public void w(String message, Object... args) {
    super.w(message, args);
    resetExplicitTag();
  }

  @Override
  public void w(Throwable t, String message, Object... args) {
    super.w(t, message, args);
    resetExplicitTag();
  }


  @Override
  public void wtf(Throwable t) {
    super.wtf(t);
    resetExplicitTag();
  }

  @Override
  public void wtf(String message, Object... args) {
    super.wtf(message, args);
    resetExplicitTag();
  }

  @Override
  public void wtf(Throwable t, String message, Object... args) {
    super.wtf(t, message, args);
    resetExplicitTag();
  }
}
