#!/bin/bash
now=$(date)

python3 -m venv venv
source venv/bin/activate
pip install python-levenshtein numpy pathlib tqdm jellyfish datasketch nltk

python3 singletester.py >"${now}"-results.txt