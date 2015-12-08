# library paths configured for Ubuntu server 15.10 x64. Use `source` to load this file.
export MATLABROOT=/usr/local/MATLAB/MATLAB_Runtime/v90/
export LD_LIBRARY_PATH=$MATLABROOT/runtime/glnxa64/:$MATLABROOT/sys/os/glnxa64:$MATLABROOT/bin/glnxa64:$MATLABROOT/extern/lib/glnxa64:$MATLABROOT/sys/java/jre/glnxa64/jre1.5.0/lib/amd64/native_threads:$MATLABROOT/sys/java/jre/glnxa64/jre1.5.0/lib/amd64/server:$MATLABROOT/sys/java/jre/glnxa64/jre1.5.0/lib/amd64:$LD_LIBRARY_PATH
export XAPPLRESDIR=$MATLABROOT/X11/app-defaults 

