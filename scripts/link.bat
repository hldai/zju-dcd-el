set dataset=LDC2015E103
::set dataset=LDC2015E75
::set dataset=LDC2016E63
set mentions_tag=0

set datadir=e:/data/edl

set info_file=%datadir%/%dataset%/output/cmn-tfidf-sys-%mentions_tag%.bin
::set mentions_file=%datadir%/%dataset%/output/all-mentions-%mentions_tag%.tab
set mentions_file=%datadir%/%dataset%/output/all-mentions-nnom-%mentions_tag%-exp.tab

::output_file=/home/dhl/data/EDL/$dataset/output/sys-link-gm.tab
set output_file=%datadir%/%dataset%/output/sys-link-sm-%mentions_tag%.tab

set bin_dir=../out/production/zju-dcd-el
set apache_cli=e:/lib/commons-cli-1.3.1/commons-cli-1.3.1.jar
set classpath=%bin_dir%;%apache_cli%

java -cp %classpath% edu.zju.edl.LinkMain -f ^
     -res /media/dhl/Data/data/el/res/ ^
     -info %info_file% ^
     -mentions %mentions_file% ^
     -o %output_file%
