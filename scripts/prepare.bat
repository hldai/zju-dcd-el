set dataset=LDC2015E103
::set dataset=LDC2015E75
::set dataset=LDC2016E63

set datadir=e:/data/edl

set mentions_file=%datadir%/%dataset%/output/all-mentions.tab
:: mentions_file=/home/dhl/data/EDL/$dataset/data/gold-eng-mentions.tab

set output_file=%datadir%/%dataset%/output/cmn-tfidf-sys.bin
:: output_file=/home/dhl/data/EDL/$dataset/output/cmn-tfidf.bin

set bin_dir=../out/production/zju-dcd-el
set stanford_corenlp=e:/lib/stanford-nlp/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar
set apache_cli=e:/lib/commons-cli-1.3.1/commons-cli-1.3.1.jar
set classpath=%bin_dir%;%stanford_corenlp%;%apache_cli%

java -Xmx10g -cp %classpath% edu.zju.edl.PrepareMain ^
     -res %datadir%/res/ ^
     -mentions %mentions_file% ^
     -dl %datadir%/%dataset%/data/eng-docs-list-win.txt ^
     -o %output_file%