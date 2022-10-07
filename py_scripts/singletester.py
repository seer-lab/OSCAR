import subprocess
from tqdm import tqdm
import numpy as np

SORTED_DEFAULT_VALUES = sorted([0, 1, 10, 100, 1000, 10000, 100000, 1000000])

OSCAR_DIR = "../"
OSCAR_ARGS = "ibm/account account.Main output"
PROGRAM = "../../output account.Main"
TESTSCRIPT_ARGS = "-j " if PROGRAM.endswith(".jar") else " " + "-da 2 -r 120 -dri"
NUMBER_RUNS = [20]  # SORTED_DEFAULT_VALUES  # [5, 10, 15]
NUMBER_THREADS = ["out lot"]  # SORTED_DEFAULT_VALUES  # [3]
FIXED_ARGS = " -lfo -m 1 -M 10 -nc sb lb -nl svbasfa svbbsfa tbbtr -p 0"
OUTPUT_FLAGS = []
DISABLE_COVERAGE = False
DISABLE_INTERLEAVING_ANALYSIS = False

#    "-p 0",
#    "-p 0.5",
#    "-p 0.10",
#    "-p 0.25",
#    "-p 0.50",
#    "-p 0.75",
#    "-p 1",

if DISABLE_COVERAGE:
    TESTSCRIPT_ARGS += " -dc"

if DISABLE_INTERLEAVING_ANALYSIS:
    TESTSCRIPT_ARGS += " -di"

n_runs = ",".join([str(element) for element in NUMBER_RUNS])

DISTANCE_ALG = ""

# Compile program
program_location = OSCAR_DIR + OSCAR_ARGS.split(" ")[0]
subprocess.run(f"cd {program_location} && javac $(find ./* | grep .java)", shell=True, stdout=subprocess.PIPE,
               stderr=subprocess.PIPE)

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

if len(OUTPUT_FLAGS) > 0:
    output_flags = ",".join([str(element) for element in OUTPUT_FLAGS])
    output_flags = f" -of {output_flags}"
else:
    output_flags = ""

p_args = f"{FIXED_ARGS}"
t_args = f"{TESTSCRIPT_ARGS} -c {n_runs}"

cmd = f'cd testscript && python3 testscript.py {PROGRAM} \"-a {n_threads} {p_args}\" {t_args} {output_flags}'
result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

print(result.stderr.decode('utf-8'))
print(result.stdout.decode('utf-8'))
