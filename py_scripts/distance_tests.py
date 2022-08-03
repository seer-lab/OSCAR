import jellyfish as jf


class test():
    def __init__(self, name, int1, int2):
        self.name = name
        self.int1 = int1
        self.int2 = int2


tests = []

tests.append(test("Short routine with no difference", "ABC", "ABC"))
tests.append(test("Short routine with small difference", "ABC", "ACB"))
tests.append(test("Short routine with a loop", "ABC", "ABBBC"))
tests.append(test("Short routine with a long loop", "ABC", "ABBBBBBC"))

tests.append(test("Totally different routine", "ABC", "DEF"))
tests.append(test("Different size routine", "ABCD", "ABCDEF"))

tests.append(test("Long routine where a whole block is in the end", "ABCDEFGHIJ", "DEFGHIJABC"))
tests.append(test("Long routine but alternated", "A1B2C3D4", "ABCD1234"))

tests.append(test("Very long routine with difference at the middle", "ABCDEFGHIJLMNO", "ABCDELNJOMFGHI"))

# Calculate average ratio
for test in tests:
    # Levenshtein
    print("------------------------------------------------------------")
    print(test.name)
    print(f"{test.int1} - {test.int2}")
    print("------------------------------------------------------------")
    print(f"Levenshtein distance: {jf.levenshtein_distance(test.int1, test.int2)}")
    print(f"Damerau Levenshtein distance: {jf.damerau_levenshtein_distance(test.int1, test.int2)}")
    print(f"Hamming distance: {jf.hamming_distance(test.int1, test.int2)}")
    print(f"Jaro similarity: {jf.jaro_similarity(test.int1, test.int2)}")
    print(f"Jaro-Wrinkler similarity: {jf.jaro_winkler_similarity(test.int1, test.int2)}")
    print()
