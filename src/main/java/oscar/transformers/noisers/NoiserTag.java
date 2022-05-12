package oscar.transformers.noisers;

import soot.tagkit.StringConstantValueTag;

public class NoiserTag {
  // Thread Creation Noiser


  public static final StringConstantValueTag DISMANTLED_ASSIGNMENT = new StringConstantValueTag("DISMANTLED_ASSIGNMENT");

  public static final StringConstantValueTag BODY_SHARED_VARIABLES_NOISED = new StringConstantValueTag("BODY_SHARED_VARIABLES_NOISED");
}
