package edu.zju.dcd.edl.tac;

import java.util.*;

import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.obj.LinkingResult;
import edu.zju.dcd.edl.obj.Mention;

public class CrossDocNilHandler {
//	private static class NilQueryList {
//		String nilId = null;
//		LinkedList<Query> queries = new LinkedList<Query>();
//	}

	// mentions of one entity
	private static class MentionsOfEntity {
		String kbid;
		LinkedList<Mention> mentions = new LinkedList<>();
	}
	
	public static void handle(HashMap<String, String> mentionIdToKbid, String edlFile) {
		System.out.println("Handling cross doc nils...");

//		LinkedList<Mention> mentions = QueryReader.loadMentionsEdlFile(edlFile);
		LinkedList<Mention> mentions = Mention.loadEdlFile(edlFile);
		LinkedList<MentionsOfEntity> mentionsOfEntities = getMentionsOfNilEntities(mentionIdToKbid, mentions);

		LinkedList<MentionsOfEntity> newMentionsOfEntities = new LinkedList<>();
		for (MentionsOfEntity me : mentionsOfEntities) {
			boolean merge = false;
			for (MentionsOfEntity nme : newMentionsOfEntities) {
				merge = shouldMerge(me.mentions, nme.mentions);
				if (merge) {
					nme.mentions.addAll(me.mentions);
					fixNilIds(me.mentions, nme.kbid, mentionIdToKbid);
					break;
				}
			}

			if (!merge) {
				newMentionsOfEntities.add(me);
			}
		}

		System.out.println("Done.");
	}

	private static void fixNilIds(LinkedList<Mention> mentions, String nilId,
								  HashMap<String, String> mentionIdToKbid) {
		for (Mention m : mentions) {
			mentionIdToKbid.put(m.mentionId, nilId);
//			System.out.println(m.mentionId + " --> " + nilId);
		}
	}

	private static boolean shouldMerge(LinkedList<Mention> mentions0, LinkedList<Mention> mentions1) {
		for (Mention m0 : mentions0) {
			for (Mention m1 : mentions1) {
				if (!m0.isNominal() && !m1.isNominal() && m0.nameString.toLowerCase().equals(
						m1.nameString.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	private static LinkedList<MentionsOfEntity> getMentionsOfNilEntities(Map<String, String> mentionIdToKbid,
																		 LinkedList<Mention> mentions) {
		LinkedList<MentionsOfEntity> mentionsOfNilEntities = new LinkedList<>();
		for (Mention m : mentions) {
			String kbid = mentionIdToKbid.get(m.mentionId);
			assert kbid != null;

			if (kbid.startsWith(ELConsts.NIL)) {
				MentionsOfEntity curMentions = findMentionsOfEntityById(mentionsOfNilEntities, kbid);
				if (curMentions == null) {
					curMentions = new MentionsOfEntity();
					curMentions.kbid = kbid;
					mentionsOfNilEntities.add(curMentions);
				}
				curMentions.mentions.add(m);
			}
		}

		return mentionsOfNilEntities;
	}

	private static MentionsOfEntity findMentionsOfEntityById(LinkedList<MentionsOfEntity> mentionsOfEntities,
															 String kbid) {
		for (MentionsOfEntity me : mentionsOfEntities) {
			if (me.kbid.equals(kbid))
				return me;
		}
		return null;
	}
}
