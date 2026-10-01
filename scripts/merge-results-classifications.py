import os
import re
import csv

BASE_FOLDER = "../results/uncertainty/current"
INPUT_CSV = "../results/uncertainty/Final Git Research Repositories - Finished.csv"
OUTPUT_CSV = "../results/uncertainty/Final Git Research Repositories - Finished with Edit Class.csv"
INPUT_FILENAME = "totalresult.metadata.txt"
WARNING_OUTPUT = "../results/uncertainty/warnings.txt"

# 0-based column indices
COL_REPOSITORY = 0
COL_COMMIT = 1
COL_INTERESTING_FILES = 2
COL_MUTATION_START = 10  # "version incompatibility (with older versions)" = column K
COL_MUTATION_END = 31    # "removed unused warning" = column AF (inclusive)
COL_WRONGLY_INCLUDED_START = 32  # AG
COL_WRONGLY_INCLUDED_END = 34    # AI (inclusive)
COL_EDIT_CLASSIFICATION = 35     # AJ

# Regex to detect "https://.../commit/<hash>" and extract the hash
COMMIT_URL_RE = re.compile(r"https?://\S+/commit/([0-9a-fA-F]+)")

# Regex to find "<filename> (<class,class,...>)" pairs in a metadata.txt line.
# Filenames contain no spaces and have an extension (e.g. .c, .h, .cpp).
FILE_CLASS_RE = re.compile(r"([^\s:()]+\.\w+)\s*\(([^()]*)\)")


def parse_metadata_file(path):
    """
    Reads a totalresult.metadata.txt file and builds a dict:
        commit_hash -> [ (filename, [class1, class2, ...]), ... ]
    in the order they appear in the respective line.
    """
    metadata_map = {}
    if not os.path.isfile(path):
        return metadata_map

    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if not line or "/commit/" not in line:
                continue

            url_match = COMMIT_URL_RE.search(line)
            if not url_match:
                continue
            commit_hash = url_match.group(1)

            # Extract all (filename, classes) pairs in the line, in original order
            pairs = []
            for file_match in FILE_CLASS_RE.finditer(line):
                filename = file_match.group(1)
                classes_raw = file_match.group(2)
                classes = [c.strip() for c in classes_raw.split(",") if c.strip()]
                if classes:
                    pairs.append((filename, classes))

            if pairs:
                # If the same hash appears multiple times in the file (shouldn't normally
                # happen), the entries get appended.
                if commit_hash in metadata_map:
                    metadata_map[commit_hash].extend(pairs)
                else:
                    metadata_map[commit_hash] = pairs

    return metadata_map

def load_all_metadata(repositories):
    """
    Loads the corresponding metadata.txt for every repository in column A and returns a dict
        repository_name -> { commit_hash -> [(filename, [classes]), ...] }
    """
    all_metadata = {}
    for repo in repositories:
        path = os.path.join(BASE_FOLDER, repo, INPUT_FILENAME)
        all_metadata[repo] = parse_metadata_file(path)
    return all_metadata


def extract_values_from_cell(cell):
    """
    Extracts all integers contained in a cell. A cell can be empty, contain a single
    number ("2"), or contain several comma-separated, separate markers ("1,2" = the
    numbers 1 AND 2, not a decimal number).
    """
    cell = (cell or "").strip()
    if not cell:
        return []
    values = []
    for part in cell.split(","):
        part = part.strip()
        if part.isdigit():
            values.append(int(part))
    return values


def collect_row_values(row):
    """Collects all numbers from the mutation columns (K-AF) of a row."""
    values = []
    for col in range(COL_MUTATION_START, COL_MUTATION_END + 1):
        if col < len(row):
            values.extend(extract_values_from_cell(row[col]))
    return values


def row_is_wrongly_included(row):
    """Checks whether one of the 'Wrongly included' columns (AG-AI) contains an 'x'."""
    for col in range(COL_WRONGLY_INCLUDED_START, COL_WRONGLY_INCLUDED_END + 1):
        if col < len(row) and row[col].strip().lower() == "x":
            return True
    return False


def ordered_unique_classes(classes):
    """Builds a list of the unique classes, ordered by first appearance."""
    seen = []
    for c in classes:
        if c not in seen:
            seen.append(c)
    return seen


def build_mapping_string(values_in_row, ordered_unique, single_class_name=None):
    """
    Builds the output string '1 = Class, 2 = Class, ...' for the numbers present.

    Normal case: value v is mapped to the v-th unique class (ordered_unique[v-1]).

    Special case (single_class_name is set): all raw entries in metadata.txt are
    identical (only 1 unique class), but the CSV marks more than 1 number (e.g. 1 and 2).
    In this case EVERY number is mapped to the same (single) class, since these are just
    multiple markers for what is content-wise the same change.
    """
    unique_values = sorted(set(values_in_row))
    parts = []
    for v in unique_values:
        if single_class_name is not None:
            parts.append(f"{v} = {single_class_name}")
        else:
            parts.append(f"{v} = {ordered_unique[v - 1]}")
    return ", ".join(parts)


