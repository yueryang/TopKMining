# TopKMining

This repository serves as a systematic collection of multiple top-$k$ mining algorithms. 

The structure of this repository is as follows. 

- ``TopKMining.java``
- Algorithms
  - THUI
  - THUFI
  - TFUI
  - TTFE
    - TTFE_v1
    - TTFE_v2
    - TTFE_v3
    - TTFE_v4
    - TTFE_v5
- Datasets
  - ...
- Literature
  - ...

## ``TopKMining.java``

This is the official implementation of the TTFE algorithm, along with its baselines. 

Except for the parameter loops, users can control behaviors via the command-line arguments (e.g., the dataset, the output file path, and the run count). 
Regarding the command-line arguments, please execute ``java TopKMining.java -h`` after navigating (``cd`` or ``cd /d``) into the root directory of this repository. 

This Java implementation is designed for experimental purposes. No mined top-$k$ patterns and values will be saved or displayed by default. 
If it is necessary to save or display the mined top-$k$ patterns and values, please adjust the code manually or use the directory entitled ``Algorithms``. 
For teaching purposes, please execute the algorithms under the directory entitled ``Algorithms`` according to the following statements. 

## Algorithms

Most of the top-$k$ mining algorithms are implemented via the Java programming language. 

**Warning: Please always use ``TopKMining.java`` instead of any of the files under this directory for experimental purposes if the algorithm is supported in ``TopKMining.java``.** 
**For historical versions, all the algorithms under this directory will no longer be maintained.** 

### THUI

This is a possible extensive implementation of the original THUI algorithm, abstracted from the SPMF. 
This implementation can output accurate results without an additional pruning strategy or fuzzy results with the additional pruning strategy. 
Multiple layers of loops are set up for better experiment implementation. 
As the classic representative of most famous traditional top-$k$ mining algorithms, THUI, as well as most of the famous traditional top-$k$ mining algorithms, can only process 1-dimensional datasets. 

### THUFI

This is the historical official implementation of mining top-$k$ high threat and frequency itemsets based on the original THUI. 
THUFI will first compute the top-$k$ high threat itemsets and top-$k$ high frequency itemsets, respectively. 
Subsequently, the two results will be merged to form the final results. 
This is not an accurate algorithm since it does not consider the two dimensions at the same time. 
However, when one tries to compare the TTFE or the TFUI algorithm with the THUI algorithm, THUFI should be used instead of the THUI algorithm since the latter does not support 2-dimensional datasets. 

### TFUI

This is an improved implementation of the THUFI algorithm with file configurations. 
It supports parsing parameter values (e.g., $\alpha$ and $\beta$) directly from the dataset file. 
TFUI is an algorithm from another academic paper that is currently unpublished. 

### TTFE

These are the historical official implementations for mining top-$k$ high threat and frequency event sets. 
They support super-parameters directly set in the dataset file, use better data structures and sorting algorithms, and have more friendly debugging procedures. 
For teaching purposes, users should enable the debugging mode and disable the experimental mode. 
For experimental purposes, users are highly recommended to use ``TopKMining.java`` under the root directory of this repository. 
Otherwise, please disable the debugging mode and enable the experimental mode before executing the experiments. 

#### TTFE_v1

This is an accurate algorithm without tree construction procedures. 
It will also compute the top-$k$ event sets in each transaction. 

#### TTFE_v2

This is an accurate algorithm with tree construction procedures. 
It has better performance due to node pruning. 

#### TTFE_v3

More switches are set. Users can try to run TTFE with different combinations of switches. 
More experimental options and file operations are provided. 

#### TTFE_v4

Extended experiments are merged. More systematic debugging outputs are provided. 
**Data should be cropped according to a fixed ratio if it is hard to test GUMM due to the limitation of computing memory. **

#### TTFE_v5 (soft link)

This is the final version of the official implementation for mining top-$k$ high threat and frequency event sets, which points to the ``TopKMining.java`` under the root directory of this repository. 
OOM issues have been greatly reduced in this version. 
For experimental purposes, the feature of parsing parameter values from the dataset file has been removed. 
Therefore, please use ``TopKMining.java`` under the root directory of this repository for experimental purposes, 
and TTFE_v4 with the debugging mode enabled and the experimental mode disabled for teaching purposes. 

All future updates will be directly merged into this file instead of creating a new version (e.g., ``v6``). 

### ``spmf.zip``

This a set of algorithms downloaded from the [SPMF](https://www.philippe-fournier-viger.com/spmf/index.php?link=download.php) platform. 

## Datasets

Eight famous datasets are included. The dataset name, the transaction count, the overall event count, the average event count, 
the overall TTF, the average TTF, the maximum TTF, and the minimum TTF for each dataset are shown as follows. 

| Dataset | Transaction count | Average event count | Event count range | Average TTF | TTF range |
| - | - | - | - | - | - |
| accidents | $340183$ | $33.81$ | $51 - 18 = 33$ | $262.98$ | $636.5 - (-45.0) = 681.5$ |
| chess | $3196$ | $37.00$ | $37 - 37 = 0$ | $271.59$ | $529.0 - 44.0 = 485.0$ |
| connect | $67557$ | $43.00$ | $43 - 43 = 0$ | $1010.15$ | $1565.5 - 428.0 = 1137.5$ |
| kosarak | $837206$ | $8.40$ | $2497 - 2 = 2495$ | $84.02$ | $21931.0 - (-115.0) = 22046.0$ |
| mushroom | $8124$ | $23.00$ | $23 - 23 = 0$ | $263.36$ | $471.5 - 28.5 = 443.0$ |
| pumsb | $30417$ | $74.00$ | $74 - 74 = 0$ | $1739.30$ | $2405.0 - 1094.5 = 1310.5$ |
| pumsb_star | $30417$ | $50.10$ | $62 - 49 = 13$ | $2254.46$ | $3388.0 - 1541.0 = 1847.0$ |
| retail | $87985$ | $10.29$ | $76 - 1 = 75$ | $241.39$ | $2174.0 - (-42.5) = 2216.5$ |

According to our experience, some datasets are recommended to be cropped according to a ratio. 
Related information after being cropped is provided as follows. 

| Dataset | Ratio | Transaction count | Average event count | Event count range | Average TTF | TTF range |
| - | - | - | - | - | - | - |
| accidents | $0.01$ | $3401$ | $33.84$ | $45 - 24 = 21$ | $273.42$ | $543.0 - 11.5 = 531.5$ |
| kosarak | $10$ | $10$ | $3.6$ | $8 - 2 = 6$ | $36.5$ | $154.5 - (-12.0) = 166.5$ |
| pumsb | $0.001$ | $30$ | $74.0$ | $74 - 74 = 0$ | $1742.73$ | $2086.0 - 1441.5 = 644.5$ |
| pumsb_star | $0.01$ | $304$ | $49.91$ | $55 - 49 = 6$ | $2242.06$ | $2757.0 - 1789.0 = 968.0$ |
| retail | $0.01$ | $879$ | $8.55$ | $52 - 1 = 51$ | $199.38$ | $1054.5 - (-22.0) = 1076.5$ |

Example datasets like ``sample.txt`` used for algorithm debugging, testing, and tracking are also proposed. 
A Python script entitled ``datasetExtender.py`` is designed to extend multiple 1-dimensional datasets to one multi-dimensional dataset. 

## Literature

Selected literature corresponding to different famous top-$k$ mining algorithms is collected here. 
