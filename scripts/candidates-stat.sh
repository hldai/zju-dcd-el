#!/usr/bin/env bash
bin_dir=../out/production/zju-dcd-el
stanford_corenlp=/media/dhl/Data/lib/stanford-nlp/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar

classpath=$bin_dir:$stanford_corenlp

java -cp $classpath edu.zju.dcd.edl.CandidatesStat \
     -res /media/dhl/Data/data/el/res/ \
     -gold /home/dhl/data/EDL/LDC2015E103/data/gold-eng-nam-mentions.tab \
     -o /home/dhl/data/EDL/LDC2015E103/result/candidates-error.txt