import os
import glob
import sys
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

plt.rcParams['font.family'] = 'Times New Roman'
plt.rcParams['font.size'] = 12

plt.rcParams['mathtext.fontset'] = 'custom'
plt.rcParams['mathtext.rm'] = 'Times New Roman'
plt.rcParams['mathtext.it'] = 'Times New Roman:italic'
plt.rcParams['mathtext.bf'] = 'Times New Roman:bold'
MARKER_MAP = {
	'THUFI': '^',
	'GUMM': 's',
	'TTFE': 'o'
}

ALGO_ALPHA_RULES = {
	'THUFI': [0.5],
	'GUMM': [0.5],
	'TTFE': [0.0, 0.25, 0.5, 0.75, 1.0]
}

def time_ns_to_sec(ns):
	return ns / 1e9

def process_csv(csv_path):
	df = pd.read_csv(csv_path)

	algo_col = 'Algorithm'
	alpha_col = '$\\alpha$'
	k_col = '$k$'
	time_col = 'Time consumption (ns)'

	required_cols = [algo_col, alpha_col, k_col, time_col]
	for col in required_cols:
		if col not in df.columns:
			print(f"Skipping {csv_path}: Column {col} is missing. ")
			return

	df[alpha_col] = pd.to_numeric(df[alpha_col], errors='coerce')
	df[k_col] = pd.to_numeric(df[k_col], errors='coerce')
	df[time_col] = pd.to_numeric(df[time_col], errors='coerce')

	df = df.dropna(subset=[alpha_col, k_col, time_col])

	mask = pd.Series(False, index=df.index)
	for algo, alphas in ALGO_ALPHA_RULES.items():
		mask |= (df[algo_col] == algo) & (df[alpha_col].isin(alphas))
	df_filtered = df[mask].copy()

	if df_filtered.empty:
		print(f"Warning: No matching data found in {csv_path}. Skip generating image. ")
		return

	df_filtered['ln_k'] = np.log(df_filtered[k_col])
	df_filtered['time_sec'] = time_ns_to_sec(df_filtered[time_col])

	groups = df_filtered.groupby([algo_col, alpha_col])

	fig, ax = plt.subplots(figsize=(8, 6))

	colors = plt.cm.tab10(np.linspace(0, 1, len(groups)))

	for (algo, alpha), group in groups:
		group_sorted = group.sort_values(k_col)
		x = group_sorted['ln_k']
		y = group_sorted['time_sec']

		marker = MARKER_MAP.get(algo, '.')

		ax.plot(x, y,
				marker=marker,
				linestyle='-',
				linewidth=1.5,
				markersize=6,
				label=f'{algo} ($\\alpha$={alpha})')

	ax.set_xlabel(r'$\ln k$', fontsize=14)
	ax.set_ylabel('Time (s)', fontsize=14)

	ax.legend(fontsize=10, frameon=True, loc='best')

	# ax.grid(True, linestyle='--', alpha=0.6)

	fig.tight_layout()

	pdf_path = os.path.splitext(csv_path)[0] + '.pdf'
	plt.savefig(pdf_path, format='pdf', bbox_inches='tight')
	plt.close(fig)

	print(f"Generated: {pdf_path}")

def main():
	csv_files = []
	for root, directoryNames, fileNames in os.walk("."):
		for fileName in fileNames:
			if os.path.splitext(fileName)[1].lower() == ".csv":
				csv_files.append(os.path.join(root, fileName))
	if not csv_files:
		print("There is no .csv file in the current folder. ")
		return

	for csv_file in csv_files:
		print(f"Processing: {csv_file}. ")
		process_csv(csv_file)

if __name__ == '__main__':
	main()