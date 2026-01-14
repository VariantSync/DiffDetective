import re
import csv
import os

# ====== Einstellungen ======
base_folder = "../results/uncertainty/current"  # Hauptordner, der alle Unterordner enthält
input_filename = "totalresult.metadata.txt"  # Dateiname in jedem Unterordner
# ===========================

def process_file(txt_path, csv_path):
    with open(txt_path, "r", encoding="utf-8") as f:
        text = f.read()

    # Suche Startposition (erstes https)
    start = text.find("https")
    if start == -1:
        print(f"⚠️  '{txt_path}' enthält kein 'https' – übersprungen.")
        return

    # Suche Endposition (erstes #Error oder EOF)
    end = text.find("#Error")
    if end == -1:
        end = len(text)  # Falls kein #Error, bis zum Ende der Datei

    text_section = text[start:end]

    lines = [line.strip() for line in text_section.splitlines() if line.strip()]

    with open(csv_path, "w", newline="", encoding="utf-8") as csvfile:
        writer = csv.writer(csvfile, delimiter=";")
        writer.writerow(["Commit", "Interesting Files", "Number uninteresting changes", "Number interesting changes", "Year of Commit"])

        for line in lines:
            # Zeilen überspringen, die mit "filtered" oder "runtime" beginnen
            if line.lower().startswith("filtered") or line.lower().startswith("runtime"):
                continue

            match = re.search(
                r'^(https?://.+?):\s*(.*?)\s*Number uninteresting Patches:\s*(\d+)\s*Number interesting Patches:\s*(\d+)\s*Year:\s*(\d{4})',
                line,
                re.IGNORECASE
            )

            if match:
                link = match.group(1)
                filenames = " ".join(match.group(2).split())  # Leerzeichen zwischen Dateinamen behalten
                num_uninteresting = match.group(3)
                num_interesting = match.group(4)
                year = match.group(5)

                writer.writerow([link, filenames, num_uninteresting, num_interesting, year])

    print(f"✅ '{csv_path}' wurde erstellt.")


# ====== Hauptlogik ======
if not os.path.isdir(base_folder):
    raise FileNotFoundError(f"Ordner '{base_folder}' existiert nicht!")

# Alle Unterordner durchgehen
for subdir, dirs, files in os.walk(base_folder):
    if input_filename in files:
        txt_path = os.path.join(subdir, input_filename)
        folder_name = os.path.basename(subdir)
        csv_path = os.path.join(subdir, f"{folder_name}.csv")

        process_file(txt_path, csv_path)

print("\n🎉 Alle Dateien wurden erfolgreich verarbeitet!")
