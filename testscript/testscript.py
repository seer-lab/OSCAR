import argparse

# Create parser for reading program arguments
import os
import shutil
import subprocess
import Levenshtein as ls
import numpy
import numpy as np
import time


# Convert a string to unicode
def to_unicode(code: int):
    return chr(int(str(code).zfill(8), 16))


def flatten_results_map(results_map):
    total = ""
    for key in results_map.keys():
        total += f"({key},{round(results_map[key], 2)})"
    return total


argparser = argparse.ArgumentParser(
    prog='testscript',
    description='Automate OSCAR\'s noise injection routine.',
    epilog='Run with argument -h for help.'
)

argparser.add_argument('program_dir', help='location of program to run.')
argparser.add_argument('executable', help='Name of program\'s main class to run or jar file name.')
argparser.add_argument('program_args', type=str, help='Arguments to be passed to program.')

argparser.add_argument('-c', '--count', default="30", type=str, help='Number of times to run program (comma separated).')
argparser.add_argument('-j', '--jar', action='store_true', help='Run program as a jar.')
argparser.add_argument('-dt', '--disable_thread_ids', action='store_true', help='Disable thread ID parsing.')
argparser.add_argument('-u', '--unordered_thread_ids', action='store_true', help='Maintain original thread ID order.')
argparser.add_argument('-dc', '--disable_coverage', action='store_true', help='Disable coverage analysis.')

argv = argparser.parse_args()

# Check if file exists
if not os.path.isdir(argv.program_dir):
    print(f'Folder {argv.program_dir} not found')
    exit(1)

os.chdir(argv.program_dir)

# Remove old generated files
if os.path.isdir('oscar_output'):
    shutil.rmtree('oscar_output')

###############################################################################################################

# Save runtimes
runtimes = []

run_counts = []
for rc in str(argv.count).split(","):
    run_counts.append(int(rc))

runs = run_counts[len(run_counts) - 1]

print(f'Running program {argv.count} times')

# Run program x times
for i in range(0, runs):
    print(f'Running {i + 1}/{runs}')
    start_time = time.time_ns() / 1_000_000

    if not argv.jar:
        result = subprocess.run(
            f'java {argv.executable} {argv.program_args}',
            shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE
        )
    else:
        result = subprocess.run(
            f'java -jar {argv.executable} {argv.program_args}',
            shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE
        )

    if result.returncode != 0:
        print(result.stderr.decode('utf-8'))
        print(result.stdout.decode('utf-8'))
        exit(1)

    runtimes.append(time.time_ns() / 1_000_000 - start_time)

print(f'Finished running. Analyzing files.')

###############################################################################################################

# Try to analyze created files
os.chdir('oscar_output')
files = os.listdir('.')

location_ids = {}
interleavings = []
interleaving_pairs = {}

for file in files:
    content = open(file, 'r')  # .read()
    thread_ids = []

    interleaving = ''

    # Get all thread ids for ordering
    for line in content:
        thread_id = int(line.split(' ')[0].strip())
        if thread_id not in thread_ids:
            thread_ids.append(thread_id)

    # Check if thread ids should maintain order when mapped
    if argv.unordered_thread_ids:
        thread_ids = numpy.sort(thread_ids)

    # Map thread ids
    mapped_thread_ids = {}
    for i in range(0, len(thread_ids)):
        mapped_thread_ids[thread_ids[i]] = to_unicode(i)

    # Parse normally
    content = open(file, 'r')

    for line in content:
        thread_id = mapped_thread_ids[int(line.split(' ')[0].strip())]

        # Make the interleaving id value start from 0
        location_id = line.split(' ')[1].strip()
        if location_id not in location_ids:
            location_ids[location_id] = to_unicode(len(location_ids) + len(thread_ids))
        location_id = location_ids[location_id]

        # Append content with or without thread id
        interleaving_pair = location_id
        if not argv.disable_thread_ids:
            interleaving_pair = f'{thread_id}{interleaving_pair}'

        # Transform interleaving pair representation in single mapped unicode
        if interleaving_pair not in interleaving_pairs:
            interleaving_pairs[interleaving_pair] = to_unicode(len(interleaving_pairs))
        interleaving += interleaving_pairs[interleaving_pair]

    interleavings.append(interleaving)

###############################################################################################################

print()
print("Results:")
print(f'\tAverage runtime (ms): {round(np.average(runtimes), 0)}')

if not argv.disable_coverage:
    avg_dist_runs = {}
    std_dev_runs = {}
    uniq_interleavings_runs = {}

    for rc in run_counts:
        interleavings_split = interleavings[0:rc]
        uniq_interleavings_runs[rc] = len(set(interleavings_split))

        # For regular pairs
        leven_dists = []

        # Calculate average ratio
        for x in range(0, len(interleavings_split) - 1):
            for y in range(x + 1, len(interleavings_split)):
                leven_dists.append(ls.distance(interleavings_split[x], interleavings_split[y]))

        avg_dist_runs[rc] = round(np.average(leven_dists), 2)
        std_dev_runs[rc] = round(float(np.std(leven_dists)), 2)

    print(f'\tUnique interleavings: {flatten_results_map(uniq_interleavings_runs)}')
    print(f'\tAverage Levenshtein distance: {flatten_results_map(avg_dist_runs)}')
    print(f'\tLevenshtein distance standard deviation: {flatten_results_map(std_dev_runs)}')
