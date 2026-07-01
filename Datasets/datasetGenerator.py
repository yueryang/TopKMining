import os
import ssl
import zipfile
import urllib.request
import numpy as np
from skmine.datasets.fimi import fetch_file

class DatasetSynthesizer:
	"""
	A class to automatically download, extract, and synthesize FIMI datasets 
	strictly inside the directory where this script resides.
	"""
	
	# Class attribute: Library containing ALL 7 valid datasets from the FIMI repository
	DATASET_LIBRARY = {
		"1": {"name": "chess", "file": "chess.dat", "is_zip": False},
		"2": {"name": "connect", "file": "connect.dat", "is_zip": False},
		"3": {"name": "mushroom", "file": "mushroom.dat", "is_zip": False},
		"4": {"name": "pumsb", "file": "pumsb.dat", "is_zip": False},
		"5": {"name": "pumsb_star", "file": "pumsb_star.dat", "is_zip": False},
		"6": {"name": "accidents", "file": "accidents.dat", "is_zip": True},
		"7": {"name": "retail", "file": "retail.dat", "is_zip": False}
	}

	def __init__(self, seed=42):
		"""
		Initialize the synthesizer and lock the target directory to the script's folder.
		"""
		self.seed = seed
		np.random.seed(self.seed)
		
		# FIX: Dynamically find the absolute path of the directory containing THIS script
		self.script_dir = os.path.dirname(os.path.abspath(__file__))

	def _download_and_extract(self, name, local_dat_file, is_zip):
		"""
		Internal method to handle downloading and unzipping datasets securely.
		"""
		# Force paths to be absolute, rooted in the script's directory
		target_dat_path = os.path.join(self.script_dir, local_dat_file)
		download_target_name = f"{name}.zip" if is_zip else local_dat_file
		download_target_path = os.path.join(self.script_dir, download_target_name)
		
		dataset_url = f"https://fimi.uantwerpen.be/data/{download_target_name}"

		if not os.path.exists(target_dat_path):
			print(f"\nFile '{local_dat_file}' not found. Downloading from {dataset_url}...")
			try:
				headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
				req = urllib.request.Request(dataset_url, headers=headers)
				context = ssl._create_unverified_context()
				
				# Download directly into the script's directory
				with urllib.request.urlopen(req, context=context) as response, open(download_target_path, 'wb') as out_file:
					out_file.write(response.read())
				print(f"Download completed: {download_target_path}")
				
				# Extract if it's a ZIP archive
				if is_zip:
					print(f"Extracting '{download_target_name}'...")
					with zipfile.ZipFile(download_target_path, 'r') as zip_ref:
						# Extract directly into the script's directory
						zip_ref.extractall(self.script_dir)
					os.remove(download_target_path)  # Clean up the zip file
					print(f"Extracted to: {target_dat_path}")
					
				return True
			except Exception as e:
				print(f"Automatic download or extraction failed for {name}. Error: {e}")
				return False
		else:
			return True

	def load_dataset_series(self, db_info):
		"""
		Ensures the file exists in the script folder and loads it via skmine.
		"""
		local_dat_file = db_info["file"]
		target_dat_path = os.path.join(self.script_dir, local_dat_file)

		# Ensure the data file is downloaded/present in the script directory
		if not self._download_and_extract(db_info["name"], local_dat_file, db_info["is_zip"]):
			return None

		# Read the local FIMI file using its bound path
		try:
			series_data = fetch_file(target_dat_path)
			return series_data
		except Exception as e:
			print(f"Error reading file {target_dat_path}: {e}")
			return None

	def synthesize(self, name, series_data, max_rows, db_info):
		"""
		Loop through FIMI transactions and append Threat, Frequency, and TTF, saving locally.
		"""
		total_available_rows = len(series_data)
		
		if max_rows is None or max_rows >= total_available_rows:
			rows_to_process = total_available_rows
			sliced_data = series_data
			print(f"\n[{name.upper()}] Synthesizing ALL {rows_to_process} rows. Please wait...")
		else:
			rows_to_process = max_rows
			sliced_data = series_data[:max_rows]
			print(f"\n[{name.upper()}] Synthesizing customized {rows_to_process} out of {total_available_rows} rows. Please wait...")

		# FIX: Force output file to be generated in the script directory
		output_filename = f"{name}.txt"
		output_filepath = os.path.join(self.script_dir, output_filename)
		
		generated_lines = ["# Event : Threat : Frequency : TTF"]

		for i, transaction in enumerate(sliced_data):
			if i > 0 and i % 50000 == 0:
				print(f"  Progress: Processed {i} / {rows_to_process} rows...")
				
			event_str = " ".join(map(str, transaction))
			n_items = len(transaction)
			target_size = n_items
			
			threat_vals = np.random.randint(-50, 55, size=n_items)
			threat_str = " ".join(map(str, threat_vals))
			
			freq_vals = np.random.randint(1, 90, size=n_items)
			freq_str = " ".join(map(str, freq_vals))
			
			ttf_val = round(np.random.uniform(10.0, 600.0), 1)
			ttf_str = f"{ttf_val}"
			
			full_line = f"{event_str}:{threat_str}:{freq_str}:{ttf_str}"
			generated_lines.append(full_line)
			
		print("Writing data to file...")
		with open(output_filepath, "w", encoding="utf-8") as f:
			f.write("\n".join(generated_lines) + "\n")

		print(f"Success! {rows_to_process} records saved to: {output_filepath}")


def main():
	synthesizer = DatasetSynthesizer()
	
	while True:
		print("\n==================================================")
		print("   Multi-Dataset Automatic Synthesizer Toolkit   ")
		print("==================================================")
		print("Available compatible datasets:")
		for key, db in DatasetSynthesizer.DATASET_LIBRARY.items():
			print(f"  [{key}] {db['name']} ({db['file']})")
		print("==================================================")
		
		choice = input("Enter a dataset number to process (or 'Q' to quit): ").strip()
		
		if choice.upper() == 'Q':
			print("Exiting program. Goodbye!")
			break
			
		if choice not in DatasetSynthesizer.DATASET_LIBRARY:
			print("Invalid selection. Please enter a number between 1 and 7.")
			continue
			
		selected_db = DatasetSynthesizer.DATASET_LIBRARY[choice]
		print(f"\nYou selected: {selected_db['name'].upper()}")
		
		print("Checking local files and loading dataset...")
		series_data = synthesizer.load_dataset_series(selected_db)
		
		if series_data is None:
			print("Failed to load dataset. Returning to main menu.")
			continue
			
		max_rows_available = len(series_data)
		
		print(f"--------------------------------------------------")
		print(f"Notice: The '{selected_db['name']}' dataset has a MAXIMUM of {max_rows_available} rows.")
		size_input = input(f"Enter number of rows to generate (1-{max_rows_available}, or press Enter for FULL dataset): ").strip()
		
		max_rows = None
		if size_input:
			try:
				max_rows = int(size_input)
				if max_rows <= 0:
					print("Invalid row count. Defaulting to FULL dataset.")
					max_rows = None
			except ValueError:
				print("Invalid input format. Defaulting to FULL dataset.")
				max_rows = None
				
		# Run synthesis (passing selected_db info along)
		synthesizer.synthesize(selected_db['name'], series_data, max_rows, selected_db)
		
		print(f"--------------------------------------------------")
		continue_prompt = input("Dataset processing complete. Do you want to process another dataset? (Y/N): ").strip()
		if continue_prompt.upper() != 'Y':
			print("Exiting program. Thank you!")
			break

if __name__ == "__main__":
	main()