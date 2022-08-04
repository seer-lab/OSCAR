import subprocess

cmd = subprocess.run(f"find ../ibm/* | grep .java", shell=True, stdout=subprocess.PIPE,
                     stderr=subprocess.PIPE)

files = cmd.stdout.decode('utf-8').split("\n")

program_results = {}
program_results_array = {}

for file in files:
    if len(file.split("../ibm")) < 2:
        continue

    program = file.split("../ibm/")[1].split("/")[0]
    if program not in program_results:
        program_results[program] = {}

    cmd = subprocess.run(f"txl ../ibm/ccmetrics/CCMetrics.Txl  {file}", shell=True, stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)

    results_parsed = []

    for result in cmd.stderr.decode('utf-8').split("\n"):
        if result.strip().startswith("# of "):
            result_left = result.split("# of ")[1].split("=")[0].strip()

            result_right = result.split("=")[1].strip()

            if len(result_right.split("(")) > 1:
                result_right = result_right.split("(")[0].strip()

            if result_left not in program_results[program]:
                program_results[program][result_left] = int(result_right)
            else:
                program_results[program][result_left] = program_results[program][result_left] + int(result_right)

for p in program_results:
    print(p)
    i = 0
    for r in program_results[p]:
        i = i + 1
        if i > 3 and i != 8:
            continue

        print(f"{program_results[p][r]} {r}")
    print("-------------------")
