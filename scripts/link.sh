#!/usr/bin/env bash
dataset=LDC2015E103
#dataset=LDC2015E75

java -cp ../out/production/zju-dcd-el/ edu.zju.dcd.edl.LinkMain -f \
     -res /media/dhl/Data/data/el/res/ \
     -feat /home/dhl/data/EDL/$dataset/output/cmn-tfidf-new.bin \
     -mentions /home/dhl/data/EDL/$dataset/data/gold-eng-mentions.tab \
     -o /home/dhl/data/EDL/$dataset/output/sys-link-gm-new.tab
