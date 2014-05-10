package com.scutdm.summary.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.scutdm.summary.analyz.ESentenceDistance;
import com.scutdm.summary.doc.ESentence;
import com.scutdm.summary.summary.ESubTopic;

/**
 * 英文
 * 句子DBSCAN聚类分析类
 * @author wRap
 *
 */
public class ESentenceDBSCAN {
	private double epsilon=0.6;//ε半径
	private int minPts=3;//密度阈值
	private List<ESentence> sentenceList=new ArrayList<ESentence>();//存储原始样本点
	private List<List<ESentence>> resultList=new ArrayList<List<ESentence>>();//存储最后的聚类结果
	
	public void Cluster(List<ESubTopic> subTopics, List<ESentence> sentences,double epsilon,int minPts){
		sentenceList=new ArrayList<ESentence>();
		resultList=new ArrayList<List<ESentence>>();
		setEpsilon(epsilon);
		setMinPts(minPts);
		sentenceList = sentences;
//		long beginTime = System.currentTimeMillis();
		System.out.println("\nStart to clustering sentences...");
		for(int index=0;index<sentenceList.size();++index){
  			if(index%100==0)
  				System.out.print(index + " ");
			List<ESentence> tmpLst=new ArrayList<ESentence>();
			ESentence p=sentenceList.get(index);
			if(p.isClassed()||p.concept.size()<3)
				continue;
			tmpLst=isKeyPoint(sentenceList, p, epsilon, minPts);
			if(tmpLst!=null){
				resultList.add(tmpLst);
			}
		}
		/*
		 * 簇的合并
		 */
		System.out.println("\nStart to merge clusters...");
		int length=resultList.size();
		for(int i=0;i<length;++i){
			for(int j=0;j<length;++j){
				if(i!=j){
					if(mergeList(resultList.get(i), resultList.get(j))){
						resultList.get(j).clear();
					}
				}
			}
		}
		/*
		 * 删除空的簇
		 */
		System.out.println("Start to delete null clusters...");
		Iterator<List<ESentence>> it = resultList.iterator();
		while(it.hasNext()){
			if(it.next().size()==0)
				it.remove();
		}
//		long runningTime = System.currentTimeMillis() - beginTime;
		
		Iterator<List<ESentence>> iterator = resultList.iterator();
		int index = 1;
		String output = "";
		while(iterator.hasNext()){
			Iterator<ESentence> iterator2 = iterator.next().iterator();
//			System.out.println("-----第"+index+"个聚类-----");
			output += "-----第"+index+"个聚类-----\n";
			List<ESentence> sentenceList = new ArrayList<ESentence>();	//记录该簇包含的句子
			Set<String> articleID = new HashSet<String>();
			while(iterator2.hasNext()){
				ESentence sentence = iterator2.next();
//				System.out.println(sentence.getSentenceString());
				output += sentence.getSentenceString() + "\n";
				for(String key:sentence.concept.keySet()){
//					System.out.print(key + "," + sentence.concept.get(key) + " ");
					output += key + "," + sentence.concept.get(key) + " ";
				}
				sentence.setClusterID(String.valueOf(index));	//设置簇标号
				//计算句子得分
				double score = 0;
				for(String key:sentence.concept.keySet()){
					score += sentence.concept.get(key);
				}
				score = score/sentence.getWords().length;
				if(Integer.parseInt(sentence.getPosition())<5){
					score = (1 + 1.0 * (5-Integer.parseInt(sentence.getPosition()))/5)*score;
				}
				sentence.setScore(score);
//				System.out.println();
				output += "\nScore:"+sentence.getScore()+" ArticleID:"+sentence.getArticleID()
						+" Position:"+sentence.getPosition() + "\n";
				sentenceList.add(sentence);
				articleID.add(sentence.getArticleID());
			}
			subTopics.add(new ESubTopic(sentenceList, articleID.size()));
			index++;
		}
//		System.out.println("簇个数：" + --index);
//		System.out.println("用时：" + runningTime + "毫秒");
//		System.out.println(resultList.size());
		
		//把聚类结果输出到文件
//		FileOutputStream outSTr = null;
//		BufferedOutputStream Buff=null;
//		try {
//			outSTr = new FileOutputStream(new File("dataset/subtopic/topic3_2.txt"));
//			Buff=new BufferedOutputStream(outSTr);
//			Buff.write(output.getBytes());
//			Buff.flush();
//			Buff.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	//检测p点是不是核心点，tmpLst存储核心点的直达点
	private List<ESentence> isKeyPoint(List<ESentence> lst,ESentence p,double e,int minp){
		int count=0;
		List<ESentence> tmpLst=new ArrayList<ESentence>();
		for(Iterator<ESentence> it=lst.iterator();it.hasNext();){
			ESentence q=it.next();
			if(q.concept.size()<3)
				continue;
			ESentenceDistance distance= new ESentenceDistance();
			if(distance.getSimilarity1(p, q)>=e){
				++count;
				if(!tmpLst.contains(q)){
					tmpLst.add(q);
					}
				}
			}
		if(count>=minp){
			p.setKey(true);
			return tmpLst;
			}
		return null;
	}
		
	//合并两个链表，前提是b中的核心点包含在a中
	private boolean mergeList(List<ESentence> a,List<ESentence> b){
		boolean merge=false;
		if(a==null || b==null){
			return false;
			}
		for(int index=0;index<b.size();++index){
			ESentence p=b.get(index);
			if(p.isKey() && a.contains(p)){
				merge=true;
				break;
				}
			}
		if(merge){
			for(int index=0;index<b.size();++index){
				if(!a.contains(b.get(index))){
					a.add(b.get(index));
					}
				}
			}
		return merge;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public int getMinPts() {
		return minPts;
	}

	public void setMinPts(int minPts) {
		this.minPts = minPts;
	}
}