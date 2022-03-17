import glob


class RuntimeResult:
    def __init__(self, dataset: str, commit_id: str, runtime: int):
        self.dataset = dataset,
        self.commit_id = commit_id,
        self.runtime = runtime


def load_runtime_results(result_dir):
    result_files = glob.glob(result_dir + "/**/*committimes.txt", recursive=True)

    runtime_results = []

    for path_to_file in result_files:
        with open(path_to_file, "r") as file:
            file_content = file.readlines()
        for line in file_content:
            splits = line.strip().split("___")
            commit_id = splits[0]
            dataset = splits[1]
            # Cut off 'ms' string with [:-2]
            runtime = int(splits[2][:-2])  # type: int
            runtime_results.append(RuntimeResult(dataset, commit_id, runtime))
    return runtime_results


# For debugging
if __name__ == "__main__":
    folder = "../results"
    results = load_runtime_results(folder)
    print(len(results))
    assert len(results) == 1708172
