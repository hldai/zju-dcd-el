#!/usr/bin/env bash
dataset=LDC2015E103
#dataset=LDC2015E75

bin_dir=../out/production/zju-dcd-el
stanford_corenlp=/media/dhl/Data/lib/stanford-nlp/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar

classpath=$bin_dir:$stanford_corenlp

java -cp $classpath edu.zju.dcd.edl.PrepareMain \
     -res /media/dhl/Data/data/el/res/ \
     -mentions /home/dhl/data/EDL/$dataset/data/gold-eng-mentions.tab \
     -dd /home/dhl/data/EDL/$dataset/data/eng-docs-all \
     -o /home/dhl/data/EDL/$dataset/output/cmn-tfidf.bin