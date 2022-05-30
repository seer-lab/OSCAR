import subprocess
from tqdm import tqdm
import numpy as np

SORTED_DEFAULT_VALUES = sorted([10, 25, 50, 100, 250, 500, 1000])

PROGRAM = "../output PrintID"
PROGRAM_ARGS = "-lfo"
TESTSCRIPT_ARGS = "-j" if PROGRAM.endswith(".jar") else ""
YIELD_MODE = False
DISABLE_COVERAGE = True
DISABLE_NOISE = True

MIN_NOISE = 1  # -m
MAX_NOISE = 1200  # -M
VARIABLE_ARGS = SORTED_DEFAULT_VALUES  # [5]
NUMBER_RUNS = [10]  # SORTED_DEFAULT_VALUES

if DISABLE_NOISE:
    PROGRAM_ARGS += " -d"
else:
    PROGRAM_ARGS += f" -m {MIN_NOISE} -M {MAX_NOISE}"

if YIELD_MODE and not DISABLE_NOISE:
    PROGRAM_ARGS += " -y"

if DISABLE_COVERAGE:
    TESTSCRIPT_ARGS += " -dc"


def print_results_from_runs(results_map):
    total = ""
    for r in NUMBER_RUNS:
        total += f"({r},{results_map[r]})"
    return total


def print_results_from_vargs(results_map):
    total = ""
    for va in VARIABLE_ARGS:
        total += f"({va}, {round(results_map[va], 2)})"
    return total


uniq_interleavings = {}
avg_run_times = {}
avg_coverages = {}
std_coverages = {}

# Run multiple times
for v_arg in tqdm(VARIABLE_ARGS):
    run_times = []
    for n_runs in tqdm(NUMBER_RUNS, leave=False):
        p_args = f"-a {v_arg} {PROGRAM_ARGS}"
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

            if not DISABLE_COVERAGE:
                if "Average Levenshtein distance" in line:
                    avg_coverages[n_runs] = float(line.split(": ")[1])

                if "Levenshtein distance standard deviation" in line:
                    std_coverages[n_runs] = float(line.split(": ")[1])

    avg_run_times[v_arg] = np.average(run_times)

    print()
    print("Unique interleavings: " + print_results_from_runs(uniq_interleavings))

    if not DISABLE_COVERAGE:
        print("Average Levenshtein distance: " + print_results_from_runs(avg_coverages))
        print("Levenshtein distance standard deviation: " + print_results_from_runs(std_coverages))

print()
print("Average runtime: " + print_results_from_vargs(avg_run_times))
