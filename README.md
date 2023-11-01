# pileupReformater
In this tool, the output of samtools pileup is to be converted into a matrix. The input should be a pileup file that was created using samtools and based on a binary map file (BAM). If pileup has been restricted to certain positions, the file is converted into a matrix after the transformation using pileup reformatter. The columns represent the individual positions. The rows contain the reads or their names. Values in the matrix are the nucleotides that were detected. The matrix can be used to identify which nucleotides occur in the individual positions of the reads. This is useful for linking single nucleotide varints by individual reads.

## Installation


## Run pileupReformater

