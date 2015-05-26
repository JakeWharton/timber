package timber.log.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class FastPrintWriter extends PrintWriter {

  private static Writer sDummyWriter = new Writer() {
    @Override
    public void close() throws IOException {
      UnsupportedOperationException ex
          = new UnsupportedOperationException("Shouldn't be here");
      throw ex;
    }

    @Override
    public void flush() throws IOException {
      close();
    }

    @Override
    public void write(char[] buf, int offset, int count) throws IOException {
      close();
    }
  };

  private final int mBufferLen;
  private final char[] mText;
  private int mPos;

  final private OutputStream mOutputStream;
  final private boolean mAutoFlush;
  final private String mSeparator;

  final private Writer mWriter;

  private CharsetEncoder mCharset;
  final private ByteBuffer mBytes;

  private boolean mIoError;

  /**
   * Constructs a new {@code PrintWriter} with {@code wr} as its target
   * writer and a custom buffer size. The parameter {@code autoFlush} determines
   * if the print writer automatically flushes its contents to the target writer
   * when a newline is encountered.
   *
   * @param wr        the target writer.
   * @param autoFlush indicates whether to flush contents upon encountering a
   *                  newline sequence.
   * @param bufferLen specifies the size of the FastPrintWriter's internal buffer; the
   *                  default is 8192.
   * @throws NullPointerException if {@code wr} is {@code null}.
   */
  public FastPrintWriter(Writer wr, boolean autoFlush, int bufferLen) {
    super(sDummyWriter, autoFlush);
    if (wr == null) {
      throw new NullPointerException("wr is null");
    }
    mBufferLen = bufferLen;
    mText = new char[bufferLen];
    mBytes = null;
    mOutputStream = null;
    mWriter = wr;
    mAutoFlush = autoFlush;
    mSeparator = System.getProperty("line.separator");
    initDefaultEncoder();
  }

  /**
   * Flushes this writer and returns the value of the error flag.
   *
   * @return {@code true} if either an {@code IOException} has been thrown
   * previously or if {@code setError()} has been called;
   * {@code false} otherwise.
   * @see #setError()
   */
  public boolean checkError() {
    flush();
    synchronized (lock) {
      return mIoError;
    }
  }

  /**
   * Sets the error state of the stream to false.
   *
   * @since 1.6
   */
  protected void clearError() {
    synchronized (lock) {
      mIoError = false;
    }
  }

  /**
   * Sets the error flag of this writer to true.
   */
  protected void setError() {
    synchronized (lock) {
      mIoError = true;
    }
  }

  private void initDefaultEncoder() {
    mCharset = Charset.defaultCharset().newEncoder();
    mCharset.onMalformedInput(CodingErrorAction.REPLACE);
    mCharset.onUnmappableCharacter(CodingErrorAction.REPLACE);
  }

  private void appendLocked(char c) throws IOException {
    int pos = mPos;
    if (pos >= (mBufferLen - 1)) {
      flushLocked();
      pos = mPos;
    }
    mText[pos] = c;
    mPos = pos + 1;
  }

  private void appendLocked(String str, int i, final int length) throws IOException {
    final int bufferLen = mBufferLen;
    if (length > bufferLen) {
      final int end = i + length;
      while (i < end) {
        int next = i + bufferLen;
        appendLocked(str, i, next < end ? bufferLen : (end - i));
        i = next;
      }
      return;
    }
    int pos = mPos;
    if ((pos + length) > bufferLen) {
      flushLocked();
      pos = mPos;
    }
    str.getChars(i, i + length, mText, pos);
    mPos = pos + length;
  }

  private void appendLocked(char[] buf, int i, final int length) throws IOException {
    final int bufferLen = mBufferLen;
    if (length > bufferLen) {
      final int end = i + length;
      while (i < end) {
        int next = i + bufferLen;
        appendLocked(buf, i, next < end ? bufferLen : (end - i));
        i = next;
      }
      return;
    }
    int pos = mPos;
    if ((pos + length) > bufferLen) {
      flushLocked();
      pos = mPos;
    }
    System.arraycopy(buf, i, mText, pos, length);
    mPos = pos + length;
  }

  private void flushBytesLocked() throws IOException {
    int position = mBytes.position();
    if (position > 0) {
      mBytes.flip();
      mOutputStream.write(mBytes.array(), 0, position);
      mBytes.clear();
    }
  }

  private void flushLocked() throws IOException {
    //Log.i("PackageManager", "flush mPos=" + mPos);
    if (mPos > 0) {
      if (mOutputStream != null) {
        CharBuffer charBuffer = CharBuffer.wrap(mText, 0, mPos);
        CoderResult result = mCharset.encode(charBuffer, mBytes, true);
        while (true) {
          if (result.isError()) {
            throw new IOException(result.toString());
          } else if (result.isOverflow()) {
            flushBytesLocked();
            result = mCharset.encode(charBuffer, mBytes, true);
            continue;
          }
          break;
        }
        flushBytesLocked();
        mOutputStream.flush();
      } else if (mWriter != null) {
        mWriter.write(mText, 0, mPos);
        mWriter.flush();
      }
      mPos = 0;
    }
  }

  /**
   * Ensures that all pending data is sent out to the target. It also
   * flushes the target. If an I/O error occurs, this writer's error
   * state is set to {@code true}.
   */
  @Override
  public void flush() {
    synchronized (lock) {
      try {
        flushLocked();
        if (mOutputStream != null) {
          mOutputStream.flush();
        } else if (mWriter != null) {
          mWriter.flush();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  @Override
  public void close() {
    synchronized (lock) {
      try {
        flushLocked();
        if (mOutputStream != null) {
          mOutputStream.close();
        } else if (mWriter != null) {
          mWriter.close();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  /**
   * Prints the string representation of the specified character array
   * to the target.
   *
   * @param charArray the character array to print to the target.
   * @see #print(String)
   */
  public void print(char[] charArray) {
    synchronized (lock) {
      try {
        appendLocked(charArray, 0, charArray.length);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Prints the string representation of the specified character to the
   * target.
   *
   * @param ch the character to print to the target.
   * @see #print(String)
   */
  public void print(char ch) {
    synchronized (lock) {
      try {
        appendLocked(ch);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Prints a string to the target. The string is converted to an array of
   * bytes using the encoding chosen during the construction of this writer.
   * The bytes are then written to the target with {@code write(int)}.
   * <p/>
   * If an I/O error occurs, this writer's error flag is set to {@code true}.
   *
   * @param str the string to print to the target.
   * @see #write(int)
   */
  public void print(String str) {
    if (str == null) {
      str = String.valueOf((Object) null);
    }
    synchronized (lock) {
      try {
        appendLocked(str, 0, str.length());
      } catch (IOException e) {
        setError();
      }
    }
  }


  @Override
  public void print(int inum) {
    if (inum == 0) {
      print("0");
    } else {
      super.print(inum);
    }
  }

  @Override
  public void print(long lnum) {
    if (lnum == 0) {
      print("0");
    } else {
      super.print(lnum);
    }
  }

  /**
   * Prints a newline. Flushes this writer if the autoFlush flag is set to {@code true}.
   */
  public void println() {
    synchronized (lock) {
      try {
        appendLocked(mSeparator, 0, mSeparator.length());
        if (mAutoFlush) {
          flushLocked();
        }
      } catch (IOException e) {
        setError();
      }
    }
  }

  @Override
  public void println(int inum) {
    if (inum == 0) {
      println("0");
    } else {
      super.println(inum);
    }
  }

  @Override
  public void println(long lnum) {
    if (lnum == 0) {
      println("0");
    } else {
      super.println(lnum);
    }
  }

  /**
   * Prints the string representation of the character array {@code chars} followed by a newline.
   * Flushes this writer if the autoFlush flag is set to {@code true}.
   */
  public void println(char[] chars) {
    print(chars);
    println();
  }

  /**
   * Prints the string representation of the char {@code c} followed by a newline.
   * Flushes this writer if the autoFlush flag is set to {@code true}.
   */
  public void println(char c) {
    print(c);
    println();
  }

  /**
   * Writes {@code count} characters from {@code buffer} starting at {@code
   * offset} to the target.
   * <p/>
   * This writer's error flag is set to {@code true} if this writer is closed
   * or an I/O error occurs.
   *
   * @param buf    the buffer to write to the target.
   * @param offset the index of the first character in {@code buffer} to write.
   * @param count  the number of characters in {@code buffer} to write.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code count < 0}, or if {@code
   *                                   offset + count} is greater than the length of {@code buf}.
   */
  @Override
  public void write(char[] buf, int offset, int count) {
    synchronized (lock) {
      try {
        appendLocked(buf, offset, count);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Writes one character to the target. Only the two least significant bytes
   * of the integer {@code oneChar} are written.
   * <p/>
   * This writer's error flag is set to {@code true} if this writer is closed
   * or an I/O error occurs.
   *
   * @param oneChar the character to write to the target.
   */
  @Override
  public void write(int oneChar) {
    synchronized (lock) {
      try {
        appendLocked((char) oneChar);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Writes the characters from the specified string to the target.
   *
   * @param str the non-null string containing the characters to write.
   */
  @Override
  public void write(String str) {
    synchronized (lock) {
      try {
        appendLocked(str, 0, str.length());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Writes {@code count} characters from {@code str} starting at {@code
   * offset} to the target.
   *
   * @param str    the non-null string containing the characters to write.
   * @param offset the index of the first character in {@code str} to write.
   * @param count  the number of characters from {@code str} to write.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code count < 0}, or if {@code
   *                                   offset + count} is greater than the length of {@code str}.
   */
  @Override
  public void write(String str, int offset, int count) {
    synchronized (lock) {
      try {
        appendLocked(str, offset, count);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Appends a subsequence of the character sequence {@code csq} to the
   * target. This method works the same way as {@code
   * PrintWriter.print(csq.subsequence(start, end).toString())}. If {@code
   * csq} is {@code null}, then the specified subsequence of the string "null"
   * will be written to the target.
   *
   * @param csq   the character sequence appended to the target.
   * @param start the index of the first char in the character sequence appended
   *              to the target.
   * @param end   the index of the character following the last character of the
   *              subsequence appended to the target.
   * @return this writer.
   * @throws
   *    StringIndexOutOfBoundsException if {@code start > end}, {@code start < 0}, {@code end < 0}
   *                                    or either {@code start} or {@code end} are greater or equal
   *                                    than the length of {@code csq}.
   */
  @Override
  public PrintWriter append(CharSequence csq, int start, int end) {
    if (csq == null) {
      csq = "null";
    }
    String output = csq.subSequence(start, end).toString();
    write(output, 0, output.length());
    return this;
  }

}
