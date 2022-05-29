import subprocess
from tqdm import tqdm
import numpy as np

PROGRAM = "../output oscar_out.jar"
PROGRAM_ARGS = "-lfo"
TESTSCRIPT_ARGS = "-j" if PROGRAM.endswith(".jar") else ""
YIELD_MODE = True

MIN_NOISE = 1  # -m
MAX_NOISE = 1200  # -M
PROGRAM_ARGS += f" -m {MIN_NOISE} -M {MAX_NOISE}"
VARIABLE_ARGS = [3]
NUMBER_RUNS = sorted([10, 25, 50, 100, 250, 500, 1000])  # -c

if YIELD_MODE:
    PROGRAM_ARGS += " -y"


def print_results_from_runs(results_map):
    total = ""
    for r in NUMBER_RUNS:
        total += f"({r},{results_map[r]})"
    return total


def print_results_from_vargs(results_map):
    total = ""
    for va in VARIABLE_ARGS:
        total += f"({va},{results_map[va]})"
    return total


uniq_interleavings = {}
avg_run_times = {}
avg_coverages = {}
std_coverages = {}

# Run multiple times
for v_arg in tqdm(VARIABLE_ARGS):
    run_times = []
    for n_runs in tqdm(NUMBER_RUNS, leave=False):
        p_args = f"{PROGRAM_ARGS} -a {v_arg}"
        t_args = f"{TESTSCRIPT_ARGS} -c {n_runs}"

        result = subprocess.run(f'python3 testscript.py {PROGRAM} \"{p_args}\" {t_args}',
                                shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        if result.returncode != 0:
            print(result.stderr.decode('utf-8'))
            print(result.stdout.decode('utf-8'))
            exit(1)

        # Parse output
        output = result.stdout.decode('utf-8')

        # Parse line by line
        for line in output.split("\n"):
            if "Unique interleavings" in line:
                uniq_interleavings[n_runs] = int(line.split(": ")[1])

            if "Average runtime" in line:
                run_times.append(float(line.split(": ")[1]))

            if "Average Levenshtein distance" in line:
                avg_coverages[n_runs] = float(line.split(": ")[1])

            if "Levenshtein distance standard deviation" in line:
                std_coverages[n_runs] = float(line.split(": ")[1])

    avg_run_times[v_arg] = np.average(run_times)

    print()
    print("Unique interleavings: " + print_results_from_runs(uniq_interleavings))
    print("Average Levenshtein distance: " + print_results_from_runs(avg_coverages))
    print("Levenshtein distance standard deviation: " + print_results_from_runs(std_coverages))

print()
print("Average runtime: " + print_results_from_vargs(avg_run_times))
