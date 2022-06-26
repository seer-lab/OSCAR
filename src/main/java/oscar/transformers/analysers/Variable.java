package oscar.transformers.analysers;

class Variable {
  private final VariableType type;
  private final String name;

  public Variable(String name, VariableType type) {
    this.name = name;
    this.type = type;
  }

  public VariableType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Variable))
      return false;

    return this.name.equals(((Variable) obj).getName());
  }

  enum VariableType {
    FIELD,
    LOCAL
  }
}
