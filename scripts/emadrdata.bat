::set dataset=LDC2015E103
::set dataset=LDC2015E75
::set dataset=LDC2016E63
set mentions_tag=0
set docvec_tag=3

set year=2011
set part=eval

::set datadir=e:/data/edl
::set datadir=e:/data/el/LDC2015E19/data/2009/eval
::set emadrdir=e:/data/emadr/el/tac/2009/eval
set datadir=e:/data/el/LDC2015E19/data/%year%/%part%
set emadrdir=e:/data/emadr/el/tac/%year%/%part%
::set datadir=e:/data/el/LDC2015E19/data/2010/eval
::set emadrdir=e:/data/emadr/el/tac/2010/eval

::set dst_file=%emadrdir%/el-%year%-%part%-%docvec_tag%.bin
set dst_file=%emadrdir%/el-%year%-%part%-name-exp-%docvec_tag%.bin
set mentions_file=%datadir%/data/mentions-all-expansion.tab

set info_file=%datadir%/output/cmn-tfidf-sys-%mentions_tag%.bin
::set info_file=%datadir%/%dataset%/output/cmn-tfidf-sys-%mentions_tag%.bin

::set mentions_file=%datadir%/%dataset%/output/all-mentions-%mentions_tag%.tab
::set mentions_file=%datadir%/%dataset%/output/all-mentions-nnom-%mentions_tag%-exp.tab

set doclist_file=%datadir%/data/eng-docs-list-win.txt
set docvecs_file=%emadrdir%/doc_vecs_%docvec_tag%.bin

set wikivecs_file=e:/data/emadr/el/vecs/wiki_dedw_vecs_%docvec_tag%.bin

::output_file=/home/dhl/data/EDL/$dataset/output/sys-link-gm.tab

set midwid_file=e:/data/edl/res/prog-gen/mid_to_wid_full_ord_mid.txt
set wids_file=e:/data/emadr/el/wiki/wiki_page_ids.bin
set mideid_file=e:/data/edl/res/prog-gen/mid-to-eid-ac.bin

set bin_dir=../out/production/zju-dcd-el
set apache_cli=e:/lib/commons-cli-1.3.1/commons-cli-1.3.1.jar
set classpath=%bin_dir%;%apache_cli%

java -cp %classpath% edu.zju.edl.EmadrDataMain ^
     -midtoeid %mideid_file% ^
     -info %info_file% ^
     -doclist %doclist_file% ^
     -docvecs %docvecs_file% ^
     -wikivecs %wikivecs_file% ^
     -midtowid %midwid_file% ^
     -wid %wids_file% ^
     -d %dst_file%
