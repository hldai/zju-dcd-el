// author: DHL brnpoem@gmail.com

package dcd.el.linker;

import java.util.LinkedList;
import java.util.Random;

import dcd.el.ELConsts;
import dcd.el.dict.AliasDict;
import dcd.el.dict.CandidatesRetriever;
import dcd.el.objects.ByteArrayString;
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
		CandidatesRetriever.Candidates[] candidates = candidatesRetriever
				.getCandidatesInDocument(doc);

		for (int i = 0; i < results.length; ++i) {
			LinkingResult result = new LinkingResult();
			result.queryId = doc.mentions[i].queryId;
			result.kbid = ELConsts.NIL;

			LinkedList<ByteArrayString> mids = candidates[i].mids;
			if (mids != null) {
				int idx = random.nextInt(mids.size());
				int j = 0;
				for (ByteArrayString mid : mids) {
					if (j == idx)
						result.kbid = mid.toString().trim();
					++j;
				}
			}
			
			results[i] = result;
		}
		
		return results;
	}

	@Override
	public LinkingResult[] link14(Document doc) {
		LinkingResult[] results = link(doc);
		for (LinkingResult result : results) {
			if (!result.kbid.equals(ELConsts.NIL)) {
				result.kbid = mteMapper.getEid(result.kbid);
				if (result.kbid == null) {
					result.kbid = ELConsts.NIL;
				}
			}
		}
		
		return results;
	}

	private Random random = new Random();
}
