process COPY_NIFTI {

  input:
    path nifti

  output:
     path "output.nii.gz", emit: nifti


  """
	cp ${nifti} output.nii.gz
  """
}