import argparse

# Create parser for reading program arguments
import os
import shutil
import subprocess
import Levenshtein as ls
import numpy
import numpy as np
from pathlib import Path

argparser = argparse.ArgumentParser(
    prog='testscript',
    description='Automate OSCAR\'s noise injection routine.',
    epilog='Run with argument -h for help.'
)

argparser.add_argument('program_dir', help='location of program to run.')
argparser.add_argument('executable', help='Name of program\'s main class to run or jar file name.')
argparser.add_argument('program_args', type=str, help='Arguments to be passed to program.')

argparser.add_argument('-c', '--count', default=30, type=int, help='Number of times to run program.')
argparser.add_argument('-j', '--jar', action='store_true', help='Run program as a jar.')
argparser.add_argument('-dt', '--disable_thread_ids', action='store_true', help='Disable thread ID parsing.')
argparser.add_argument('-u', '--unordered_thread_ids', action='store_true', help='Maintain original thread ID order.')

argv = argparser.parse_args()

# Check if file exists
if not os.path.isdir(argv.program_dir):
    print(f'Folder {argv.program_dir} not found')
    exit(1)

# Check if temp directory exists and create it
if os.path.isdir('.testscript_temp'):
    shutil.rmtree('.testscript_temp')

if argv.count < 0:
    print('Invalid program run count.')
    exit(1)

os.mkdir('.testscript_temp')

print(f'Running program {argv.count} times')

os.chdir(argv.program_dir)

# Remove old generated files
if os.path.isdir('oscar_output'):
    shutil.rmtree('oscar_output')

for i in range(0, argv.count):
    print(f'Running {i + 1}/{argv.count}')

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

print(f'Finished running. Analyzing files.')

# Try to analyze created files
os.chdir('oscar_output')

files = os.listdir('.')

interleavings = []
interleaving_ids = {}

for file in files:
    content = open(file, 'r')  # .read()
    content_appended = ''

    thread_ids = []
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
        mapped_thread_ids[thread_ids[i]] = i

    # Parse normally
    content = open(file, 'r')

    for line in content:
        thread_id = int(line.split(' ')[0].strip())
        thread_id = mapped_thread_ids[thread_id]

        # Make the interleaving id value start from 0
        interleaving_id = line.split(' ')[1].strip()
        if interleaving_id not in interleaving_ids:
            interleaving_ids[interleaving_id] = len(interleaving_ids) + 1
        interleaving_id = interleaving_ids[interleaving_id]

        # append content with or without thread id
        if argv.disable_thread_ids:
            content_fixed = str(interleaving_id)
        else:
            content_fixed = f'{thread_id}_{interleaving_id}'

        if content_appended == '':
            content_appended += content_fixed
        else:
            content_appended += ' ' + content_fixed

    interleavings.append(content_appended)

# Calculate average ratio
ratios = []

for x in range(0, len(interleavings)):
    for y in range(0, len(interleavings)):
        if x != y:
            ratios.append(ls.ratio(interleavings[x], interleavings[y]))


print(f'Found {len(set(interleavings))} unique types of interleavings in a total of {len(interleavings)}.')
print(f'Difference ratio: {np.average(ratios)}')