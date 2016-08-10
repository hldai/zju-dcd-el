package edu.zju.edl.prep;

import edu.zju.edl.utils.IOUtils;
import edu.zju.edl.obj.ByteArrayString;

import java.io.DataOutputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by dhl on 16-7-30.
 */
public class CandidatesDictGen {
    private static class CandidateEntry implements Comparable<CandidateEntry> {
        ByteArrayString mid;
        int cnt;

        @Override
        public int compareTo(CandidateEntry c) {
            return c.cnt - cnt;
        }
    }

    private static class AliasEntry implements Comparable<AliasEntry> {
        ByteArrayString alias;
        ByteArrayString[] mids;
        int[] cnts;

        @Override
        public int compareTo(AliasEntry ae) {
            return alias.compareTo(ae.alias);
        }
    }

    private static AliasEntry handleCandidateEntries(LinkedList<CandidateEntry> candidateEntries) {
        Collections.sort(candidateEntries);

        int len = candidateEntries.size() > 50 ? 50 : candidateEntries.size();

        AliasEntry aliasEntry = new AliasEntry();
        aliasEntry.mids = new ByteArrayString[len];
        aliasEntry.cnts = new int[len];
        Iterator<CandidateEntry> iter = candidateEntries.iterator();
        for (int i = 0; i < len; ++i) {
            CandidateEntry ce = iter.next();
            aliasEntry.mids[i] = ce.mid;
            aliasEntry.cnts[i] = ce.cnt;
        }

        return aliasEntry;
//		System.out.println(aliasEntry.alias.toString());
//		for (CandidateEntry ce : aliasEntry.candidateEntries) {
//			System.out.println(String.format("%s\t%d", ce.mid.toString(), ce.cnt));
//		}
//		System.out.println();
    }

    private static LinkedList<AliasEntry> loadMidAliasCntFile(String midAliasCntFile) throws IOException {
        System.out.println(String.format("Loading %s ...", midAliasCntFile));

        BufferedReader reader = IOUtils.getUTF8BufReader(midAliasCntFile);
        String line = null;
        int lineCnt = 0;
        String prevAlias = "";
        LinkedList<AliasEntry> aliasEntries = new LinkedList<>();
        LinkedList<CandidateEntry> candidateEntries = null;
        while ((line = reader.readLine()) != null) {
            String[] vals = line.split("\t");

            CandidateEntry candidateEntry = new CandidateEntry();
            candidateEntry.mid = new ByteArrayString(vals[0]);
            candidateEntry.cnt = Integer.valueOf(vals[2]);

            if (prevAlias.equals(vals[1])) {
                candidateEntries.add(candidateEntry);
            } else {
                if (candidateEntries != null) {
                    AliasEntry aliasEntry = handleCandidateEntries(candidateEntries);
                    aliasEntry.alias = new ByteArrayString(prevAlias);
                    if (aliasEntry.alias.bytes.length < 128)
                        aliasEntries.add(aliasEntry);
                }

                candidateEntries = new LinkedList<>();
                candidateEntries.add(candidateEntry);
            }

            prevAlias = vals[1];

            ++lineCnt;
//			if (lineCnt == 100)
//				break;
            if (lineCnt % 1000000 == 0)
                System.out.println(lineCnt);
        }
        reader.close();
        AliasEntry aliasEntry = handleCandidateEntries(candidateEntries);
        aliasEntry.alias = new ByteArrayString(prevAlias);
        if (aliasEntry.alias.bytes.length < 128)
            aliasEntries.add(aliasEntry);

        System.out.println("Done");

        return aliasEntries;
    }

    private static void writeCandidatesFile(LinkedList<AliasEntry> aliasEntries,
                                            String dstFile) throws IOException {
        DataOutputStream dos = IOUtils.getBufferedDataOutputStream(dstFile);
        System.out.println(String.format("Writing %d aliases ...", aliasEntries.size()));

        int totalNumCandidates = 0;
        for (AliasEntry aliasEntry : aliasEntries) {
            totalNumCandidates += aliasEntry.mids.length;
        }

        dos.writeInt(aliasEntries.size());
        dos.writeInt(totalNumCandidates);
        for (AliasEntry aliasEntry : aliasEntries) {
            aliasEntry.alias.toFileWithByteLen(dos);
            dos.writeShort(aliasEntry.mids.length);

            float sum = 0;
            for (int cnt : aliasEntry.cnts) {
                sum += cnt;
            }

            for (int i = 0; i < aliasEntry.mids.length; ++i) {
                aliasEntry.mids[i].toFileWithByteLen(dos);
                dos.writeFloat(aliasEntry.cnts[i] / sum);
            }
        }
        dos.close();
    }

    private static void genDict() throws IOException {
        String midAliasCntFile = "/home/dhl/data/EDL/tmpres/mid-alias-cnt-ord-alias-filtered.txt";
        String dstFile = "/home/dhl/data/EDL/tmpres/candidates-dict.bin";

        LinkedList<AliasEntry> aliasEntries = loadMidAliasCntFile(midAliasCntFile);
        Collections.sort(aliasEntries);
        writeCandidatesFile(aliasEntries, dstFile);

//		int cnt = 0;
//		for (AliasEntry ae : aliasEntries) {
//			System.out.println(ae.alias.toString());
//			for (int i = 0; i < ae.mids.length; ++i) {
//				System.out.println(String.format("%s\t%d", ae.mids[i].toString(), ae.cnts[i]));
//			}
//			System.out.println();
//			cnt += 1;
//			if (cnt == 10)
//				break;
//		}
    }

    public static void main(String args[]) throws IOException {
        genDict();
    }
}
