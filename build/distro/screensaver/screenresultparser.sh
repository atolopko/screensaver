# usage: screenresultparser.bat <metadata file> [<wells to print #>] [-ignorefilepaths]
./run.sh edu.harvard.med.screensaver.io.ScreenResultParser -metadatafile $1 -wellstoprint ${2:-10} $3