package edu.zju.dcd.edl.wordvec;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.zju.dcd.edl.ELConsts;
import edu.zju.dcd.edl.io.IOUtils;
import edu.zju.dcd.edl.obj.ByteArrayString;
import edu.zju.dcd.edl.utils.MathUtils;

public class NameVecSimilarity {
	public NameVecSimilarity(String fileName, WordVectorSet wordVectorSet) {
		this.wordVectorSet = wordVectorSet;
		
		DataInputStream dis = IOUtils.getBufferedDataInputStream(fileName);
		try {
			int numEntities = dis.readInt();
			vecLen = dis.readInt();
			System.out.println(numEntities + "\t" + vecLen);
			
			mids = new ByteArrayString[numEntities];
			nameVecs = new float[numEntities][vecLen];
			for (int i = 0; i < numEntities; ++i) {
				mids[i] = new ByteArrayString();
				mids[i].fromFileWithFixedLen(dis, ELConsts.MID_BYTE_LEN);
				for (int j = 0; j < vecLen; ++j) {
					nameVecs[i][j] = dis.readFloat();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public float getSimilarity(String text, ByteArrayString mid) {
		int midPos = Arrays.binarySearch(mids, mid);
		if (midPos < 0)
			return 0;
		
		float[] textVec = getAvgVecForText(text);
		float rslt = 0;
		for (int i = 0; i < vecLen; ++i) {
			rslt += textVec[i] * nameVecs[midPos][i];
		}
		
		return rslt;
	}
	
	public float[] getAvgVecForText(String text) {
		float[] vec = new float[vecLen];
		
		StringReader sr = new StringReader(text);
		List<CoreLabel> labels = null;
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(sr, new CoreLabelTokenFactory(),
				"ptb3Escaping=false,untokenizable=noneKeep");
		labels = ptbt.tokenize();
		sr.close();
		int hitWordCnt = 0;
		for (CoreLabel label : labels) {
			float[] tmpVec = wordVectorSet.getVector(label.value().toLowerCase());
			if (tmpVec != null) {
				for (int j = 0; j < vec.length; ++j) {
					vec[j] += tmpVec[j];
				}
				++hitWordCnt;
			}
		}
		
		if (hitWordCnt > 0) {
			for (int i = 0; i < vec.length; ++i) {
				vec[i] /= hitWordCnt;
			}
		}
		
		MathUtils.toUnitVector(vec);
		
		return vec;
	}
	
	ByteArrayString[] mids = null;
	float[][] nameVecs = null;
	WordVectorSet wordVectorSet = null;
	int vecLen = 0;
}
