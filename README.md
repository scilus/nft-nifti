# nft-nifti

nf-test plugin to provide support for NIFTI files. Based on the [niftijio library](https://github.com/amirshamaei/niftijio)

## CheckSum MD5 for NIFTI files

```
assert snapshot(Nifti_md5sum(process.out.nifti.get(0))).match()
```
