import subprocess
from tqdm import tqdm
import numpy as np

SORTED_DEFAULT_VALUES = sorted([50, 100, 250, 500, 1000, 2500, 5000])

PROGRAM = "../output oscar_out.jar"
TESTSCRIPT_ARGS = "-j" if PROGRAM.endswith(".jar") else ""
DISABLE_COVERAGE = False
NUMBER_RUNS = [10]  # SORTED_DEFAULT_VALUES
NUMBER_THREADS = [2] #SORTED_DEFAULT_VALUES  # [3]
FIXED_ARGS = "-lfo -m 1"

VARIABLE_ARGS = [
    "-M 5",
    "-M 10",
    "-M 25",
    "-M 50",
    "-M 10 -y",
    "-M 100 -y",
    "-M 500 -y",
    "-M 1000 -y",
    "-d",
]

if DISABLE_COVERAGE:
    TESTSCRIPT_ARGS += " -dc"


def flatten_results_map(results_map, v_arg):
    total = f"    % {v_arg}\n"
    total += "    \\addplot coordinates {\n"
    total += "        "
    for key in results_map.keys():
        total += f"({key}, {round(results_map[key], 2)})"

    total += "\n    };\n"
    return total


v_arg_avg_run_times = {}
v_arg_uniq_interleavings = {}
v_arg_avg_coverages = {}
v_arg_std_coverages = {}
v_arg_avg_coverages_no_pairs = {}
v_arg_std_coverages_no_pairs = {}

# Run multiple times
for v_arg in tqdm(VARIABLE_ARGS, desc="Variable Args"):
    avg_run_times = {}
    uniq_interleavings = {}
    avg_coverages = {}
    std_coverages = {}
    avg_coverages_no_pairs = {}
    std_coverages_no_pairs = {}

    for n_threads in tqdm(NUMBER_THREADS, desc="Number of Threads", leave=False):
        run_times = []

        for n_runs in tqdm(NUMBER_RUNS, desc="Number of Runs", leave=False):
            p_args = f"{FIXED_ARGS} {v_arg}"
            t_args = f"{TESTSCRIPT_ARGS} -c {n_runs}"

            cmd = f'python3 testscript.py {PROGRAM} \"-a {n_threads} {p_args}\" {t_args}'
            result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

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

                if "Average runtime (ms):" in line:
                    run_times.append(float(line.split(": ")[1]))

                if not DISABLE_COVERAGE:
                    if "Average Levenshtein distance:" in line:
                        avg_coverages[n_runs] = float(line.split(": ")[1])

                    if "Levenshtein distance standard deviation:" in line:
                        std_coverages[n_runs] = float(line.split(": ")[1])

                    if "Average Levenshtein distance (No Pairs):" in line:
                        avg_coverages_no_pairs[n_runs] = float(line.split(": ")[1])

                    if "Levenshtein distance standard deviation (No Pairs):" in line:
                        std_coverages_no_pairs[n_runs] = float(line.split(": ")[1])

        avg_run_times[n_threads] = np.average(run_times)

    v_arg_avg_run_times[v_arg] = flatten_results_map(avg_run_times, v_arg)
    v_arg_uniq_interleavings[v_arg] = flatten_results_map(uniq_interleavings, v_arg)
    v_arg_avg_coverages[v_arg] = flatten_results_map(avg_coverages, v_arg)
    v_arg_std_coverages[v_arg] = flatten_results_map(std_coverages, v_arg)
    v_arg_std_coverages_no_pairs[v_arg] = flatten_results_map(std_coverages_no_pairs, v_arg)
    v_arg_avg_coverages_no_pairs[v_arg] = flatten_results_map(avg_coverages_no_pairs, v_arg)

print()
print()

print("Average runtime: ")
for v_arg in VARIABLE_ARGS:
    print(v_arg_avg_run_times[v_arg])

if not DISABLE_COVERAGE:
    print("Unique interleavings: ")
    for v_arg in VARIABLE_ARGS:
        print(v_arg_uniq_interleavings[v_arg])

    print("Average Levenshtein distance: ")
    for v_arg in VARIABLE_ARGS:
        print(v_arg_avg_coverages[v_arg])

    print("Levenshtein distance standard deviation: ")
    for v_arg in VARIABLE_ARGS:
        print(v_arg_std_coverages[v_arg])

    print("Average Levenshtein distance (no pairs): ")
    for v_arg in VARIABLE_ARGS:
        print(v_arg_avg_coverages_no_pairs[v_arg])

    print("Levenshtein distance standard deviation (no pairs): ")
    for v_arg in VARIABLE_ARGS:
        print(v_arg_std_coverages_no_pairs[v_arg])
