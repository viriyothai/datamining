package mining.EntityLinkingControl;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mining.Database.*;
import mining.EntityLinkingStructure.CandidateStore;
import mining.EntityLinkingStructure.EntityStore;
import mining.EntityLinkingStructure.GraphStructure;
import opennlp.maxent.Counter;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.InvalidFormatException;

import javax.json.*;
import org.json.*;

public class EntityLinking {

	private String paragraph;
	private ArrayList<String> entities;
	private ArrayList<EntityStore> storeArr;
	private GraphStructure g;

	public EntityLinking(String inputText) throws InvalidFormatException, IOException, SQLException {
		entities = new ArrayList<String>();
		paragraph = inputText;
		findEntitiesMention(paragraph);
		findCandidateEntities(storeArr);
		findPageLink(storeArr);
		findTopicalRelatedness();

		for (EntityStore entity : storeArr) {
			findPriorProbability(entity);
		}

		findContextSimilarity();

	}

	public ArrayList<EntityStore> getEntityStoreArr() {
		return storeArr;
	}

	public String readURL(String urlString) throws IOException {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public void findEntitiesMention(String paragraph) throws InvalidFormatException, IOException, SQLException {
		// Find name entities
		ExtractEntity extractEntities = new ExtractEntity();
		String[] sentences = extractEntities.findSentences(paragraph);
		storeArr = new ArrayList<>();

		for (String sentence : sentences) {
			extractEntities.extractNameEntities(sentence);
			for (String entity : extractEntities.getNameEntities()) {
				EntityStore newEntity = new EntityStore();
				newEntity.setEntityMention(replaceCharMention(entity));
				newEntity.setSentence(sentence);
				storeArr.add(newEntity);
			}
		}
	}

	public void findCandidateEntities(ArrayList<EntityStore> storeArr) throws SQLException {
		GraphDatabase graphDB = new GraphDatabase();

		ArrayList<CandidateStore> candidate = new ArrayList<>();

		// System.out.println("Size: " + entities.size());
		for (EntityStore entity : storeArr) {
			candidate = graphDB.findCandidateFromDB(replaceCharDB(entity.getEntityMention()));
			entity.setCandidateEntities(candidate);
		}
	}

	public void findPageLink(ArrayList<EntityStore> storeArr) throws IOException {
		for (EntityStore candidateEntitiesArr : storeArr) {
			ArrayList<CandidateStore> candidateEntities = candidateEntitiesArr.getCandidateEntities();

			for (CandidateStore candidateStore : candidateEntities) {
				String candidateEntity = candidateStore.getCandidateName();
				ArrayList<String> title = new ArrayList<>();
				try {
					JSONObject json = new JSONObject(
							readURL("https://en.wikipedia.org/w/api.php?action=query&list=backlinks&format=json&bltitle="
									+ candidateEntity
									+ "&bldir=ascending&blfilterredir=nonredirects&bllimit=500&generator=revisions"));

					json = json.getJSONObject("query");
					JSONArray data = json.getJSONArray("backlinks");

					if (data != null) {
						for (int i = 0; i < data.length(); i++) {
							json = data.getJSONObject(i);
							if (json != null) {
								title.add(json.getString("title"));
							}
						}
					}
					candidateStore.setPageLink(title);
					System.out.println(candidateEntity + " : " + title.size());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void findTopicalRelatedness() {
		g = new GraphStructure(storeArr);

		for (int i = 0; i < storeArr.size(); i++) {
			for (int j = i + 1; j < storeArr.size(); j++) {
				for (CandidateStore candidate1 : storeArr.get(i).getCandidateEntities()) {
					for (CandidateStore candidate2 : storeArr.get(j).getCandidateEntities()) {
						String[] key = new String[2];
						key[0] = candidate1.getCandidateName();
						key[1] = candidate2.getCandidateName();
						int count = 0;
						// ArrayList<String> link1 = candidate1.getPageLink();
						// link1.retainAll(candidate2.getPageLink());

						for (String link : candidate1.getPageLink()) {
							for (String link2 : candidate2.getPageLink()) {
								if (link.equals(link2))
									count++;
							}
						}
						g.setEdges(key, calculateTopicalRelatedness((double) candidate1.getPageLink().size(),
								(double) candidate2.getPageLink().size(), 3381716.0, (double) count));
					}
				}
			}
		}
	}

	public double calculateTopicalRelatedness(double countLink1, double countLink2, double entireLink,
			double countSameLink) {
		double TR = 0;
		if (countSameLink == 0) {
			TR = 0;
		} else {
			TR = 1 - ((Math.log(Math.max(countLink1, countLink2)) - Math.log(countSameLink))
					/ (Math.log(entireLink) - Math.log(Math.min(countLink1, countLink2))));
		}
		return TR;
	}

	public void findPriorProbability(EntityStore entity) {
		int sum = 0;
		for (CandidateStore candidate : entity.getCandidateEntities()) {
			sum += candidate.getPageLink().size();
		}
		for (CandidateStore candidate : entity.getCandidateEntities()) {
			candidate.setPriorProbability((double) candidate.getPageLink().size() / (double) sum);
		}
	}

	public void findContextSimilarity() throws IOException {
		for (EntityStore entitiesArr : storeArr) {
			HashMap<String, Integer> mentionedEn = findBagWordsMention(entitiesArr.getEntityMention(),
					replaceExtraForContext(entitiesArr.getSentence()));
			for (CandidateStore candidate : entitiesArr.getCandidateEntities()) {
				HashMap<String, Integer> candidateEn = findBagWordsCandidate(candidate);
				Set<String> combineEn = new TreeSet<String>();
				combineEn.addAll(mentionedEn.keySet());
				combineEn.addAll(candidateEn.keySet());

				// List<ArrayList> counts = new ArrayList<ArrayList>();
				ArrayList<Integer> count1 = new ArrayList<Integer>();
				ArrayList<Integer> count2 = new ArrayList<Integer>();
				for (String word : combineEn) {
					int temp1 = mentionedEn.get(word) != null ? mentionedEn.get(word) : 0;
					int temp2 = candidateEn.get(word) != null ? candidateEn.get(word) : 0;
					count1.add(temp1);
					count2.add(temp2);
				}
				Double magnitude1 = 0.0;
				for (Integer value : count1) {
					magnitude1 += value * value;
				}
				magnitude1 = Math.sqrt(magnitude1);
				//System.out.println("Magnitude1 : " + magnitude1);

				Double magnitude2 = 0.0;
				for (Integer value : count2) {
					magnitude2 += value * value;
				}
				magnitude2 = Math.sqrt(magnitude2);
				//System.out.println("Magnitude2 : " + magnitude2);
				
				Double multipleMagnitude = 0.0;
				for (int i = 0; i < count1.size(); i++) {
					multipleMagnitude += count1.get(i) * count2.get(i);
				}
				//System.out.println("MultipleMagnitude: " + multipleMagnitude);
				Double cosin = multipleMagnitude / (magnitude1 * magnitude2);
				System.out.println("Cosin Similarity between " + entitiesArr.getEntityMention() + " & " 
				+ candidate.getCandidateName() + ": " + cosin);
			}
		}
	}

	public HashMap<String, Integer> findBagWordsMention(String entityMention, String sentence)
			throws InvalidFormatException, IOException {
		ArrayList<String> words = new ArrayList<String>();
		ExtractEntity extract = new ExtractEntity();
		words.addAll(Arrays.asList(extract.findTokens(sentence)));
		Collections.sort(words);
		int count = 1;
		HashMap<String, Integer> wordsFrequent = new HashMap<String, Integer>();
		for (int i = 0; i < words.size(); i++) {
			if (i != words.size() - 1) {
				if (words.get(i).equals(words.get(i + 1))) {
					count++;
				} else {
					System.out.println(words.get(i) + " : " + count);
					wordsFrequent.put(words.get(i), count);
					count = 1;
				}
			} else {
				System.out.println(words.get(i) + " : " + count);
				wordsFrequent.put(words.get(i), count);
				count = 1;
			}
		}
		return wordsFrequent;
	}

	public HashMap<String, Integer> findBagWordsCandidate(CandidateStore candidateStore) throws IOException {
		HashMap<String, Integer> wordsFrequent = new HashMap<String, Integer>();
		String candidateEntity = candidateStore.getCandidateName();
		ArrayList<String> words = new ArrayList<String>();
		ExtractEntity extract = new ExtractEntity();

		try {
			JSONObject json = new JSONObject(
					readURL("https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srsearch="
							+ candidateEntity + "&srwhat=text&srprop=snippet&srlimit=50&generator=revisions"));
			json = json.getJSONObject("query");
			JSONArray data = json.getJSONArray("search");
			//System.out.println("Candidate: " + candidateEntity);
			candidateEntity = candidateEntity.replace("_", " ");
			if (data != null) {
				for (int i = 0; i < data.length(); i++) {
					json = data.getJSONObject(i);
					if (json != null) {
						String contextString = json.getString("snippet").replace("<span class=\"searchmatch\">", "");
						contextString = contextString.replace("</span>", "");
						//System.out.println("\n" + contextString);
						String[] sentences = extract.findSentences(contextString);
						for (String sentence : sentences) {
							if (sentence.contains(candidateEntity)) {
								words.addAll(Arrays.asList(extract.findTokens(sentence)));
							}
						}
					}
				}
			}
		} catch (

		JSONException e)

		{
			e.printStackTrace();
		}
		Collections.sort(words);
		//System.out.println("words: " + words.size());

		int count = 1;
		for (int i = 0; i < words.size(); i++)

		{
			if (i != words.size() - 1) {
				if (words.get(i).equals(words.get(i + 1))) {
					count++;
				} else {
					System.out.println(words.get(i) + " : " + count);
					wordsFrequent.put(words.get(i), count);
					count = 1;
				}
			} else {
				System.out.println(words.get(i) + " : " + count);
				wordsFrequent.put(words.get(i), count);
				count = 1;
			}
		}
		return wordsFrequent;

	}

	public void findCoherent() {

	}
	
	public String replaceCharMention(String input){
		return input;
	}

	public String replaceCharDB(String input) {
		input = input.replace(" ", "_");
		input = input.replace("'s", "");
		return input;
	}

	public String replaceExtraChar(String input) {
		input = input.replace("%28", "(");
		input = input.replace("%29", ")");
		input = input.replace("_", " ");
		input = input.replace("%2C", ",");
		input = input.replaceAll("[('s)@#\\%]", "");
		return input;
	}

	public String replaceExtraForContext(String input) {
		input = input.replace("#", "");
		return input;
	}

}
