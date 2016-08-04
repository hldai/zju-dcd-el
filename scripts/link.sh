#!/usr/bin/env bash
dataset=LDC2015E103
#dataset=LDC2015E75

#feat_file=/home/dhl/data/EDL/$dataset/output/cmn-tfidf.bin
feat_file=/home/dhl/data/EDL/$dataset/output/cmn-tfidf-sys.bin

#mentions_file=/home/dhl/data/EDL/$dataset/data/gold-eng-mentions.tab
mentions_file=/home/dhl/data/EDL/$dataset/output/all-mentions.tab

#output_file=/home/dhl/data/EDL/$dataset/output/sys-link-gm.tab
output_file=/home/dhl/data/EDL/$dataset/output/sys-link-sm.tab

java -cp ../out/production/zju-dcd-el/ edu.zju.dcd.edl.LinkMain -f \
     -res /media/dhl/Data/data/el/res/ \
     -feat $feat_file \
     -mentions $mentions_file \
     -o $output_file
