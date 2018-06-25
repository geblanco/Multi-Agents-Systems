#!/usr/bin/env sh

work_dir=$(pwd)
log_dir="$work_dir/results"
file_path=$work_dir/data/tsp/$1
if [[ ! -f $file_path ]]; then
  file_path=$work_dir/$1
fi
jars=(
	"out/production/TSPLIB4J"
	"lib/commons-cli-1.2.jar"
	"lib/rsyntaxtextarea.jar"
	"lib/commons-codec-1.8.jar"
	"lib/commons-math3-3.4.1.jar"
	"lib/jcommon-1.0.20.jar"
	"lib/JMetal-4.3.jar"
	"lib/MOEAFramework-2.12.jar"
	"lib/jfreechart-1.0.15.jar"
)
last_jar="lib/commons-lang3-3.1.jar"
classpath=""
for i in "${jars[@]}"; do
	classpath+="$work_dir/$i:"
done
classpath+=$work_dir/$last_jar

if [[ ! -d "$log_dir" ]]; then
	mkdir -p "$log_dir"
fi

java -classpath $classpath DPSO_TSP.TSP $file_path | tee "$log_dir/$1_results.txt"
