::set datadir=e:/data/edl/LDC2015E103
::set datadir=e:/data/edl/LDC2015E75
::set datadir=e:/data/edl/LDC2016E63
::set datadir=e:/data/el/LDC2015E19/data/2009/eval
set datadir=e:/data/el/LDC2015E19/data/2010/eval
::set datadir=e:/data/el/LDC2015E19/data/2011/eval

set mentions_tag=0

::set mentions_file=%datadir%/data/mentions-all-expansion.tab
set mentions_file=%datadir%/data/mentions.tab
::set mentions_file=%datadir%/%dataset%/output/all-mentions-%mentions_tag%.tab
::set mentions_file=%datadir%/output/all-mentions-nnom-%mentions_tag%-exp.tab
:: mentions_file=/home/dhl/data/EDL/$dataset/data/gold-eng-mentions.tab

set output_file=%datadir%/output/cmn-tfidf-sys-%mentions_tag%.bin
:: output_file=/home/dhl/data/EDL/$dataset/output/cmn-tfidf.bin

set resdir=e:/data/edl/res
set bin_dir=../out/production/zju-dcd-el
set stanford_corenlp=e:/lib/stanford-nlp/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar
set apache_cli=e:/lib/commons-cli-1.3.1/commons-cli-1.3.1.jar
set classpath=%bin_dir%;%stanford_corenlp%;%apache_cli%

java -Xmx10g -cp %classpath% edu.zju.edl.PrepareMain ^
     -res %resdir% ^
     -mentions %mentions_file% ^
     -dl %datadir%/data/eng-docs-list-win.txt ^
     -o %output_file%