def main():
    warnings = []

    with open(INPUT_CSV, "r", encoding="utf-8-sig", newline="") as f:
        reader = csv.reader(f)
        all_rows = list(reader)

    if len(all_rows) < 2:
        print("CSV contains no data.")
        return

    category_header = all_rows[0]
    column_header = all_rows[1]
    data_rows = all_rows[2:]

    # Collect all repository names from column A (non-empty only)
    repositories = sorted({
        row[COL_REPOSITORY].strip()
        for row in data_rows
        if len(row) > COL_REPOSITORY and row[COL_REPOSITORY].strip()
    })

    metadata_by_repo = load_all_metadata(repositories)

    # Split rows into commit groups.
    # A new group starts whenever column B (Commit) starts with "http".
    groups = []  # list of lists of (row_index_in_data_rows, row)
    current_group = None
    current_repo = None

    for idx, row in enumerate(data_rows):
        # Skip rows with no content at all (e.g. trailing empty lines)
        if not any(cell.strip() for cell in row):
            continue

        repo = row[COL_REPOSITORY].strip() if len(row) > COL_REPOSITORY else ""
        commit_field = row[COL_COMMIT].strip() if len(row) > COL_COMMIT else ""

        is_new_commit = commit_field.startswith("http")

        if is_new_commit or current_group is None:
            current_group = []
            groups.append((repo if repo else current_repo, current_group))
            if repo:
                current_repo = repo

        current_group.append((idx, row))

    # Process the rows of each group
    for repo, group in groups:
        if not group:
            continue

        first_row = group[0][1]
        commit_field = first_row[COL_COMMIT].strip() if len(first_row) > COL_COMMIT else ""
        url_match = COMMIT_URL_RE.search(commit_field)
        if not url_match:
            continue
        commit_hash = url_match.group(1)
        commit_link_for_log = commit_field

        metadata_list = metadata_by_repo.get(repo, {}).get(commit_hash, [])

        for pos_in_group, (row_idx, row) in enumerate(group):
            if len(row) <= COL_EDIT_CLASSIFICATION:
                row.extend([""] * (COL_EDIT_CLASSIFICATION + 1 - len(row)))

            wrongly_included = row_is_wrongly_included(row)
            if wrongly_included:
                continue

            values_in_row = collect_row_values(row)
            if not values_in_row:
                continue

            max_val = max(values_in_row)

            if pos_in_group >= len(metadata_list):
                if not wrongly_included:
                    warnings.append(
                        f"[WARNING] Commit {commit_link_for_log} (Repo: {repo}): "
                        f"No metadata.txt file entry found at position {pos_in_group} "
                        f"(only {len(metadata_list)} entries available)."
                    )
                continue

            meta_filename, meta_classes = metadata_list[pos_in_group]

            interesting_files_field = (
                row[COL_INTERESTING_FILES].strip() if len(row) > COL_INTERESTING_FILES else ""
            )
            if pos_in_group == 0 and interesting_files_field and not wrongly_included:
                first_listed_file = interesting_files_field.split()[0]
                if os.path.basename(first_listed_file) != os.path.basename(meta_filename):
                    warnings.append(
                        f"[WARNING] Commit {commit_link_for_log} (Repo: {repo}): "
                        f"Filename in CSV ('{first_listed_file}') does not match "
                        f"metadata.txt ('{meta_filename}') at position {pos_in_group}."
                    )

            ordered_unique = ordered_unique_classes(meta_classes)

            if max_val <= len(ordered_unique):
                # Normal case: enough distinct classes available, map normally.
                mapping_string = build_mapping_string(values_in_row, ordered_unique)
                row[COL_EDIT_CLASSIFICATION] = mapping_string

            elif len(ordered_unique) == 1 and max_val <= len(meta_classes):
                # Special case: all raw entries are the same class (e.g.
                # "Generalization,Generalization"), but the CSV marks several numbers
                # (e.g. 1 and 2). As long as the highest number does not exceed the
                # number of raw entries, this is not an error -> every number is
                # mapped to the one (single) class.
                mapping_string = build_mapping_string(
                    values_in_row, ordered_unique, single_class_name=ordered_unique[0]
                )
                row[COL_EDIT_CLASSIFICATION] = mapping_string

            else:
                # Genuine contradiction: either there are several distinct classes and
                # the number still exceeds their count, or there is only one class but
                # the number even exceeds the number of raw entries.
                warnings.append(
                    f"[WARNING] Commit {commit_link_for_log} (Repo: {repo}), "
                    f"File '{meta_filename}': highest number in CSV ({max_val}) is "
                    f"greater than the number of unique edit classes in metadata.txt "
                    f"({len(ordered_unique)} -> {ordered_unique}, "
                    f"raw entries: {len(meta_classes)} -> {meta_classes})."
                )
                continue

    # Write the output CSV
    os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)
    with open(OUTPUT_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(category_header)
        writer.writerow(column_header)
        writer.writerows(data_rows)

    # Write the warnings
    os.makedirs(os.path.dirname(WARNING_OUTPUT), exist_ok=True)
    with open(WARNING_OUTPUT, "w", encoding="utf-8") as f:
        for w in warnings:
            f.write(w + "\n")

    print(f"Done. {len(warnings)} warnings written to {WARNING_OUTPUT}")
    print(f"Result written to {OUTPUT_CSV}")


if __name__ == "__main__":
    main()
