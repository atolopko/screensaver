# usage: import-legacy-screen-result.sh <input file> <screen number>[<wells to print #>]
./run.sh edu.harvard.med.screensaver.io.screenresults.ScreenResultParser --input-file $1 --screen $2 --wells ${2:-10} --ignore-file-paths --legacy-format --import