// author: DHL brnpoem@gmail.com

package edu.zju.dcd.edl.linker;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.cg.AliasDict;
import edu.zju.dcd.edl.obj.Document;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.tac.MidToEidMapper;

public class RandomLinker extends LinkerWithAliasDict {

	public RandomLinker(AliasDict dict, MidToEidMapper mapper) {
		super(dict, mapper);
	}

	// TODO
	@Override
	public LinkingResult[] link(Document doc) {
		LinkingResult[] results = new LinkingResult[doc.mentions.length];
//		CandidatesRetriever.Candidates[] candidates = candidatesRetriever
//				.getCandidatesInDocument(doc);
//
//		for (int i = 0; i < results.length; ++i) {
//			LinkingResult result = new LinkingResult();
//			result.queryId = doc.mentions[i].queryId;
//			result.kbid = ELConsts.NIL;
//
//			LinkedList<ByteArrayString> mids = candidates[i].mids;
//			if (mids != null) {
//				int idx = random.nextInt(mids.size());
//				int j = 0;
//				for (ByteArrayString mid : mids) {
//					if (j == idx)
//						result.kbid = mid.toString().trim();
//					++j;
//				}
//			}
//			
//			results[i] = result;
//		}
		
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

//	private Random random = new Random();
}
