#/bin/bash
var="python3 solution.py $@"
eval $var | sed -n '1p ; $p' 
# remove "| sed ..." for the full output with interchaged messages"
