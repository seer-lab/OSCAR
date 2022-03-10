from pathlib import Path
import shutil, os, sys

if __name__ == "__main__":

    if len(sys.argv) < 4:
        sys.exit(1)

    directory = Path(sys.argv[1])
    directory.mkdir(exist_ok=True)

    # Remove current test contents
    for root, dirs, files in os.walk(directory):
        for f in files:
            os.unlink(os.path.join(root, f))
        for d in dirs:
            shutil.rmtree(os.path.join(root, d))
    
    # num_files_per_dir = 10
    num_files_per_dir = int(sys.argv[3])
    # num_dirs = 100 # Number of directories per level
    num_dirs = int(sys.argv[2])

    # Generate test files
    for i in range(num_dirs):
        curr_dir = directory / Path('dir{}/'.format(i+1))
        curr_dir.mkdir(exist_ok=True)
        for j in range(num_files_per_dir):
            (curr_dir / Path('file{}{}.java'.format((i+1), (j+1)))).touch()

    # print('[py] Generated {} files at {}'.format(num_files_per_dir*num_dirs, directory))