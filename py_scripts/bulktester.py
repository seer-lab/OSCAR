import subprocess
from tqdm import tqdm
import numpy as np

SORTED_DEFAULT_VALUES = sorted([10])

OSCAR_DIR = "../"
OSCAR_ARGS = "ibm/bubblesort2 tr.Loader output"
PROGRAM = "../../output tr.Loader"
TESTSCRIPT_ARGS = "-j " if PROGRAM.endswith(".jar") else " " + "-da 2"
DISABLE_COVERAGE = True
NUMBER_RUNS = SORTED_DEFAULT_VALUES  # [5, 10, 15]
NUMBER_THREADS = ["out little"]  # SORTED_DEFAULT_VALUES  # [3]
FIXED_ARGS = "-lfo -np tbbtr svbbsfa svbasfa -nc lb sb -m 1"
OUTPUT_FLAGS = ["finished","SortProgram"]

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

DISTANCE_ALG = ""

# Compile program
program_location = OSCAR_DIR + OSCAR_ARGS.split(" ")[0]
subprocess.run(f"cd {program_location} && javac $(find ./* | grep .java)", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Run Oscar
result = subprocess.run(f"cd {OSCAR_DIR} && mvn clean", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
if result.returncode != 0:
    print(result.stderr.decode('utf-8'))
    print(result.stdout.decode('utf-8'))
    exit(1)

result = subprocess.run(f"cd {OSCAR_DIR} && mvn compile", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
if result.returncode != 0:
    print(result.stderr.decode('utf-8'))
    print(result.stdout.decode('utf-8'))
    exit(1)

result = subprocess.run(f"cd {OSCAR_DIR} && mvn exec:java -Dexec.mainClass=oscar.Main -Dexec.args=\"{OSCAR_ARGS}\"",
                        shell=True,
                        stdout=subprocess.PIPE, stderr=subprocess.PIPE)
if result.returncode != 0:
    print(result.stderr.decode('utf-8'))
    print(result.stdout.decode('utf-8'))
    exit(1)

output_flags_per_args = {}

# Run multiple times
for v_arg in tqdm(VARIABLE_ARGS, desc="Variable Args"):
    avg_run_times = {}
    output_flags_detected = {}

    for n_threads in tqdm(NUMBER_THREADS, desc="Number of Threads", leave=False):
        run_times = []

        output_flags = ",".join([str(element) for element in OUTPUT_FLAGS])

        p_args = f"{FIXED_ARGS} {v_arg}"
        t_args = f"{TESTSCRIPT_ARGS} -c {n_runs}"

        cmd = f'cd testscript && python3 testscript.py {PROGRAM} \"-a {n_threads} {p_args}\" {t_args} -of {output_flags}'
        result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        if result.returncode != 0:
            print(result.stderr.decode('utf-8'))
            print(result.stdout.decode('utf-8'))

        # Parse output
        output = result.stdout.decode('utf-8')

        # Parse line by line
        for line in output.split("\n"):
            if "Detected flag" in line:
                line_clean = line.split("Detected flag")[1]
                flag = line_clean.split("_-_-")[1].split("-_-_")[0].strip()
                flag_count = line_clean.split("-_-_")[1].strip()

                output_flags_detected[flag] = flag_count
            if "Average runtime (ms):" in line:
                oscar_output_reached = True
                run_times.append(float(line.split(": ")[1]))

            if not DISABLE_COVERAGE:
                if "Unique interleavings" in line:
                    uniq_interleavings[v_arg] = line.split(": ")[1].strip()

                if "distance:" in line:
                    avg_coverages[v_arg] = line.split(": ")[1].strip()
                    if DISTANCE_ALG == "":
                        DISTANCE_ALG = line.split("Average")[1].split("distance")[0].strip()

                if "distance standard deviation:" in line:
                    std_coverages[v_arg] = line.split(": ")[1].strip()

                if "Average Cluster Size:" in line:
                    avg_cluster_sizes[v_arg] = line.split(": ")[1].strip()

        avg_run_times[n_threads] = np.average(run_times)

    v_arg_avg_run_times[v_arg] = ""
    for key in avg_run_times.keys():
        v_arg_avg_run_times[v_arg] += f"({key}, {round(avg_run_times[key], 2)})"
    output_flags_per_args[v_arg] = output_flags_detected

print()
print()

if not DISABLE_COVERAGE:
    print("Average runtime: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(v_arg_avg_run_times, v_arg)

    print("Unique interleavings: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(uniq_interleavings, v_arg)

    print(f"Average {DISTANCE_ALG} distance: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(avg_coverages, v_arg)

    print(f"{DISTANCE_ALG} distance standard deviation: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(std_coverages, v_arg)

    print("Average Cluster Size: ")
    for v_arg in VARIABLE_ARGS:
        pgfplots_format(avg_cluster_sizes, v_arg)

print("Program Flags: ")
for v_arg in output_flags_per_args:
    print(f"\t{v_arg}")
    for flag in output_flags_per_args[v_arg]:
        print(f"\t\tFLAG=\"{flag}\" COUNT={output_flags_per_args[v_arg][flag]}")
