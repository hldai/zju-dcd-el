#!/usr/bin/env bash
dataset=LDC2015E103
#dataset=LDC2015E75

bin_dir=../out/production/zju-dcd-el
stanford_corenlp=/media/dhl/Data/lib/stanford-nlp/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar

mentions_file=/home/dhl/data/EDL/$dataset/output/all-mentions-tac.txt
#mentions_file=/home/dhl/data/EDL/$dataset/data/gold-eng-mentions.tab

output_file=/home/dhl/data/EDL/$dataset/output/cmn-tfidf-sys.bin
#output_file=/home/dhl/data/EDL/$dataset/output/cmn-tfidf.bin

classpath=$bin_dir:$stanford_corenlp

java -Xmx10g -cp $classpath edu.zju.dcd.edl.PrepareMain \
     -res /media/dhl/Data/data/el/res/ \
     -mentions $mentions_file \
     -dl /home/dhl/data/EDL/$dataset/data/eng-docs-list.txt \
     -o $output_file