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

Eight famous datasets are included. The dataset name, transaction count, different event count, average transaction length, 
transaction length range, average TTF, TTF range, and density for each dataset are shown as follows. 

| Dataset | Transaction count | Different event count | Average transaction length | Transaction length range | Average TTF | TTF range | Dataset Density |
| - | - | - | - | - | - | - | - |
| accidents | $340183$ | $468$ | $11500870 / 340183 \approx 33.81$ | $51 - 18 = 33$ | $89460719 / 340183 \approx 262.98$ | $636.5 - (-45.0) = 681.5$ | 7.2239% |
| chess | $3196$ | $75$ | $118252 / 3196 = 37$ | $37 - 37 = 0$ | $868004 / 3196 \approx 271.59$ | $529.0 - 44.0 = 485.0$ | 49.3333% |
| connect | $67557$ | $129$ | $2904951 / 67557 = 43$ | $43 - 43 = 0$ | $68242801.5 / 67557 \approx 1010.15$ | $1565.5 - 428.0 = 1137.5$ | 33.3333% |
| kosarak | $837206$ | $41001$ | $7866192 / 837206 \approx 9.40$ | $2497 - 2 = 2495$ | $70343495 / 837206 \approx 84.02$ | $21931.0 - (-115.0) = 22046.0$ | 0.0229% |
| mushroom | $8124$ | $119$ | $186852 / 8124 = 23$ | $23 - 23 = 0$ | $2139568 / 8124  \approx 263.36$ | $471.5 - 28.5 = 443.0$ | 19.3277% |
| pumsb | $30417$ | $1964$ | $2250858 / 30417 = 74$ | $74 - 74 = 0$ | $52904220 / 30417 \approx 1739.30$ | $2405.0 - 1094.5 = 1310.5$ | 3.7678% |
| pumsb_star | $30417$ | $1939$ | $1523930 / 30417 \approx 50.10$ | $62 - 49 = 13$ | $35849254 / 30417 \approx 1178.59$ | $1817.0 - 651.0 = 1166.0$ |2.5839% |
| retail | $87985$ | $16459$ | $904955 / 87985 \approx 10.29$ | $76 - 1 = 75$ | $21238619 / 87985 \approx 241.39$ | $2174.0 - (-42.5) = 2216.5$ | 0.0625% |

The delta ratio is designed for the static threshold algorithm, GUMM. The default delta ratio is $0.7$. 
If Out-Of-Memory (OOM) errors occur, the delta ratio can be increased to $0.9$ with a step of $0.05$ until no OOM errors occur. 
Based on our experience, it is recommended that some datasets be cropped to a specified ratio to allow successful testing via GitHub Actions. 
The recommended ratios and post-cropping details for each dataset are shown below. 

| Dataset | Delta ratio | Cropping ratio | Transaction count | Different event count | Average transaction length | Transaction length range | Average TTF | TTF range | Dataset Density |
| - | - | - | - | - | - | - | - | - | - |
| accidents | $0.8$ | $0.2$ | $68036$ | $368$ | $2307783 / 68036 \approx 33.92$ | $48 - 20 = 28$ | $18845555.5 / 68036 \approx 276.99$ | $568.5 - (-2.5) = 571.0$ | 9.2174% |
| connect | $0.9$ | $0.02$ | $1351$ | $95$ | $58093 / 1351 = 43$ | $43 - 43 = 0$ | $1368163 / 1351 \approx 1012.70$ | $1477.0 - 566.5  = 910.5$ | 45.2632% |
| kosarak | $0.85$ | $0.00002$ | $16$ | $64$ | $92 / 16 = 5.75$ | $19 - 2 = 17$ | $873.5 / 16 \approx 54.59$ | $174.5 - (-12.0) = 186.5$ | 8.9844% |
| pumsb | $0.9$ | $0.0001$ | $3$ | $110$ | $222 / 3 = 74$ | $74 -74 = 0$ | $5623 / 3 \approx 1874.33$ | $2075.0 -1744.0 = 331.0$ | 67.2727% |
| pumsb_star | $0.9$ | $0.002$ | $60$ | $342$ | $3008 / 60 \approx 50.13$ | $55 - 49 = 6$ | $69846 / 60 = 1164.1$ | $1493.5 - 889.0 = 604.5$ | 14.6589% |
| retail | $0.8$ | $0.002$ | $175$ | $844$ | $1381 / 175 \approx 7.89$ | $28 - 1 = 27$ | $33309.5 / 175 \approx 190.34$ | $763.5 - (-22.0) = 785.5$ | 0.9350% |

Example datasets like ``sample.txt`` used for algorithm debugging, testing, and tracking are also proposed. 
A Python script entitled ``datasetExtender.py`` is designed to extend multiple 1-dimensional datasets to one multi-dimensional dataset. 

## Literature

Selected literature corresponding to different famous top-$k$ mining algorithms is collected here. 
