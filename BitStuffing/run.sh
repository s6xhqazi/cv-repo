#/bin/bash
var=$@
if [ "$var" = "alternating" ];
then
var="python3 solution.py $@ a a"
eval $var | sed -n '1p ; $p' 
# remove "| sed ..." for the full output with interchaged messages"
else python3 solution.py $@
fi
