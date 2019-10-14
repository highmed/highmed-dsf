#!/bin/bash

echo "Executing DSF BPE with"
java --version

trap 'kill -TERM $PID' TERM INT
java -cp lib/*:plugin/*:dsf_bpe.jar org.highmed.dsf.bpe.BpeJettyServer &
PID=$!
wait $PID
trap - TERM INT
wait $PID

JAVA_EXIT=$?
if [ $JAVA_EXIT -eq 143 ]; then
	echo java exited with code $JAVA_EXIT, converting to 0	
	exit 0
else
	echo java exited with code $JAVA_EXIT
	exit $JAVA_EXIT
fi
