package oscar.transformers.noisers;

import soot.tagkit.StringConstantValueTag;

public class NoiserTag {
  public static final StringConstantValueTag INSTRUMENTED = new StringConstantValueTag("INSTRUMENTED");

  public static final StringConstantValueTag DISMANTLED_ASSIGNMENT = new StringConstantValueTag("DISMANTLED_ASSIGNMENT");
  public static final StringConstantValueTag THREAD_LAUNCHED = new StringConstantValueTag("THREAD_LAUNCHED");
  public static final StringConstantValueTag BODY_SHARED_VARIABLES_NOISED = new StringConstantValueTag("BODY_SHARED_VARIABLES_NOISED");
}
