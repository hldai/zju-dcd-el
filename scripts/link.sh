#!/usr/bin/env bash
java -cp ../out/production/zju-dcd-el/ edu.zju.dcd.edl.LinkMain -f \
     -res /media/dhl/Data/data/el/res/ \
     -feat /home/dhl/data/EDL/LDC2015E103/result/cmn-tfidf.bin \
     -mentions /home/dhl/data/EDL/LDC2015E103/data/gold-eng-mentions.tab \
     -o /home/dhl/data/EDL/LDC2015E103/result/sys-link-gm.tab
