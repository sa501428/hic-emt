# Hi-C EMT

## Hi-C Extraction and Manipulation Toolkit

Tools for extracting data from `.hic` files to build new compact files that use the latest file version. Files can be
subsampled. Regions from different files may also be stitched together.

### Excise

```
excise [-r resolution] [-c chromosomes] [--seed random_seed] [--subsample num_contacts] 
       [--cleanup] <file> <out_folder>
```

The required arguments are:

* `<file>` URL or local path to `.hic` file from which data is to be extracted.
* `<out_folder>` Local folder in which to save temporary and final files.

The optional arguments are:

* `-r <int>` specifies highest resolution at which data will be extracted. Default: `1000`.
* `-c <String(s)>` specifies chromosome(s) which will be extracted. Default: all chromosomes.
* `--seed <long>` fixes random seed for PRNG in random subsampling. Default: `0`.
* `--subsample <long>` number of Hi-C contacts to approximately retain when subsampling file. Default: no subsampling.
* `--cleanup` delete temporary files (e.g. merged_no_dups) at the end. Default: keep all files.

### Stitch

```
stitch [-r resolution] [-k NONE/VC/VC_SQRT/KR/SCALE] [--reset-origin] [--cleanup]
       <file1,file2,...> <name1,name2,...> <chr1:x1:y1,chr2:x2:y2,...> <out_folder>
```

The required arguments are:

* `<file1,file2,...>` comma-separated list of `.hic` files from which data is to be extracted.
* `<name1,name2,...>` comma-separated list of short names/stems corresponding to the `.hic` files.
* `<chr1:x1:y1,chr2:x2:y2,...>` comma-separated list of regions from which to extract data.
* `<out_folder>` Local folder in which to save temporary and final files.

The optional arguments are:

* `-r <int>` specifies highest resolution at which data will be extracted. Default: `1000`.
* `[-k NONE/VC/VC_SQRT/KR/SCALE]` specific normalization type to extract for the data. Default: `SCALE`.
* `[--reset-origin]` set the origin of each region at its relative start, instead of absolute coordinates. Default: use
  absolute coordinates.
* `[--cleanup]` delete temporary files (e.g. merged_no_dups) at the end. Default: keep all files.
