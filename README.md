# TopKMining

This repository serves as a systematic collection of multiple top-$k$ mining algorithms. 

## Algorithms

Most of the top-$k$ mining algorithms are implemented via the Java programming language. 

**Warning: Please always use ``TopKMining.java`` instead of any of the files under this directory if the algorithm is supported in this file. **

### SPMF

This a set of algorithms downloaded from the [SPMF](https://www.philippe-fournier-viger.com/spmf/index.php?link=download.php) platform. 

### THUI

This is an extensive implementation of the original THUI algorithm, abstracted from the SPMF. 
This implementation can output accurate results without an additional pruning strategy or fuzzy results with the additional pruning strategy. 
Multiple layers of loops are set up for better experiment implementation. 
THUI can only process 1-dimensional datasets. 

### THUFI

This is the implementation of mining top-$k$ high threat and frequency itemsets based on the original THUI. 
THUFI will first compute the top-$k$ high threat itemsets and top-$k$ high frequency itemsets, respectively. 
Subsequently, the two results will be merged to form the final results. 
This is not an accurate algorithm since it does not consider the two dimensions at the same time. 
However, when one tries to compare the TTFE or the TFUI algorithm with the THUI algorithm, THUFI should be used instead of the THUI algorithm since the latter does not support 2-dimensional datasets. 

### TFUI

This is an improved implementation of the THUFI algorithm with file configures. 
It supports the alpha and the beta values directly set in the database file. 

### TTFE

This is the official implementation of mining top-$k$ high threat and frequency event sets. 
It supports super-parameters directly set in the database file. 
It uses better data structure and sorting algorithms. 
It has more friendly debugging procedures. 

#### TTFE_v1

This is an accurate algorithm without tree construction procedures. 
It will also compute the top-$k$ event sets in each transaction. 

#### TTFE_v2

This is an accurate algorithm with tree construction procedures. 
It has better performance due to the node pruning. 

#### TTFE_v3

More switches are set. Users can try to run TTFE with different combinations of switches. 
More experimental options and file operations are provided. 

#### TTFE_v4

Extended experiments are merged. 
**Data should be cut according to a fixed ratio if it is hard to test GUMM due to the limitation of computing memory. **

## Datasets

The following six famous datasets are included. 

- accidents
- chess
- kosarak
- mushroom
- pumsb
- retail

Example datasets used for algorithm debugging, testing, and tracking are proposed. 
A Python script entitled ``generateTTFEDatasets.py`` is designed for 2D dataset generation. 

## Literature

Selected literature corresponding to different famous top-$k$ mining algorithms is collected here. 
