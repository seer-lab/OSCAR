import argparse

# Create parser for reading program arguments
import hashlib
import os
import shutil
import subprocess
from pathlib import Path

argparser = argparse.ArgumentParser(
    prog='testscript',
    description='Automate OSCAR\'s noise injection routine.',
    epilog='Run with argument -h for help.'
)

argparser.add_argument('program_dir', help='location of program to run.')
argparser.add_argument('main_class', help='name of program\'s main class to run.')
argparser.add_argument('program_args', type=str, help='Arguments to be passed to program.')

argparser.add_argument('-c', '--count', default=30, type=int, help='Number of times to run program.')

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

    result = subprocess.run(
        f'java {argv.main_class} {argv.program_args}',
        shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    )

print(f'Finished running. Analyzing files.')

## Try to analyze created files
hash_function = hashlib.sha512()
os.chdir('oscar_output')

files = os.listdir('.')

interleavings = []

for file in files:
    content = open(file, 'r').read().encode('utf-8')

    hashed_content = hashlib.sha512(content).hexdigest()
    interleavings.append(hashed_content)

print(f'Found {len(set(interleavings))} unique interleavings in a total of {len(interleavings)}.')