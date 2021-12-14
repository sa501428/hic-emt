# Hi-C EMT

## Hi-C Extraction and Manipulation Toolkit

Tools for extracting data from `.hic` files to build new compact files that use the latest file version. Files can be
subsampled. Regions from different files may also be stitched together.
* Note: because files will be built to use the latest v9 .hic format, you will need to make sure whatever downstream tool you are using
  is capable of reading v9 .hic files. The latest Juicer Tools jar, Straw, and Juicebox Jar (under Github Releases) are capable of doing so.
  If you are unsure whether your downstream software can read v9 files, feel free to create a Github issue to ask in this repo.

## Excise

### Usage

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

### Example

To subsample a map with a depth of ~5 million Hi-C contacts which goes down to 25kB resolution from `GM12878_30.hic`,
use:

```
java -Xmx5g -jar hic_emt.jar excise -r 25000 --subsample 5000000 /Desktop/files/GM12878_30.hic gm_file_5M
```

To only subsample the first 3 chromosomes at this approximate depth (i.e. ~5 million contacts genomewide but ~600,000
contacts when only filtering for first 3 chromosomes.):

```
java -Xmx5g -jar hic_emt.jar excise -r 25000 -c 1,2,3 --subsample 5000000 /Desktop/files/GM12878_30.hic gm_file_5M
```

## Stitch

### Usage

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

### Example

To grab a subset of KR normalized reads from chromosomes 1, 2, and 3 from three `.hic` files and put them in one file:

```
java -Xmx5g -jar hic_emt.jar stitch -r 25000 -k KR GM12878.hic,K562.hic,Hap1.hic GM,K562,Hap1 
                 1:100000000:110005000,2:115000000:125000000,3:80010000:90005000 results
```

