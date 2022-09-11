import os
import random
import string

# printing lowercase
letters = string.ascii_letters.join(string.ascii_lowercase).join(string.ascii_uppercase)

stmts_per_method = 1000
methods = 10
classes = 1000

os.mkdir("classes")

for z in range(classes):
    randname = ''.join(random.choice(letters) for r in range(30))

    f = open(f"classes/{randname}.java", "w")

    f.write(f"public class {randname} " + "{\n")
    f.write("\tpublic static void main(String[] args) {\n")
    f.write("\t\tSystem.out.println(\"asfasf\");\n")
    f.write("\t}\n")
    f.write("\n")

    for m in range(methods):
        randname = ''.join(random.choice(letters) for r in range(30))

        f.write(f"\tpublic void {randname}(String[] args) " + "{\n")
        for m in range(stmts_per_method):
            f.write("\t\tdoNothing();\n")
        f.write("\t}\n")
        f.write("\n")

    f.write("\tstatic synchronized void doNothing() {\n")
    f.write("\t\treturn;\n")
    f.write("\t}\n")
    f.write("}\n")

    f.close()
