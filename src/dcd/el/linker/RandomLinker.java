package dcd.el.linker;

import java.util.LinkedList;
import java.util.Random;

import dcd.el.ELConsts;
import dcd.el.dict.AliasDict;
import dcd.el.objects.Document;
import dcd.el.objects.LinkingResult;
import dcd.el.tac.MidToEidMapper;

public class RandomLinker extends LinkerWithAliasDict {

	public RandomLinker(AliasDict dict, MidToEidMapper mapper) {
		super(dict, mapper);
	}

	@Override
	public LinkingResult[] link(Document doc) {
		LinkingResult[] results = new LinkingResult[doc.mentions.length];

		for (int i = 0; i < results.length; ++i) {
			LinkingResult result = new LinkingResult();
			result.queryId = doc.mentions[i].queryId;
			result.kbid = ELConsts.NIL;

			LinkedList<String> mids = aliasDict
					.getMids(doc.mentions[i].nameString);
			if (mids != null) {
				int idx = random.nextInt(mids.size());
				int j = 0;
				for (String mid : mids) {
					if (j == idx)
						result.kbid = mid;
					++j;
				}
			}
			
			results[i] = result;
		}
		
		return results;
	}

	@Override
	public LinkingResult[] link14(Document doc) {
		LinkingResult[] results = new LinkingResult[doc.mentions.length];

		for (int i = 0; i < results.length; ++i) {
			LinkingResult result = new LinkingResult();
			result.queryId = doc.mentions[i].queryId;
			result.kbid = ELConsts.NIL;
			
			LinkedList<String> mids = aliasDict.getMids(doc.mentions[i].nameString);
			
			// randomly choose an eid from the candidates
			String[] eids = new String[mids.size()];
			int cnt = 0;
			for (String mid : mids) {
				String eid = mteMapper.getEid(mid);
				if (eid != null) {
					eids[cnt++] = eid;
				}
			}
			if (cnt > 0)
				result.kbid = eids[random.nextInt(cnt)];
		}
		
		return results;
	}

	private Random random = new Random();
}
