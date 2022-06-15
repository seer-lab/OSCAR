import subprocess
from tqdm import tqdm
import numpy as np

SORTED_DEFAULT_VALUES = sorted([50, 100, 250, 500, 1000])

PROGRAM = "../output Main"
TESTSCRIPT_ARGS = "-j" if PROGRAM.endswith(".jar") else ""
DISABLE_COVERAGE = False
NUMBER_RUNS = [5, 25, 50]  # [5, 10, 15]
NUMBER_THREADS = [2]  # SORTED_DEFAULT_VALUES  # [3]
FIXED_ARGS = " -lfo -np tbbtr -nc lb sb -m 1"

VARIABLE_ARGS = [
    "-M 5",
    "-M 10",
    "-M 25",
    "-M 5 -y",
    "-M 10 -y",
    "-M 25 -y",
    "-M 50 -y",
    "-d",
]


def pgfplots_format(param, k):
    print(f"    % {k}")
    print("    \\addplot coordinates {")
    print("        " + param[k])
    print("    };")
    print()


if DISABLE_COVERAGE:
    TESTSCRIPT_ARGS += " -dc"

v_arg_avg_run_times = {}
uniq_interleavings = {}
avg_coverages = {}
std_coverages = {}
avg_cluster_sizes = {}

n_runs = ",".join([str(element) for element in NUMBER_RUNS])

# Run multiple times
for v_arg in tqdm(VARIABLE_ARGS, desc="Variable Args"):
    avg_run_times = {}

    for n_threads in tqdm(NUMBER_THREADS, desc="Number of Threads", leave=False):
        run_times = []

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
            if "Average runtime (ms):" in line:
                run_times.append(float(line.split(": ")[1]))

            if not DISABLE_COVERAGE:
                if "Unique interleavings" in line:
                    uniq_interleavings[v_arg] = line.split(": ")[1].strip()

                if "Average Levenshtein distance:" in line:
                    avg_coverages[v_arg] = line.split(": ")[1].strip()

                if "Levenshtein distance standard deviation:" in line:
                    std_coverages[v_arg] = line.split(": ")[1].strip()

                if "Average Cluster Size:" in line:
                    avg_cluster_sizes[v_arg] = line.split(": ")[1].strip()

        avg_run_times[n_threads] = np.average(run_times)

    v_arg_avg_run_times[v_arg] = ""
    for key in avg_run_times.keys():
        v_arg_avg_run_times[v_arg] += f"({key}, {round(avg_run_times[key], 2)})"

print()
print()

if DISABLE_COVERAGE:
    print("Average runtime: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(v_arg_avg_run_times, v_arg)
else:
    print("Unique interleavings: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(uniq_interleavings, v_arg)

    print("Average Levenshtein distance: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(avg_coverages, v_arg)

    print("Levenshtein distance standard deviation: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(std_coverages, v_arg)

    print("Average Cluster Size: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(avg_cluster_sizes, v_arg)
