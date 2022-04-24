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
argparser.add_argument('executable', help='Name of program\'s main class to run or jar file name.')
argparser.add_argument('program_args', type=str, help='Arguments to be passed to program.')

argparser.add_argument('-c', '--count', default=30, type=int, help='Number of times to run program.')
argparser.add_argument('-j', '--jar', action='store_true', help='Run program as a jar.')

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

## Try to analyze created files
hash_function = hashlib.sha512()
os.chdir('oscar_output')

files = os.listdir('.')

interleavings = []

for file in files:
    content = open(file, 'r') #.read()

    content_clean = ""
    for line in content:
        content_clean += line.split(" ")[1]

    hashed_content = hashlib.sha512(content_clean.encode('utf-8')).hexdigest()
    interleavings.append(hashed_content)

print(f'Found {len(set(interleavings))} unique types of interleavings in a total of {len(interleavings)}.')
